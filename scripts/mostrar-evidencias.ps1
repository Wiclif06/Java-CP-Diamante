$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location $projectRoot

function Show-Section([string]$title) {
    Write-Host ""
    Write-Host ("=" * 72) -ForegroundColor Cyan
    Write-Host $title -ForegroundColor Cyan
    Write-Host ("=" * 72) -ForegroundColor Cyan
}

Show-Section "01 - Versoes do Docker"
docker --version
docker compose version

Show-Section "02 - Containers do CampusGigs"
docker compose ps

Show-Section "03 - Migration Flyway"
docker compose exec -T db psql -U campusgigs -d campusgigs -c "select installed_rank, version, description, success from flyway_schema_history;"

Show-Section "04 - Casos manuais importantes"
$evidencePath = Join-Path $projectRoot "docs\evidencia-manual.json"
if (-not (Test-Path -LiteralPath $evidencePath)) {
    throw "Execute scripts\demo.ps1 antes de mostrar as evidencias."
}
$evidence = Get-Content -LiteralPath $evidencePath -Raw -Encoding UTF8 | ConvertFrom-Json
$importantCases = $evidence.tests | Where-Object {
    $_.expectedStatus -in @(401, 403, 409, 422) -or
    $_.case -like "*ADMIN*"
}
$importantCases | Format-Table case, expectedStatus, observedStatus, passed, responseCode -AutoSize
$evidence.totals | Format-List

Show-Section "05 - Cinco checkpoints e revisao final"
git log --oneline --reverse

Write-Host ""
Write-Host "Evidencias exibidas sem senhas e sem tokens." -ForegroundColor Green
