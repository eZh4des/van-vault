<#
.SYNOPSIS
    Migra los productos que estan en la RAIZ de Firebase Realtime Database
    (ej. /CAMARA 2MP..., /SWITCH 5P...) hacia el nodo /inventory/ que
    la app espera.

.DESCRIPTION
    Problema: el import manual desde Firebase Console (importar JSON) en
    la RAIZ deja los productos sueltos en el nodo raiz, no en /inventory/.

    La app busca en /inventory/{nombre} y /searchIndex/{token}/{nombre}.
    Si los datos estan en la raiz, ninguna de las dos rutas los encuentra.

    Este script:
      1. Hace backup del nodo raiz
      2. Migra los productos sueltos (que tengan model/quantity/unitPrice) a /inventory/
      3. Borra la raiz (o solo las keys migradas, conservando otros nodos)
      4. NO migra datos fuera de la raiz (ej. /users/{uid} queda intacto)

.NOTES
    File Name      : migrate-firebase-data.ps1
    Prerequisite   : PowerShell 5+ con acceso publico a la REST API de RTDB
    Usage          : .\tools\migrate-firebase-data.ps1 -DryRun
#>

[CmdletBinding(SupportsShouldProcess = $true)]
param(
    [string]$DatabaseUrl = "https://van-vault-bf675-default-rtdb.firebaseio.com",
    [switch]$DryRun = $false,
    [switch]$Yes = $false
)

$ErrorActionPreference = "Stop"

# ============ Helpers ============

function Format-Json {
    param($obj, $depth = 10)
    if ($null -eq $obj) { return "null" }
    $json = $obj | ConvertTo-Json -Depth $depth -Compress
    return $json
}

function Invoke-Rtdb {
    param(
        [Parameter(Mandatory)][string]$Method,  # GET | PUT | PATCH | DELETE
        [Parameter(Mandatory)][string]$Path,    # ej. /inventory.json
        $Body = $null
    )
    $uri = "$DatabaseUrl$Path"
    $params = @{
        Uri = $uri
        Method = $Method
        UseBasicParsing = $true
    }
    if ($PSBoundParameters.ContainsKey('Body') -and $null -ne $Body) {
        $params['Body'] = $Body
        $params['ContentType'] = "application/json"
    }
    $response = Invoke-WebRequest @params
    if ($response.StatusCode -lt 200 -or $response.StatusCode -ge 300) {
        throw "RTDB call failed: $($response.StatusCode) $($response.Content)"
    }
    if ([string]::IsNullOrWhiteSpace($response.Content)) { return $null }
    return $response.Content | ConvertFrom-Json
}

# ============ 1. Backup ============

Write-Host "==> 1. Backup del nodo raiz" -ForegroundColor Cyan
$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$backupPath = "app\backups\firebase-root-$timestamp.json"
if (-not (Test-Path "app\backups")) { New-Item -ItemType Directory -Path "app\backups" | Out-Null }
$rootJson = (Invoke-WebRequest -Uri "$DatabaseUrl/.json" -UseBasicParsing).Content
$rootJson | Out-File -Encoding UTF8 $backupPath
Write-Host "   Backup guardado en: $backupPath" -ForegroundColor Green

# ============ 2. Detectar productos sueltos ============

Write-Host "`n==> 2. Detectando productos sueltos en la raiz" -ForegroundColor Cyan
$root = $rootJson | ConvertFrom-Json

if ($null -eq $root) {
    Write-Host "   No hay datos en la raiz. Nada que migrar." -ForegroundColor Yellow
    exit 0
}

$inventoryUpdates = @{}
$keysToDelete = @()
$migrated = 0
$skipped = 0

foreach ($prop in $root.PSObject.Properties) {
    $key = $prop.Name
    $value = $prop.Value

    # Solo procesar si es un objeto con campos de producto
    if ($value -is [PSCustomObject] -and
        ($value.PSObject.Properties.Name -contains "model" -or
         $value.PSObject.Properties.Name -contains "quantity" -or
         $value.PSObject.Properties.Name -contains "unitPrice")) {

        $inventoryUpdates["/inventory/$key.json"] = Format-Json $value
        $keysToDelete += "/$key.json"
        $migrated++
        Write-Host "   + $key" -ForegroundColor Green
    } else {
        $skipped++
        Write-Host "   - (no es producto) $key" -ForegroundColor DarkGray
    }
}

