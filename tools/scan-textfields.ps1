# Reemplaza OutlinedTextField por M3TextField en los formularios.
# T06 de la auditoria: 'Diseno de Formularios y Teclado Contextual'.
$ErrorActionPreference = "Stop"

$root = "app\src\main\java\com\vanvault\android\ui\screens"

# Verifica que M3TextField exista
$component = "app\src\main\java\com\vanvault\android\ui\components\M3TextField.kt"
if (-not (Test-Path $component)) {
    Write-Host "ERROR: M3TextField.kt no existe" -ForegroundColor Red
    exit 1
}

Write-Host "Archivos a migrar manualmente (requieren revision semantica):" -ForegroundColor Cyan
$files = Get-ChildItem -Path $root -Filter "*.kt"
foreach ($f in $files) {
    $c = Get-Content $f.FullName -Raw
    $count = ([regex]::Matches($c, 'OutlinedTextField\(')).Count
    if ($count -gt 0) {
        Write-Host ("  {0,-28} {1} OutlinedTextField(s)" -f $f.Name, $count) -ForegroundColor Yellow
    }
}
