param(
    [string]$MigrationDirectory = (Join-Path $PSScriptRoot "..\src\main\resources\db\migration"),
    [string]$OutputDirectory = (Join-Path $PSScriptRoot "..\database")
)

$ErrorActionPreference = "Stop"

function Split-SqlStatements {
    param([Parameter(Mandatory)][string]$Sql)

    $statements = [System.Collections.Generic.List[string]]::new()
    $buffer = [System.Text.StringBuilder]::new()
    $inString = $false
    $inLineComment = $false

    for ($index = 0; $index -lt $Sql.Length; $index++) {
        $character = $Sql[$index]
        $next = if ($index + 1 -lt $Sql.Length) { $Sql[$index + 1] } else { [char]0 }

        if ($inLineComment) {
            if ($character -eq "`n") {
                $inLineComment = $false
                [void]$buffer.Append($character)
            }
            continue
        }

        if (-not $inString -and $character -eq '-' -and $next -eq '-') {
            $inLineComment = $true
            $index++
            continue
        }

        if ($character -eq "'") {
            [void]$buffer.Append($character)
            if ($inString -and $next -eq "'") {
                [void]$buffer.Append($next)
                $index++
                continue
            }
            $inString = -not $inString
            continue
        }

        [void]$buffer.Append($character)
        if (-not $inString -and $character -eq ';') {
            $statement = $buffer.ToString().Trim()
            if ($statement) {
                $statements.Add($statement)
            }
            [void]$buffer.Clear()
        }
    }

    $remaining = $buffer.ToString().Trim()
    if ($remaining) {
        $statements.Add($remaining)
    }
    return $statements
}

function Get-MigrationVersion {
    param([Parameter(Mandatory)][System.IO.FileInfo]$File)
    return [int]([regex]::Match($File.BaseName, '^V(\d+)').Groups[1].Value)
}

function Normalize-Statement {
    param([Parameter(Mandatory)][string]$Statement)
    return ($Statement.Trim() -replace "`r`n", "`n")
}

function Add-Section {
    param(
        [Parameter(Mandatory)][System.Text.StringBuilder]$Builder,
        [Parameter(Mandatory)][string]$Title,
        [Parameter(Mandatory)][AllowEmptyCollection()][string[]]$Statements
    )

    if ($Statements.Count -eq 0) {
        return
    }
    [void]$Builder.AppendLine()
    [void]$Builder.AppendLine("-- =============================================================================")
    [void]$Builder.AppendLine("-- $Title")
    [void]$Builder.AppendLine("-- =============================================================================")
    [void]$Builder.AppendLine()
    foreach ($statement in $Statements) {
        [void]$Builder.AppendLine((Normalize-Statement $statement))
        [void]$Builder.AppendLine()
    }
}

$migrationFiles = Get-ChildItem -LiteralPath $MigrationDirectory -Filter 'V*.sql' |
    Sort-Object { Get-MigrationVersion $_ }

if (-not $migrationFiles) {
    throw "No Flyway migrations found in $MigrationDirectory"
}
$latestVersion = Get-MigrationVersion $migrationFiles[-1]

$schemaBuilder = [System.Text.StringBuilder]::new()
$dataBuilder = [System.Text.StringBuilder]::new()

[void]$schemaBuilder.AppendLine("-- Generated from Flyway V1-V$latestVersion. Do not edit by hand.")
[void]$schemaBuilder.AppendLine('-- MySQL 8.0+. DESTRUCTIVE: recreates the local inspection database.')
[void]$schemaBuilder.AppendLine('SET NAMES utf8mb4;')
[void]$schemaBuilder.AppendLine('SET time_zone = ''+00:00'';')
[void]$schemaBuilder.AppendLine('SET FOREIGN_KEY_CHECKS = 0;')
[void]$schemaBuilder.AppendLine()
[void]$schemaBuilder.AppendLine('DROP DATABASE IF EXISTS lyrashop_db;')
[void]$schemaBuilder.AppendLine()
[void]$schemaBuilder.AppendLine('CREATE DATABASE lyrashop_db')
[void]$schemaBuilder.AppendLine('    CHARACTER SET utf8mb4')
[void]$schemaBuilder.AppendLine('    COLLATE utf8mb4_0900_ai_ci;')
[void]$schemaBuilder.AppendLine('USE lyrashop_db;')

[void]$dataBuilder.AppendLine('-- Seed/demo records separated from table definitions.')
[void]$dataBuilder.AppendLine('-- Requires 01_schema.sql to have been run first.')
[void]$dataBuilder.AppendLine('SET NAMES utf8mb4;')
[void]$dataBuilder.AppendLine('SET time_zone = ''+00:00'';')
[void]$dataBuilder.AppendLine('USE lyrashop_db;')
[void]$dataBuilder.AppendLine('SET FOREIGN_KEY_CHECKS = 0;')

