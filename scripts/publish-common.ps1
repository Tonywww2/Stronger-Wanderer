#Requires -Version 5.1
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Get-PublishProperty([string]$Path, [string]$Name) {
    if (-not (Test-Path -LiteralPath $Path)) { return '' }
    $pattern = '^\s*' + [regex]::Escape($Name) + '\s*=(.*)$'
    foreach ($line in [IO.File]::ReadAllLines($Path)) {
        if ($line -match $pattern) { return $Matches[1].Trim() }
    }
    return ''
}

function Get-PublishContext {
    $repo = Split-Path -Parent $PSScriptRoot
    $properties = Join-Path $repo 'gradle.properties'
    $gradleHome = if ($env:GRADLE_USER_HOME) { $env:GRADLE_USER_HOME } else { Join-Path $env:USERPROFILE '.gradle' }
    $version = Get-PublishProperty $properties 'mod.version'
    $modId = Get-PublishProperty $properties 'mod.id'
    if (-not $version -or -not $modId) { throw 'Missing mod.version or mod.id in gradle.properties.' }
    $nodes = @(foreach ($dir in Get-ChildItem -LiteralPath (Join-Path $repo 'versions') -Directory | Sort-Object Name) {
        if ($dir.Name -notmatch '^(.+)-(forge|neoforge)$') { continue }
        $mc = $Matches[1]; $loader = $Matches[2]
        [pscustomobject]@{
            Node = $dir.Name; Minecraft = $mc; Loader = $loader
            File = Join-Path $dir.FullName "build/libs/$modId-$loader-$version+$mc.jar"
        }
    })
    if ($nodes.Count -eq 0) { throw 'No supported version nodes found.' }
    $tokenSet = -not [string]::IsNullOrWhiteSpace($env:CURSEFORGE_TOKEN)
    if (-not $tokenSet) {
        $tokenSet = -not [string]::IsNullOrWhiteSpace((Get-PublishProperty (Join-Path $gradleHome 'gradle.properties') 'curseforge.token'))
    }
    [pscustomobject]@{
        Repo = $repo; GradleHome = $gradleHome; Nodes = $nodes; Version = $version
        Name = Get-PublishProperty $properties 'mod.name'
        ProjectId = Get-PublishProperty $properties 'curseforge.projectId'
        ReleaseType = Get-PublishProperty $properties 'curseforge.releaseType'
        TokenConfigured = $tokenSet
    }
}

function Select-PublishJava($Context, [string]$JavaHome) {
    $candidates = @()
    if ($JavaHome) { $candidates = @($JavaHome) }
    else {
        if ($env:JAVA_HOME) { $candidates += $env:JAVA_HOME }
        $cache = Join-Path $Context.GradleHome 'jdks'
        if (Test-Path -LiteralPath $cache) {
            $candidates += @(Get-ChildItem -LiteralPath $cache -Directory | Sort-Object Name | Select-Object -ExpandProperty FullName)
        }
        $javaCommand = Get-Command java.exe -ErrorAction SilentlyContinue
        if ($javaCommand) { $candidates += Split-Path -Parent (Split-Path -Parent $javaCommand.Source) }
    }
    foreach ($candidate in $candidates) {
        $java = Join-Path $candidate 'bin/java.exe'
        $release = Join-Path $candidate 'release'
        if ((Test-Path -LiteralPath $java) -and (Test-Path -LiteralPath $release)) {
            $text = [IO.File]::ReadAllText($release)
            if ($text -match '(?m)^JAVA_VERSION="25(?:[.+"-])') { return (Resolve-Path -LiteralPath $candidate).Path }
        }
    }
    throw 'JDK 25 not found. Install JDK 25 or pass -JavaHome <JDK25 directory>.'
}

function Assert-PublishConfiguration($Context, [switch]$DryRun) {
    if ($Context.ReleaseType -notin @('release','beta','alpha')) { throw 'curseforge.releaseType must be release, beta or alpha.' }
    if (-not $DryRun) {
        if ($Context.ProjectId -notmatch '^[1-9][0-9]*$') { throw 'Set the numeric curseforge.projectId in gradle.properties before publishing.' }
        if (-not $Context.TokenConfigured) { throw 'Set CURSEFORGE_TOKEN or curseforge.token in user-level Gradle properties.' }
    }
    $changelog = Join-Path $Context.Repo 'CHANGELOG.md'
    if ([string]::IsNullOrWhiteSpace($env:CHANGELOG) -and (-not (Test-Path -LiteralPath $changelog) -or [string]::IsNullOrWhiteSpace([IO.File]::ReadAllText($changelog)))) {
        throw 'Provide nonempty CHANGELOG.md or the CHANGELOG environment variable.'
    }
}

function Invoke-PublishGradle($Context, [string[]]$GradleArgs) {
    & (Join-Path $Context.Repo 'gradlew.bat') @GradleArgs
    $result = $LASTEXITCODE
    if ($result -ne 0) { throw "Gradle failed with exit code $result." }
}

function Show-PublishFiles($Context) {
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    foreach ($node in $Context.Nodes) {
        if (-not (Test-Path -LiteralPath $node.File)) { throw "Missing release JAR: $($node.File)" }
        $zip = [IO.Compression.ZipFile]::OpenRead($node.File)
        try {
            if (@($zip.Entries | Where-Object { $_.FullName -match 'SkillGameTests|/arena\.nbt$' }).Count -gt 0) {
                throw "GameTest files found in $($node.File). Rebuild without -PswGameTest."
            }
            $metadata = if ($node.Loader -eq 'forge') { 'META-INF/mods.toml' } else { 'META-INF/neoforge.mods.toml' }
            if (-not $zip.GetEntry($metadata)) { throw "Missing $metadata in $($node.File)." }
        } finally { $zip.Dispose() }
        $item = Get-Item -LiteralPath $node.File
        Write-Host "  $($node.Node): $($item.Name) ($($item.Length) bytes)"
    }
}
