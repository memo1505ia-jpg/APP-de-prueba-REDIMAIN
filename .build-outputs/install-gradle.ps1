$ErrorActionPreference = "Stop"
$gradleZip = Join-Path $env:TEMP "gradle-8.5-bin.zip"
$extractDir = Join-Path "C:\Users\guillermo matute\Downloads\redimain-control" ".gradle-dist"

if (-not (Test-Path $extractDir)) {
    New-Item -ItemType Directory -Path $extractDir | Out-Null
}

Write-Host "Downloading Gradle 8.5..."
Invoke-WebRequest -Uri "https://services.gradle.org/distributions/gradle-8.5-bin.zip" -OutFile $gradleZip

Write-Host "Extracting Gradle 8.5..."
Expand-Archive -Path $gradleZip -DestinationPath $extractDir -Force

$gradleBat = Join-Path $extractDir "gradle-8.5\bin\gradle.bat"
if (Test-Path $gradleBat) {
    Write-Host "Gradle extracted successfully. Generating wrapper..."
    # Generate Gradle wrapper in the project directory
    & $gradleBat wrapper
    Write-Host "Wrapper generated successfully!"
} else {
    Write-Error "gradle.bat not found at $gradleBat"
}
