$ErrorActionPreference = "Stop"

$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
$JavaHome = if ($env:JAVA_HOME) { $env:JAVA_HOME } else { "C:\Program Files\Android\Android Studio\jbr" }
$env:JAVA_HOME = $JavaHome
$env:PATH = (Join-Path $JavaHome "bin") + ";" + $env:PATH

Push-Location $Root
try {
    $BuildFile = Get-Content -Raw "app\build.gradle"
    $VersionName = [regex]::Match($BuildFile, 'versionName\s+"([^"]+)"').Groups[1].Value
    if ([string]::IsNullOrWhiteSpace($VersionName)) {
        throw "Could not read versionName from app\build.gradle"
    }

    .\gradlew.bat assembleRelease
    if ($LASTEXITCODE -ne 0) {
        throw "Gradle build failed with exit code $LASTEXITCODE"
    }
    New-Item -ItemType Directory -Force -Path "dist" | Out-Null
    $ApkName = "FatLoss-v$VersionName.apk"
    $ApkPath = Join-Path "dist" $ApkName
    Copy-Item -Force "app\build\outputs\apk\release\app-release.apk" $ApkPath
    Write-Host "APK built: $Root\$ApkPath"
}
finally {
    Pop-Location
}
