#Requires -Version 5.1
<# .SYNOPSIS
Build the three default release JARs and publish them to CurseForge.
.PARAMETER Yes
Skip interactive confirmation for unattended use.
.PARAMETER JavaHome
Optional JDK 25 directory. Otherwise use an installed JDK 25 from the Gradle cache.
#>
[CmdletBinding()]
param([switch]$Yes, [string]$JavaHome)
. (Join-Path $PSScriptRoot 'publish-common.ps1')
$context = Get-PublishContext
Assert-PublishConfiguration $context
$selectedJava = Select-PublishJava $context $JavaHome
$previousJava = $env:JAVA_HOME
Push-Location $context.Repo
try {
    $env:JAVA_HOME = $selectedJava
    # Prepare the actual release files before requesting confirmation.
    Invoke-PublishGradle $context @('build','--console=plain')
    Write-Host "== Publish $($context.Name) $($context.Version) to CurseForge project $($context.ProjectId) ==" -ForegroundColor Yellow
    Write-Host "Release type: $($context.ReleaseType); client and server"
    Show-PublishFiles $context
    if (-not $Yes) {
        $answer = Read-Host 'Upload these files now? [y/N]'
        if ($answer -notmatch '^(y|yes)$') { Write-Host 'Cancelled. Nothing was uploaded.'; return }
    }
    Invoke-PublishGradle $context @('publishAllVersions','--console=plain')
    Write-Host 'All three versions published successfully.' -ForegroundColor Green
} finally { $env:JAVA_HOME = $previousJava; Pop-Location }
