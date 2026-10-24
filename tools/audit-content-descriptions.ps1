# Audita los contentDescription de los Iconos de Compose.
# T09 de la auditoria: 'Etiquetado Semantico para Lector de Pantalla (TalkBack)'.
#
# Reglas:
#  - Icono dentro de un boton con texto -> contentDescription = null (decorativo)
#  - Icono que es el UNICO contenido de un boton -> DEBE tener description
#  - Icono informativo (aviso, estado) -> DEBE tener description
$ErrorActionPreference = "Stop"

$root = "app\src\main\java\com\vanvault\android\ui"

$total = 0
$missing = 0

Get-ChildItem -Path $root -Recurse -Filter "*.kt" | ForEach-Object {
    $file = $_
    $lines = Get-Content $file.FullName
    $block = ""

    for ($i = 0; $i -lt $lines.Count; $i++) {
        $line = $lines[$i]

        if ($line -match 'Icon\(') {
            $total++
            # Capturar el bloque del Icon (siguientes ~8 lineas)
            $end = [Math]::Min($i + 8, $lines.Count - 1)
            $block = $lines[$i..$end] -join "`n"

            if ($block -notmatch 'contentDescription') {
                $missing++
                Write-Host "  FALTA  $($file.Name):$($i + 1)" -ForegroundColor Red
                Write-Host "        $($line.Trim())" -ForegroundColor DarkGray
            }
        }
    }
}

Write-Host "`nTotal Icon() en la app: $total" -ForegroundColor Cyan
Write-Host "Sin contentDescription (revisar): $missing" -ForegroundColor Yellow
