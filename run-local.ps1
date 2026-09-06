# Same path as scripts/local-up.ps1: Docker MySQL on 127.0.0.1:3307, then Gradle bootRun with the dev profile.
$ErrorActionPreference = "Stop"
& "$PSScriptRoot\scripts\local-up.ps1"
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}
