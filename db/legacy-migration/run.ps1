# Legacy -> public2 migration runner (Windows PowerShell). ASCII only.
# Usage:
#   .\run.ps1 -Psql "C:\Program Files\PostgreSQL\17\bin\psql.exe"
#   Log:  .\run.ps1 ... *> migration-log.txt
# Prompts once for DB name, port and password; pass them as params to skip a prompt.
param(
    [string]$DbHost = 'localhost',
    [int]$Port      = 0,
    [string]$Db     = '',
    [string]$User   = 'postgres',
    [string]$Psql   = 'psql'
)
# psql NOTICE goes to stderr; do not let PowerShell treat it as a fatal error.
$ErrorActionPreference = 'Continue'
$env:PGOPTIONS = '-c client_min_messages=warning'
$env:PGCLIENTENCODING = 'UTF8'

if (-not $Db) {
    $Db = Read-Host "Database name [isemid]"
    if (-not $Db) { $Db = 'isemid' }
}
if ($Port -eq 0) {
    $portInput = Read-Host "Port [5434]"
    if ($portInput) { $Port = [int]$portInput } else { $Port = 5434 }
}
if (-not $env:PGPASSWORD) {
    $securePwd = Read-Host "Password for user $User" -AsSecureString
    $bstr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePwd)
    $env:PGPASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringAuto($bstr)
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr)
}

$files = @(
    '00-prep.sql','10-organization.sql','20-users.sql','30-patient.sql',
    '40-form058.sql','45-form058-1.sql',
    '50-card.sql','51-card161.sql','52-card174.sql','53-card175.sql','54-card205.sql',
    '55-card-tube.sql','60-act.sql','61-act-subtypes.sql','90-finalize.sql'
)

Set-Location $PSScriptRoot
Write-Host "WARNING: 00-prep TRUNCATEs public2 business tables (CASCADE also hits"
Write-Host "         rp_*, form_129, user_roles, outbound_webhook_dispatch). Backup ready?"
$ans = Read-Host "Continue? [yes/NO]"
if ($ans -ne 'yes') { Write-Host 'aborted'; exit 1 }

foreach ($f in $files) {
    Write-Host "=== $f ==="
    & $Psql -h $DbHost -p $Port -U $User -d $Db -v ON_ERROR_STOP=1 -X -q -f $f 2>&1 | ForEach-Object { "$_" }
    if ($LASTEXITCODE -ne 0) {
        Write-Host ">>> $f FAILED (psql exit $LASTEXITCODE) - STOPPED"
        exit 1
    }
}
Write-Host "DONE. Check: SELECT * FROM public2._migration_notes;  and the 90-finalize report."
