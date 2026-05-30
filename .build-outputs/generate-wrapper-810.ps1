$ErrorActionPreference = "Stop"
$gradleZip = Join-Path $env:TEMP "gradle-8.10-bin.zip"
$extractDir = Join-Path "C:\Users\guillermo matute\Downloads\redimain-control" ".gradle-dist-810"

if (-not (Test-Path $extractDir)) {
    New-Item -ItemType Directory -Path $extractDir | Out-Null
}

Write-Host "Downloading Gradle 8.10..."
$ProgressPreference = "SilentlyContinue"
Invoke-WebRequest -Uri "https://services.gradle.org/distributions/gradle-8.10-bin.zip" -OutFile $gradleZip

Write-Host "Extracting Gradle 8.10..."
Expand-Archive -Path $gradleZip -DestinationPath $extractDir -Force

$gradleBat = Join-Path $extractDir "gradle-8.10\bin\gradle.bat"
if (Test-Path $gradleBat) {
    Write-Host "Gradle extracted successfully. Generating wrapper..."
    $env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot"
    $env:PATH = "C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot\bin;" + $env:PATH
    & $gradleBat wrapper
    Write-Host "Wrapper generated successfully!"
} else {
    Write-Error "gradle.bat not found at $gradleBat"
}
