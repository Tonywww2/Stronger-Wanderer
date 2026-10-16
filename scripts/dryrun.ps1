#Requires -Version 5.1
<# .SYNOPSIS
Build all versions and exercise CurseForge publishing without uploading.
The project ID and token may remain unset for this local dry run.
#>
[CmdletBinding()]
param([string]$JavaHome)
. (Join-Path $PSScriptRoot 'publish-common.ps1')
$context = Get-PublishContext
Assert-PublishConfiguration $context -DryRun
$selectedJava = Select-PublishJava $context $JavaHome
$previousJava = $env:JAVA_HOME
Push-Location $context.Repo
try {
    $env:JAVA_HOME = $selectedJava
    Write-Host "== $($context.Name) $($context.Version): dry run, no uploads ==" -ForegroundColor Cyan
    if (-not $context.ProjectId) { Write-Host 'CurseForge project ID is pending; metadata will be validated locally.' -ForegroundColor Yellow }
    Write-Host "JDK: $selectedJava"
    Invoke-PublishGradle $context @('build','--console=plain')
    Show-PublishFiles $context
    Invoke-PublishGradle $context @('publishAllVersions','-Ppublish.dryRun=true','--console=plain')
    Write-Host 'Dry run passed. Nothing was uploaded.' -ForegroundColor Green
} finally { $env:JAVA_HOME = $previousJava; Pop-Location }
