#!/usr/bin/env bash

set -euo pipefail

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
guard="$script_dir/check-flyway-migrations.sh"
test_root="$(mktemp -d)"
repository="$test_root/repository"

cleanup() {
  rm -rf "$test_root"
}
trap cleanup EXIT

git init -q "$repository"
cd "$repository"
git config user.name "Flyway Guard Test"
git config user.email "flyway-guard@example.invalid"
git config core.autocrlf false
mkdir -p src/main/resources/db/migration
mkdir -p drafts
printf '%s\n' 'CREATE TABLE identity_table (id INT PRIMARY KEY);' > src/main/resources/db/migration/V1__create_identity_table.sql
printf '%s\n' 'CREATE TABLE draft_table (id INT PRIMARY KEY);' > drafts/V2__draft_table.sql
git add src/main/resources/db/migration/V1__create_identity_table.sql drafts/V2__draft_table.sql
git commit -q -m "base migration"
base_sha="$(git rev-parse HEAD)"
real_git="$(command -v git)"
mock_bin="$test_root/mock-bin"
mkdir -p "$mock_bin"
cat > "$mock_bin/git" <<'MOCK_GIT'
#!/usr/bin/env bash
set -euo pipefail

if [[ "${FAIL_GIT_COMMAND:-}" == "ls-tree" && "$1" == "ls-tree" ]]; then
  exit 73
fi

if [[ "$1" == "diff" ]]; then
  for argument in "$@"; do
    if [[ "${FAIL_GIT_COMMAND:-}" == "immutable-diff" && "$argument" == "--diff-filter=MDT" ]]; then
      exit 74
    fi
    if [[ "${FAIL_GIT_COMMAND:-}" == "added-diff" && "$argument" == "--diff-filter=A" ]]; then
      exit 75
    fi
  done
fi

exec "$REAL_GIT" "$@"
MOCK_GIT
chmod +x "$mock_bin/git"

reset_case() {
  git reset -q --hard "$base_sha"
  git clean -q -fd
}

commit_case() {
  git add src/main/resources/db/migration
  git commit -q -m "test case"
}

expect_pass() {
  local name="$1"
  if ! bash "$guard" "$base_sha" HEAD; then
    echo "Expected guard to pass: $name" >&2
    exit 1
  fi
}

expect_failure() {
  local name="$1"
  local expected_message="$2"
  local output

  if output="$(bash "$guard" "$base_sha" HEAD 2>&1)"; then
    echo "Expected guard to fail: $name" >&2
    exit 1
  fi
  if ! grep -Fq "$expected_message" <<< "$output"; then
    echo "Guard failed without expected message for: $name" >&2
    echo "$output" >&2
    exit 1
  fi
}

expect_git_failure() {
  local failed_command="$1"
  local expected_message="$2"
  local output

  if output="$(PATH="$mock_bin:$PATH" REAL_GIT="$real_git" FAIL_GIT_COMMAND="$failed_command" bash "$guard" "$base_sha" HEAD 2>&1)"; then
    echo "Expected guard to fail closed when git command fails: $failed_command" >&2
    exit 1
  fi
  if ! grep -Fq "$expected_message" <<< "$output"; then
    echo "Guard did not report the expected git failure: $failed_command" >&2
    echo "$output" >&2
    exit 1
  fi
}

expect_pass "unchanged migrations"
expect_git_failure "immutable-diff" "Could not compare released Flyway migrations."
expect_git_failure "ls-tree" "Could not inspect released Flyway migrations."
expect_git_failure "added-diff" "Could not inspect added Flyway migrations."

reset_case
printf '%s\n' 'CREATE TABLE second_table (id INT PRIMARY KEY);' > src/main/resources/db/migration/V2__create_second_table.sql
commit_case
expect_pass "next integer version"

reset_case
printf '%s\n' 'CREATE TABLE patch_table (id INT PRIMARY KEY);' > src/main/resources/db/migration/V1_1__create_patch_table.sql
commit_case
expect_pass "next dotted version"

reset_case
unicode_name="$(printf 'V2__unicode_\303\251.sql')"
printf '%s\n' 'CREATE TABLE unicode_table (id INT PRIMARY KEY);' > "src/main/resources/db/migration/$unicode_name"
commit_case
expect_pass "unicode migration description"

