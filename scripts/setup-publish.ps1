#Requires -Version 5.1
<# .SYNOPSIS
Check the local Java and credential environment. Optionally save the public CurseForge project ID.
This script never asks for, writes, or prints API token values.
#>
[CmdletBinding()]
param([ValidatePattern('^[1-9][0-9]*$')][string]$ProjectId, [string]$JavaHome)
. (Join-Path $PSScriptRoot 'publish-common.ps1')
$context = Get-PublishContext
if ($ProjectId) {
    $path = Join-Path $context.Repo 'gradle.properties'
    $text = [IO.File]::ReadAllText($path)
    $text = [regex]::Replace($text, '(?m)^curseforge\.projectId=.*$', 'curseforge.projectId=' + $ProjectId)
    [IO.File]::WriteAllText($path, $text, (New-Object Text.UTF8Encoding($false)))
    $context = Get-PublishContext
}
$selectedJava = Select-PublishJava $context $JavaHome
Assert-PublishConfiguration $context -DryRun
Write-Host "JDK 25: $selectedJava" -ForegroundColor Green
Write-Host "Gradle daemon: Java 25; Minecraft toolchains: 17 / 21 / 25."
if ($context.TokenConfigured) { Write-Host 'CurseForge token: configured (value hidden).' -ForegroundColor Green }
else { Write-Host 'CurseForge token: missing. Set CURSEFORGE_TOKEN or user-level curseforge.token.' -ForegroundColor Yellow }
if ($context.ProjectId) { Write-Host "CurseForge project ID: $($context.ProjectId)" }
else { Write-Host 'CurseForge project ID: pending. Later run setup-publish.ps1 -ProjectId <numeric ID>.' -ForegroundColor Yellow }
Write-Host 'Next: .\scripts\dryrun.ps1'