$variantToProduct = @{}
foreach ($file in $migrationFiles) {
    $content = Get-Content -LiteralPath $file.FullName -Raw -Encoding UTF8
    $matches = [regex]::Matches(
        $content,
        "(?is)\(UUID_TO_BIN\('(?<variant>30000000-[0-9a-f-]+)'\),\s*UUID_TO_BIN\('(?<product>20000000-[0-9a-f-]+)'\)"
    )
    foreach ($match in $matches) {
        $variantToProduct[$match.Groups['variant'].Value] = $match.Groups['product'].Value
    }
}

foreach ($file in $migrationFiles) {
    $version = Get-MigrationVersion $file
    $statements = Split-SqlStatements (Get-Content -LiteralPath $file.FullName -Raw -Encoding UTF8)
    $schemaStatements = [System.Collections.Generic.List[string]]::new()
    $dataStatements = [System.Collections.Generic.List[string]]::new()

    foreach ($rawStatement in $statements) {
        $statement = Normalize-Statement $rawStatement
        if ($statement -match '(?is)^(CREATE\s+TABLE|ALTER\s+TABLE|CREATE\s+(?:UNIQUE\s+)?INDEX)\b') {
            $schemaStatements.Add($statement)
            continue
        }
        if ($statement -match '(?is)^INSERT(?:\s+IGNORE)?\s+INTO\b') {
            if ($version -in @(16, 24) -and $statement -match '(?is)^INSERT\s+INTO\b') {
                $statement = $statement -replace '(?is)^INSERT\s+INTO', 'INSERT IGNORE INTO'
            }
            if ($version -eq 10 -and $statement -match '(?is)^INSERT\s+IGNORE\s+INTO\s+order_items\b') {
                $statement = $statement -replace '(?i)variant_id,\s*product_name', 'variant_id, product_id, product_name'
                $statement = [regex]::Replace(
                    $statement,
                    "(?is)(\(\s*\d+\s*,\s*UUID_TO_BIN\('[0-9a-f-]+'\)\s*,\s*UUID_TO_BIN\('(?<variant>30000000-[0-9a-f-]+)'\))\s*,",
                    {
                        param($match)
                        $variant = $match.Groups['variant'].Value
                        if (-not $variantToProduct.ContainsKey($variant)) {
                            throw "Cannot resolve product_id for seeded variant $variant"
                        }
                        return "$($match.Groups[1].Value), UUID_TO_BIN('$($variantToProduct[$variant])'),"
                    }
                )
            }
            $dataStatements.Add($statement)
            continue
        }
        if ($statement -match '(?is)^UPDATE\b') {
            $dataStatements.Add($statement)
        }
    }

    Add-Section -Builder $schemaBuilder -Title "$($file.BaseName) - structure" -Statements $schemaStatements
    Add-Section -Builder $dataBuilder -Title "$($file.BaseName) - records" -Statements $dataStatements
}

Add-Section -Builder $dataBuilder -Title 'Final-schema data normalization' -Statements @(
    @'
UPDATE orders
SET subtotal_amount = total_amount
WHERE subtotal_amount = 0;
'@,
    @'
UPDATE orders
SET expires_at = CASE
    WHEN payment_method = 'VNPAY' AND payment_status = 'UNPAID'
        THEN DATE_ADD(created_at, INTERVAL 30 MINUTE)
    ELSE DATE_ADD(created_at, INTERVAL 24 HOUR)
END
WHERE status = 'PENDING' AND expires_at IS NULL;
'@
)

[void]$schemaBuilder.AppendLine('SET FOREIGN_KEY_CHECKS = 1;')
[void]$dataBuilder.AppendLine('SET FOREIGN_KEY_CHECKS = 1;')

$outputPath = [System.IO.Path]::GetFullPath($OutputDirectory)
[System.IO.Directory]::CreateDirectory($outputPath) | Out-Null

$utf8NoBom = [System.Text.UTF8Encoding]::new($false)
$schemaPath = Join-Path $outputPath '01_schema.sql'
$dataPath = Join-Path $outputPath '02_seed_data.sql'
[System.IO.File]::WriteAllText($schemaPath, $schemaBuilder.ToString(), $utf8NoBom)
[System.IO.File]::WriteAllText($dataPath, $dataBuilder.ToString(), $utf8NoBom)
[System.IO.File]::WriteAllText(
    [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..\lyrashop_schema.sql')),
    $schemaBuilder.ToString(),
    $utf8NoBom
)

$combined = [System.Text.StringBuilder]::new()
[void]$combined.AppendLine('-- LyraShop complete inspection database (schema + seed records).')
[void]$combined.AppendLine('-- Generated file. Prefer 01_schema.sql and 02_seed_data.sql for easier review.')
[void]$combined.AppendLine()
[void]$combined.AppendLine($schemaBuilder.ToString().TrimEnd())
[void]$combined.AppendLine()
[void]$combined.AppendLine($dataBuilder.ToString().TrimEnd())
[System.IO.File]::WriteAllText((Join-Path $outputPath 'lyrashop_db.sql'), $combined.ToString(), $utf8NoBom)

Write-Output "Generated: $schemaPath"
Write-Output "Generated: $dataPath"
Write-Output "Generated: $(Join-Path $outputPath 'lyrashop_db.sql')"
Write-Output "Updated: $(Join-Path $PSScriptRoot '..\lyrashop_schema.sql')"
