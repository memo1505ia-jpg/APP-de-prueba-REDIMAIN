param(
    [switch]$Simulate,
    [string]$SearchPath = "C:\Users\guillermo matute",
    [string]$DestinationPath = "C:\Users\guillermo matute\Desktop\PDF_Organizados"
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

Write-Host "--- CLASIFICADOR INTELIGENTE DE ARCHIVOS PDF ---" -ForegroundColor Cyan
Write-Host "Ruta de busqueda: $SearchPath"
Write-Host "Ruta de destino: $DestinationPath"
if ($Simulate) {
    Write-Host "MODO SIMULACION ACTIVO (No se movera ningun archivo)" -ForegroundColor Yellow
}
Write-Host "--------------------------------------------------"

# Listas de palabras clave para clasificacion (case-insensitive)
$WorkKeywords = @(
    "redimain", "redi", "fanb", "zodi", "ceo-mc", "urra", "militar",
    "combate", "sifontes", "roraima", "oficio", "patria", "defensa", 
    "operacion", "armada", "ejercito", "aviacion", "conas", "nacional",
    "milicia", "comandante", "ayudantia", "ayudante", "reglamento", "resolucion",
    "instructivo", "radiograma", "directiva", "concepto", "manual", "tuncap",
    "tunes", "cne", "combustible", "mision", "seguridad", "nacion", "geopolitica",
    "geoestrategia", "soberania", "decreto", "oddh", "oddi", "armas", "armamento",
    "tactica", "bajas", "captacion", "oficial", "acta", "agenda", "imputado",
    "fiscal", "penal", "intervencion", "ruta", "pabastos", "plan", "evidencia",
    "memo", "bolivar", "chavez", "zamora", "ley", "orden", "constitucion", "rdgma",
    "cgejb", "lucha", "batalla", "guerra", "ascenso", "alm", "cnel", "tf", "tn",
    "tcnel", "mg", "va", "cf"
)

$PersonalKeywords = @(
    "solvencia", "banco", "credito", "personal", "cedula", "curriculum",
    "factura", "pago", "recibo", "novia", "estudios", "curriculo", "martha",
    "identidad", "viaje", "cuenta", "mercantil", "banesco", "solicitud",
    "comprobante", "madrina", "novio", "constancia", "rif", "declaracion",
    "certificado", "claseflix", "atajos", "photoshop", "boleta", "permiso",
    "ingreso", "egreso", "referencia", "seniat", "transferencia", "presupuesto",
    "planificador", "marca", "recaudos", "help", "script", "presets", "ae",
    "earnings", "report", "presentation", "horkheimer", "adorno", "covid"
)

# Crear estructura de carpetas de destino si no es simulacion
$Subdirs = @{
    "REDIMAIN" = Join-Path $DestinationPath "REDIMAIN"
    "Personales" = Join-Path $DestinationPath "Personales"
    "Otros" = Join-Path $DestinationPath "Otros_Por_Clasificar"
}

if (-not $Simulate -and -not (Test-Path -Path $DestinationPath)) {
    New-Item -ItemType Directory -Path $DestinationPath -Force | Out-Null
    foreach ($sub in $Subdirs.Values) {
        New-Item -ItemType Directory -Path $sub -Force | Out-Null
    }
    Write-Host "Estructura de carpetas de destino creada con exito." -ForegroundColor Green
}

Write-Host "Buscando archivos PDF..." -ForegroundColor Cyan
$allFiles = Get-ChildItem -Path $SearchPath -Filter *.pdf -Recurse -File -ErrorAction SilentlyContinue

# Filtrar exclusiones y la propia carpeta de destino
$filteredFiles = $allFiles | Where-Object {
    $filePath = $_.FullName
    $exclude = $false
    
    if ($filePath.StartsWith($DestinationPath, [System.StringComparison]::OrdinalIgnoreCase)) {
        $exclude = $true
    }
    
    foreach ($excludeDir in $ExcludePaths) {
        if ($filePath -like "*\$excludeDir\*") {
            $exclude = $true
            break
        }
    }
    
    -not $exclude
}

$totalCount = $filteredFiles.Count
Write-Host "Se encontraron $totalCount archivos PDF en total." -ForegroundColor Green

if ($totalCount -eq 0) {
    Write-Host "No se encontraron archivos PDF." -ForegroundColor Yellow
    exit
}

# Lista para almacenar clasificaciones
$classifiedList = @()
$ambiguousFiles = @()

Write-Host "Fase 1: Clasificacion rapida por Ruta y Nombre..." -ForegroundColor Cyan
$i = 1
foreach ($file in $filteredFiles) {
    $category = "Otros"
    $reason = "Ambiguo"
    
    $pathLower = $file.FullName.ToLower()
    $nameLower = $file.Name.ToLower()
    
    # Reglas rapidas de ruta (Ej: carpetas militares conocidas)
    if ($pathLower.Contains("\urras\") -or $pathLower.Contains("\redi\") -or $pathLower.Contains("manuales") -or $pathLower.Contains("fortaleza") -or $pathLower.Contains("zodidoc") -or $pathLower.Contains("redimain-control")) {
        $category = "REDIMAIN"
        $reason = "Ruta militar/trabajo"
    } elseif ($pathLower.Contains("\personal\") -or $pathLower.Contains("\novia\") -or $pathLower.Contains("\fotos personales\")) {
        $category = "Personales"
        $reason = "Ruta personal"
    } else {
        # Reglas rapidas de nombre de archivo
        $workScore = 0
        $personalScore = 0
        
        foreach ($kw in $WorkKeywords) {
            if ($nameLower -like "*$kw*") { $workScore++ }
        }
        foreach ($kw in $PersonalKeywords) {
            if ($nameLower -like "*$kw*") { $personalScore++ }
        }
        
        if ($workScore -gt $personalScore) {
            $category = "REDIMAIN"
            $reason = "Nombre de archivo (Trabajo)"
        } elseif ($personalScore -gt $workScore) {
            $category = "Personales"
            $reason = "Nombre de archivo (Personal)"
        }
    }
    
    if ($category -ne "Otros") {
        $classifiedList += [PSCustomObject]@{
            File = $file
            Category = $category
            Reason = $reason
            LastWriteTime = $file.LastWriteTime
        }
    } else {
        $ambiguousFiles += $file
    }
    $i++
}

Write-Host ("Fase 1 completada. Clasificados: {0} | Ambiguos: {1}" -f $classifiedList.Count, $ambiguousFiles.Count) -ForegroundColor Green

# Fase 2: Analizar archivos ambiguos leyendo el texto de forma segura con timeout
if ($ambiguousFiles.Count -gt 0) {
    Write-Host "Fase 2: Analizando archivos ambiguos leyendo el contenido..." -ForegroundColor Cyan
    
    # Inicializar Word de forma local
    $word = $null
    try {
        $word = New-Object -ComObject Word.Application
        $word.Visible = $false
        $word.DisplayAlerts = 0
    } catch {
        Write-Host "Word no disponible para lectura profunda. Se clasificaran como Otros." -ForegroundColor Yellow
    }
    
    $idx = 1
    $totalAmb = $ambiguousFiles.Count
    
    foreach ($file in $ambiguousFiles) {
        $category = "Otros"
        $reason = "Contenido no coincide con palabras clave"
        
        # Limitar analisis a archivos de menos de 5MB para evitar bloqueos
        if ($word -and $file.Length -lt 5000000) {
            Write-Host ("  [{0}/{1}] Leyendo contenido: {2} ({3} KB)" -f $idx, $totalAmb, $file.Name, [Math]::Round($file.Length/1024))
            
            # Crear un trabajo de PowerShell para abrir el documento con un timeout de 4 segundos
            $job = Start-Job -ScriptBlock {
                param($pdfPath, $workKWs, $personalKWs)
                try {
                    $w = New-Object -ComObject Word.Application
                    $w.Visible = $false
                    $w.DisplayAlerts = 0
                    
                    $d = $w.Documents.Open($pdfPath, $false, $true, $false)
                    $txt = $d.Content.Text
                    $d.Close($false)
                    $w.Quit()
                    
                    # Contar coincidencias en el subproceso
                    $wScore = 0
                    $pScore = 0
                    
                    foreach ($kw in $workKWs) {
                        $matches = [regex]::Matches($txt, "\b$kw\b", "IgnoreCase")
                        $wScore += $matches.Count
                    }
                    foreach ($kw in $personalKWs) {
                        $matches = [regex]::Matches($txt, "\b$kw\b", "IgnoreCase")
                        $pScore += $matches.Count
                    }
                    
                    return "$wScore|$pScore"
                } catch {
                    if ($w) { try { $w.Quit() } catch {} }
                    return "ERROR"
                }
            } -ArgumentList $file.FullName, $WorkKeywords, $PersonalKeywords
            
            # Esperar al subproceso maximo 4 segundos
            $jobResult = Wait-Job $job -Timeout 4
            
            if ($jobResult -eq $null) {
                # Se supero el tiempo limite (hang en Word)
                Remove-Job $job -Force
                # Forzar el cierre de cualquier proceso winword colgado
                Get-Process -Name winword -ErrorAction SilentlyContinue | Stop-Process -Force
                # Re-inicializar la instancia principal de Word si es necesario
                try {
                    $word = New-Object -ComObject Word.Application
                    $word.Visible = $false
                    $word.DisplayAlerts = 0
                } catch {}
                
                $reason = "Lectura cancelada por tiempo limite (PDF muy complejo)"
            } else {
                $scoreStr = Receive-Job $job
                Remove-Job $job
                
                if ($scoreStr -and $scoreStr -ne "ERROR") {
                    $parts = $scoreStr.Split("|")
                    $wScore = [int]$parts[0]
                    $pScore = [int]$parts[1]
                    
                    if ($wScore -gt $pScore) {
                        $category = "REDIMAIN"
                        $reason = "Contenido (Trabajo: {0}, Personal: {1})" -f $wScore, $pScore
                    } elseif ($pScore -gt $wScore) {
                        $category = "Personales"
                        $reason = "Contenido (Trabajo: {0}, Personal: {1})" -f $wScore, $pScore
                    }
                }
            }
        } else {
            if ($file.Length -ge 5000000) {
                $reason = "Archivo demasiado grande para lectura dinamica (>= 5MB)"
            }
        }
        
        $classifiedList += [PSCustomObject]@{
            File = $file
            Category = $category
            Reason = $reason
            LastWriteTime = $file.LastWriteTime
        }
        
        $idx++
    }
    
    # Limpiar Word al terminar
    if ($word) {
        $word.Quit()
        # Matar procesos huerfanos por si acaso
        Get-Process -Name winword -ErrorAction SilentlyContinue | Stop-Process -Force
    }
}

# Procesar y mover archivos por categoria
Write-Host "Moviendo y renombrando archivos clasificados..." -ForegroundColor Cyan

$logEntries = @()
$successCount = 0
$errorCount = 0

# Agrupar clasificados
$groups = $classifiedList | Group-Object Category

foreach ($group in $groups) {
    $catName = $group.Name
    $catDir = $Subdirs[$catName]
    
    # Ordenar por fecha descendente (mas nuevo primero)
    $catFiles = $group.Group | Sort-Object LastWriteTime -Descending
    $catTotal = $catFiles.Count
    
    # Padding de indices dinamico
    $padding = [Math]::Max(3, $catTotal.ToString().Length)
    
    Write-Host "Categoria: $catName ($catTotal archivos)" -ForegroundColor Green
    
    $idx = 1
    foreach ($item in $catFiles) {
        $file = $item.File
        $idxStr = "{0:D$padding}" -f $idx
        $dateStr = $file.LastWriteTime.ToString("yyyyMMdd_HHmmss")
        $newName = "{0}_{1}_{2}" -f $idxStr, $dateStr, $file.Name
        $destinationFile = Join-Path $catDir $newName
        
        $fileDate = $file.LastWriteTime.ToString("yyyy-MM-dd HH:mm:ss")
        $logLine = "{0} - {1} - Categoria: {2} - Razon: {3} - Original: {4} -> Nuevo: {5}" -f $idxStr, $fileDate, $catName, $item.Reason, $file.FullName, $newName
        $logEntries += $logLine
        
        if (-not $Simulate) {
            try {
                Move-Item -Path $file.FullName -Destination $destinationFile -Force -ErrorAction Stop
                $successCount++
            } catch {
                Write-Host ("  ERROR al mover {0} : {1}" -f $file.Name, $_.Exception.Message) -ForegroundColor Red
                $errorCount++
            }
        } else {
            $successCount++
        }
        
        $idx++
    }
}

# Escribir archivo de registro
if (-not $Simulate) {
    $logFilePath = Join-Path $DestinationPath "registro_origen.txt"
    $header = @(
        "==================================================",
        "REGISTRO DE CLASIFICACION Y ORGANIZACION DE PDFs",
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