reset_case
printf '%s\n' 'CREATE TABLE trailing_zero_table (id INT PRIMARY KEY);' > src/main/resources/db/migration/V2_0__create_trailing_zero_table.sql
commit_case
expect_pass "single trailing-zero version"

reset_case
printf '%s\n' 'ALTER TABLE identity_table ADD COLUMN name VARCHAR(255);' >> src/main/resources/db/migration/V1__create_identity_table.sql
commit_case
expect_failure "modified released migration" "Released Flyway migrations are immutable."

reset_case
printf '%s\n' 'CREATE TABLE zero_table (id INT PRIMARY KEY);' > src/main/resources/db/migration/V0__create_zero_table.sql
commit_case
expect_failure "out-of-order version" "must be greater than released version"

reset_case
printf '%s\n' 'CREATE TABLE padded_table (id INT PRIMARY KEY);' > src/main/resources/db/migration/V02__create_padded_table.sql
commit_case
expect_failure "ambiguous padded version" "must use V<numeric_version>__<description>.sql"

reset_case
printf '%s\n' 'CREATE TABLE second_table (id INT PRIMARY KEY);' > src/main/resources/db/migration/V2__create_second_table.sql
printf '%s\n' 'CREATE TABLE duplicate_table (id INT PRIMARY KEY);' > src/main/resources/db/migration/V2_0__create_duplicate_table.sql
commit_case
expect_failure "equivalent duplicate versions" "duplicate Flyway version 2"

reset_case
printf '%s\n' 'CREATE TABLE second_table (id INT PRIMARY KEY);' > src/main/resources/db/migration/V2__create_second_table.sql
printf '%s\n' 'CREATE TABLE third_table (id INT PRIMARY KEY);' > src/main/resources/db/migration/V3__create_third_table.sql
commit_case
expect_pass "multiple increasing versions"

reset_case
printf '%s\n' 'CREATE TABLE malformed_table (id INT PRIMARY KEY);' > src/main/resources/db/migration/V2_create_malformed_table.sql
commit_case
expect_failure "malformed migration name" "must use V<numeric_version>__<description>.sql"

reset_case
mkdir -p src/main/resources/db/migration/archive
printf '%s\n' 'CREATE TABLE nested_zero_table (id INT PRIMARY KEY);' > src/main/resources/db/migration/archive/V0__create_nested_zero_table.sql
commit_case
expect_failure "nested out-of-order version" "must be greater than released version"

reset_case
git mv drafts/V2__draft_table.sql src/main/resources/db/migration/V2__draft_table.sql
git commit -q -m "promote migration draft"
expect_pass "migration promoted from outside the migration directory"

reset_case
git mv src/main/resources/db/migration/V1__create_identity_table.sql src/main/resources/db/migration/V2__create_identity_table.sql
commit_case
expect_failure "renamed released migration" "Released Flyway migrations are immutable."

reset_case
git rm -q src/main/resources/db/migration/V1__create_identity_table.sql
git commit -q -m "empty migration base"
expect_failure "deleted released migration" "Released Flyway migrations are immutable."
empty_base_sha="$(git rev-parse HEAD)"
mkdir -p src/main/resources/db/migration
printf '%s\n' 'CREATE TABLE identity_table (id INT PRIMARY KEY);' > src/main/resources/db/migration/V1__create_identity_table.sql
commit_case
if ! bash "$guard" "$empty_base_sha" HEAD; then
  echo "Expected guard to pass: first migration on empty base" >&2
  exit 1
fi

reset_case
git mv src/main/resources/db/migration/V1__create_identity_table.sql src/main/resources/db/migration/V9__create_identity_table.sql
git commit -q -am "version nine base"
version_nine_base_sha="$(git rev-parse HEAD)"
printf '%s\n' 'CREATE TABLE tenth_table (id INT PRIMARY KEY);' > src/main/resources/db/migration/V10__create_tenth_table.sql
commit_case
if ! bash "$guard" "$version_nine_base_sha" HEAD; then
  echo "Expected guard to compare V10 greater than V9 numerically" >&2
  exit 1
fi

echo "Flyway migration guard tests passed."
