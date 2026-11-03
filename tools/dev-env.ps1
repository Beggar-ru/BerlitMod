# Shell setup for this checkout. Two ways to use it:
#
#   1) Run a task directly:
#        .\tools\dev-env.ps1 build
#        .\tools\dev-env.ps1 runClient
#        .\tools\dev-env.ps1 preview check
#
#   2) Load it into the session (note the leading dot and space):
#        . .\tools\dev-env.ps1
#        gw build
#        pv check
#
# It points JAVA_HOME at the workspace-local JDK 25, GRADLE_USER_HOME at
# .gradle-home and TMP/TEMP at .gradle-home\tmp, since neither %USERPROFILE%\.gradle
# nor the system %TEMP% is usable for this checkout.

param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]] $Task
)

$root = Split-Path -Parent $PSScriptRoot

$jdk = Join-Path $root '.tools\jdk-25'
if (Test-Path (Join-Path $jdk 'bin\java.exe')) {
    $env:JAVA_HOME = $jdk
    $env:PATH = "$jdk\bin;$env:PATH"
} else {
    Write-Warning "JDK 25 not found in .tools\jdk-25 - run: python .tools\fetch_jdk25.py"
}

$gradleHome = Join-Path $root '.gradle-home'
if (-not (Test-Path $gradleHome)) { New-Item -ItemType Directory -Force -Path $gradleHome | Out-Null }
$env:GRADLE_USER_HOME = $gradleHome

# Spotless/blowdryer cannot write its cache into the system %TEMP% here, so both
# the shell and the Gradle JVM (see .gradle-home\gradle.properties) use a
# workspace temp directory.
$temp = Join-Path $gradleHome 'tmp'
if (-not (Test-Path $temp)) { New-Item -ItemType Directory -Force -Path $temp | Out-Null }
$env:TMP = $temp
$env:TEMP = $temp

# Python for the texture/GUI tools: recorded by the setup step, else PATH.
$pythonFile = Join-Path $root '.tools\python.txt'
$python = if (Test-Path $pythonFile) { (Get-Content $pythonFile -Raw).Trim() } else { 'python' }

$gwPath = Join-Path $root 'gradlew.bat'
$pvPath = Join-Path $root 'tools\preview.py'

# Native file watching and native integration fail under the sandbox with
# "Couldn't open current thread, error = 5". Both are switched off in
# .gradle-home/gradle.properties; --no-watch-fs is the belt-and-braces flag.
function Invoke-BerlitGradle {
    & $gwPath --no-watch-fs @args
}

function Invoke-BerlitPreview {
    & $python $pvPath @args
}

# Arguments were passed, so this file was run rather than dot-sourced: execute.
# The command output must go straight to the pipeline - wrapping it in exit (...)
# would capture and discard it - so exit with $LASTEXITCODE afterwards.
if ($Task.Count -gt 0) {
    if ($Task[0] -in @('preview', 'pv')) {
        & $python $pvPath @($Task | Select-Object -Skip 1)
    } else {
        & $gwPath --no-watch-fs @Task
    }
    exit $LASTEXITCODE
}

# Run with no arguments means the user expected something to happen; the helpers
# only survive dot-sourcing, so say that instead of silently defining and leaving.
if ($MyInvocation.InvocationName -ne '.') {
    Write-Host 'Usage:'
    Write-Host '  .\tools\dev-env.ps1 build            # run a Gradle task'
    Write-Host '  .\tools\dev-env.ps1 runClient'
    Write-Host '  .\tools\dev-env.ps1 preview check    # run a preview.py command'
    Write-Host ''
    Write-Host 'Or load the environment into this session (note the leading dot):'
    Write-Host '  . .\tools\dev-env.ps1'
    Write-Host '  gw build / gw runClient / pv check'
    exit 0
}

# Dot-sourced: publish the helpers into the caller's session. The paths are baked
# into the bodies so the functions keep working after this script has returned.
Set-Item -Path Function:global:gw -Value ([scriptblock]::Create("& '$gwPath' --no-watch-fs @args"))
Set-Item -Path Function:global:pv -Value ([scriptblock]::Create("& '$python' '$pvPath' @args"))

Write-Host "JAVA_HOME        = $env:JAVA_HOME"
Write-Host "GRADLE_USER_HOME = $env:GRADLE_USER_HOME"
Write-Host "TMP              = $env:TMP"
Write-Host "python           = $python"
Write-Host "Ready: 'gw build', 'gw runClient', 'pv check', 'pv sheet'."
