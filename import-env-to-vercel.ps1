$ErrorActionPreference = "Stop"

Write-Host "=== IMPORTADOR DE VARIABLES .ENV A VERCEL ===" -ForegroundColor Cyan
Write-Host "Este script configurara tus credenciales en Vercel de forma automatica y redesplegara tu backend."

# 1. Login to Vercel (This will open a browser window to authenticate)
Write-Host "`n[1/4] Iniciando sesion en Vercel..." -ForegroundColor Yellow
& npx --package vercel vercel login

# 2. Link the local folder to your Vercel project
Write-Host "`n[2/4] Vinculando el proyecto local con Vercel..." -ForegroundColor Yellow
Write-Host "Responde las preguntas presionando ENTER para usar los valores por defecto:"
& npx --package vercel vercel link

# 3. Import the .env file to Vercel environment variables
Write-Host "`n[3/4] Subiendo las variables del archivo .env a Vercel..." -ForegroundColor Yellow
if (Test-Path ".env") {
    & npx --package vercel vercel env import ".env"
} elseif (Test-Path "..\.env") {
    & npx --package vercel vercel env import "..\.env"
} else {
    Write-Error "No se encontro el archivo .env en la raiz del proyecto."
}

# 4. Redeploy the production serverless backend
Write-Host "`n[4/4] Redesplegando el servidor en produccion con las nuevas variables..." -ForegroundColor Yellow
& npx --package vercel vercel deploy --prod

Write-Host "`n=== PROCESO COMPLETADO CON EXITO ===" -ForegroundColor Green
Write-Host "Tu servidor en https://app-de-prueba-redimain.vercel.app/ ahora debe mostrar 'Servidor Activo'."
