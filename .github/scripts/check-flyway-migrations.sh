#!/usr/bin/env bash

set -euo pipefail
export LC_ALL=C

base_sha="${1:?base SHA is required}"
head_sha="${2:-HEAD}"
migration_dir="src/main/resources/db/migration"
failed=0
inspection_dir="$(mktemp -d)"

cleanup() {
  rm -rf -- "$inspection_dir"
}
trap cleanup EXIT

normalize_version() {
  local path="$1"
  local filename="${path##*/}"
  local version

  if [[ ! "$filename" =~ ^V((0|[1-9][0-9]*)([._](0|[1-9][0-9]*))*)__.+\.sql$ ]]; then
    return 1
  fi

  version="${BASH_REMATCH[1]//_/.}"
  IFS='.' read -r -a segments <<< "$version"

  while (( ${#segments[@]} > 1 )); do
    local last_index=$((${#segments[@]} - 1))
    if [[ "${segments[$last_index]}" != "0" ]]; then
      break
    fi
    unset 'segments[last_index]'
  done

  local normalized
  normalized="$(IFS='.'; echo "${segments[*]}")"
  printf '%s\n' "$normalized"
}

version_is_greater() {
  local candidate="$1"
  local baseline="$2"
  local greatest

  greatest="$(printf '%s\n%s\n' "$candidate" "$baseline" | sort -V | tail -n 1)"
  [[ "$candidate" != "$baseline" && "$greatest" == "$candidate" ]]
}

is_versioned_migration() {
  local filename="${1##*/}"
  [[ "$filename" == V*.sql ]]
}

immutable_file="$inspection_dir/immutable"
if ! git diff --name-status -z --no-renames --diff-filter=MDT "$base_sha"..."$head_sha" -- "$migration_dir" > "$immutable_file"; then
  echo "Could not compare released Flyway migrations." >&2
  exit 1
fi

immutable_changes=()
while IFS= read -r -d '' status && IFS= read -r -d '' path; do
  if is_versioned_migration "$path"; then
    immutable_changes+=("$status"$'\t'"$path")
  fi
done < "$immutable_file"

if (( ${#immutable_changes[@]} > 0 )); then
  echo "Released Flyway migrations are immutable." >&2
  printf '%s\n' "${immutable_changes[@]}" >&2
  failed=1
fi

base_file="$inspection_dir/base"
if ! git ls-tree -r -z --name-only "$base_sha" -- "$migration_dir" > "$base_file"; then
  echo "Could not inspect released Flyway migrations." >&2
  exit 1
fi

base_versions=()
while IFS= read -r -d '' path; do
  is_versioned_migration "$path" || continue
  if ! version="$(normalize_version "$path")"; then
    echo "Existing migration has an unsupported version name: $path" >&2
    failed=1
    continue
  fi
  base_versions+=("$version")
done < "$base_file"

base_max="0"
if (( ${#base_versions[@]} > 0 )); then
  base_max="$(printf '%s\n' "${base_versions[@]}" | sort -V | tail -n 1)"
fi

added_file="$inspection_dir/added"
if ! git diff --name-status -z --no-renames --diff-filter=A "$base_sha"..."$head_sha" -- "$migration_dir" > "$added_file"; then
  echo "Could not inspect added Flyway migrations." >&2
  exit 1
fi

declare -A added_versions=()
while IFS= read -r -d '' status && IFS= read -r -d '' path; do
  is_versioned_migration "$path" || continue

  if ! version="$(normalize_version "$path")"; then
    echo "New migration must use V<numeric_version>__<description>.sql: $path" >&2
    failed=1
    continue
  fi

  if ! version_is_greater "$version" "$base_max"; then
    echo "New migration version $version must be greater than released version $base_max: $path" >&2
    failed=1
  fi

  if [[ -n "${added_versions[$version]:-}" ]]; then
    echo "New migrations contain duplicate Flyway version $version:" >&2
    echo "  ${added_versions[$version]}" >&2
    echo "  $path" >&2
    failed=1
  else
    added_versions[$version]="$path"
  fi
done < "$added_file"

exit "$failed"
