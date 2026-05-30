$ErrorActionPreference = "Stop"

# 1. Decode debug keystore
$base64Path = "C:\Users\guillermo matute\Downloads\redimain-control\debug.keystore.base64"
$keystorePath = "C:\Users\guillermo matute\Downloads\redimain-control\debug.keystore"

if (Test-Path $base64Path) {
    Write-Host "Decoding debug keystore..."
    $base64Text = Get-Content $base64Path -Raw
    $bytes = [System.Convert]::FromBase64String($base64Text.Trim())
    [System.IO.File]::WriteAllBytes($keystorePath, $bytes)
    Write-Host "Keystore decoded successfully."
} else {
    Write-Warning "debug.keystore.base64 not found!"
}

# 2. Configure paths
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot"
$env:PATH = "C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot\bin;" + $env:PATH
$env:ANDROID_HOME = "C:\Users\guillermo matute\AppData\Local\Android\Sdk"

# 3. Build APK
Write-Host "Building project with Gradle..."
& cmd.exe /c "gradlew.bat assembleDebug"

# 4. Copy build output
$builtApk = "C:\Users\guillermo matute\Downloads\redimain-control\app\build\outputs\apk\debug\app-debug.apk"
$destApk = "C:\Users\guillermo matute\Downloads\redimain-control\.build-outputs\REDIMAIN PRUEBA 1.1.apk"

if (Test-Path $builtApk) {
    Write-Host "Build succeeded! Copying APK to destination..."
    Copy-Item -Path $builtApk -Destination $destApk -Force
    Write-Host "APK created and copied to: $destApk"
} else {
    Write-Error "Build output APK not found at $builtApk"
}