if ($migrated -eq 0) {
    Write-Host "`nNo se encontraron productos para migrar." -ForegroundColor Yellow
    exit 0
}

# ============ 3. Confirmar antes de escribir ============

Write-Host "`n==> Resumen:" -ForegroundColor Cyan
Write-Host "   Productos a migrar:   $migrated" -ForegroundColor White
Write-Host "   Keys a NO tocar:     $skipped" -ForegroundColor DarkGray
Write-Host "   Backup:              $backupPath" -ForegroundColor Green

if ($DryRun) {
    Write-Host "`n=== MODO DRY-RUN: no se hicieron cambios ===" -ForegroundColor Yellow
    exit 0
}

if (-not $Yes) {
    try {
        $confirmation = Read-Host "`nProceder con la migracion? (s/N)"
        if ($confirmation -ne 's' -and $confirmation -ne 'S') {
            Write-Host "Cancelado." -ForegroundColor Yellow
            exit 0
        }
    } catch {
        # Modo non-interactive: si Read-Host falla y no se paso -Yes, abortar
        Write-Host "`nRead-Host no disponible (modo non-interactive). Use -Yes para confirmar." -ForegroundColor Yellow
        exit 2
    }
}

# ============ 4. Escribir en /inventory/ ============

Write-Host "`n==> 3. Escribiendo productos en /inventory/" -ForegroundColor Cyan
# Construir el body: { "nombre1": {...}, "nombre2": {...}, ... }
# donde cada value es el objeto Producto.
# El approach: leer todo el JSON raiz, parsearlo como objeto, y escribirlo
# en /inventory.json (PUT porque /inventory esta vacio).

$rootData = $rootJson | ConvertFrom-Json
$inventoryMap = @{}
foreach ($prop in $rootData.PSObject.Properties) {
    $key = $prop.Name
    $value = $prop.Value
    # Solo productos (los que tienen model/quantity/unitPrice)
    if ($value -is [PSCustomObject] -and
        ($value.PSObject.Properties.Name -contains "model" -or
         $value.PSObject.Properties.Name -contains "quantity" -or
         $value.PSObject.Properties.Name -contains "unitPrice")) {
        $inventoryMap[$key] = $value
    }
}
$inventoryJson = $inventoryMap | ConvertTo-Json -Depth 10 -Compress
Invoke-Rtdb -Method "PUT" -Path "/inventory.json" -Body $inventoryJson | Out-Null
Write-Host "   $($inventoryMap.Count) productos escritos en /inventory/" -ForegroundColor Green

# ============ 5. Borrar keys migradas de la raiz ============

Write-Host "`n==> 4. Borrando keys migradas de la raiz" -ForegroundColor Cyan
foreach ($path in $keysToDelete) {
    try {
        Invoke-Rtdb -Method "DELETE" -Path $path | Out-Null
    } catch {
        Write-Host "   Aviso: no se pudo borrar $path" -ForegroundColor Yellow
    }
}
Write-Host "   $($keysToDelete.Count) keys borradas de la raiz" -ForegroundColor Green

# ============ 6. Verificacion final ============

Write-Host "`n==> 5. Verificacion" -ForegroundColor Cyan
$newRoot = (Invoke-WebRequest -Uri "$DatabaseUrl/.json?shallow=true" -UseBasicParsing).Content
$newRootCount = ($newRoot | ConvertFrom-Json).PSObject.Properties.Count
$inventoryCount = (Invoke-WebRequest -Uri "$DatabaseUrl/inventory.json?shallow=true" -UseBasicParsing).Content
$inventoryItems = ($inventoryCount | ConvertFrom-Json).PSObject.Properties.Count

Write-Host "   Keys en raiz:      $newRootCount (antes: $($root.PSObject.Properties.Count))" -ForegroundColor White
Write-Host "   Keys en /inventory/: $inventoryItems (migrados: $migrated)" -ForegroundColor White

Write-Host "`nMigracion completada. Reabre la app para verificar." -ForegroundColor Green
Write-Host "Backup en: $backupPath" -ForegroundColor DarkGray