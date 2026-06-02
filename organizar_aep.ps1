param(
    [switch]$Simulate,
    [string]$SearchPath = "C:\Users\guillermo matute",
    [string]$DestinationPath = "C:\Users\guillermo matute\Desktop\AEP_Organizados"
)

# Definir carpetas a excluir
$ExcludePaths = @(
    "AppData",
    ".gemini",
    "node_modules",
    "Local Settings",
    "Application Data"
)

# Normalizar rutas
$SearchPath = [System.IO.Path]::GetFullPath($SearchPath)
$DestinationPath = [System.IO.Path]::GetFullPath($DestinationPath)

Write-Host "--- ORGANIZADOR DE ARCHIVOS AEP ---" -ForegroundColor Cyan
Write-Host "Ruta de busqueda: $SearchPath"
Write-Host "Ruta de destino: $DestinationPath"
if ($Simulate) {
    Write-Host "MODO SIMULACION ACTIVO (No se movera ningun archivo)" -ForegroundColor Yellow
}
Write-Host "------------------------------------"

# Crear carpeta de destino si no existe
if (-not $Simulate -and -not (Test-Path -Path $DestinationPath)) {
    New-Item -ItemType Directory -Path $DestinationPath -Force | Out-Null
    Write-Host "Carpeta de destino creada con exito." -ForegroundColor Green
}

Write-Host "Buscando archivos AEP..." -ForegroundColor Cyan
# Obtener todos los archivos AEP de forma recursiva
$allFiles = Get-ChildItem -Path $SearchPath -Filter *.aep -Recurse -File -ErrorAction SilentlyContinue

# Filtrar exclusiones y la propia carpeta de destino
$filteredFiles = $allFiles | Where-Object {
    $filePath = $_.FullName
    $exclude = $false
    
    # Excluir si esta dentro de la carpeta de destino
    if ($filePath.StartsWith($DestinationPath, [System.StringComparison]::OrdinalIgnoreCase)) {
        $exclude = $true
    }
    
    # Excluir carpetas del sistema/ocultas configuradas
    foreach ($excludeDir in $ExcludePaths) {
        if ($filePath -like "*\$excludeDir\*") {
            $exclude = $true
            break
        }
    }
    
    -not $exclude
}

# Ordenar de mas nuevo a mas antiguo
$sortedFiles = $filteredFiles | Sort-Object LastWriteTime -Descending

$totalCount = $sortedFiles.Count
Write-Host "Se encontraron $totalCount archivos AEP." -ForegroundColor Green

if ($totalCount -eq 0) {
    Write-Host "No se encontraron archivos AEP para organizar." -ForegroundColor Yellow
    exit
}

# Determinar longitud del padding para los indices
$paddingLength = [Math]::Max(3, $totalCount.ToString().Length)

# Coleccion para el registro
$logEntries = @()
$successCount = 0
$errorCount = 0

$index = 1
foreach ($file in $sortedFiles) {
    # Formatear el indice
    $indexString = "{0:D$paddingLength}" -f $index
    
    # Obtener fecha de modificacion formateada
    $dateString = $file.LastWriteTime.ToString("yyyyMMdd_HHmmss")
    $displayDate = $file.LastWriteTime.ToString("yyyy-MM-dd")
    $fileDate = $file.LastWriteTime.ToString("yyyy-MM-dd HH:mm:ss")
    
    # Formar el nuevo nombre de archivo
    $fileName = $file.Name
    $newName = "{0}_{1}_{2}" -f $indexString, $dateString, $fileName
    $destinationFile = Join-Path $DestinationPath $newName
    
    # Registrar la accion
    $origPath = $file.FullName
    $logLine = "{0} - {1} - Original: {2} -> Nuevo: $newName" -f $indexString, $fileDate, $origPath
    $logEntries += $logLine
    
    Write-Host ("[{0}/{1}] preparando: {2} ({3})" -f $indexString, $totalCount, $fileName, $displayDate)
    
    if (-not $Simulate) {
        try {
            # Mover archivo
            Move-Item -Path $origPath -Destination $destinationFile -Force -ErrorAction Stop
            $successCount++
        } catch {
            Write-Host ("  ERROR al mover {0} : {1}" -f $fileName, $_.Exception.Message) -ForegroundColor Red
            $errorCount++
        }
    } else {
        $successCount++
    }
    
    $index++
}

# Escribir archivo de registro
if (-not $Simulate) {
    $logFilePath = Join-Path $DestinationPath "registro_origen.txt"
    $header = @(
        "==================================================",
        "REGISTRO DE ORGANIZACION DE ARCHIVOS AEP",
        "Fecha de ejecucion: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')",
        "Total de archivos procesados: $totalCount",
        "Exitos: $successCount - Errores: $errorCount",
        "==================================================",
        ""
    )
    $fullLog = $header + $logEntries
    $fullLog | Out-File -FilePath $logFilePath -Encoding utf8
    Write-Host "Registro de origen creado en: $logFilePath" -ForegroundColor Green
    Write-Host "Proceso completado. Exitos: $successCount, Errores: $errorCount." -ForegroundColor Green
} else {
    Write-Host "Simulacion completada. $successCount archivos listados para mover." -ForegroundColor Yellow
}
