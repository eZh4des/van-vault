# Audita IconButton / IconToggleButton con contentDescription = null.
# Un boton de solo-icono SIN descripcion es inaccesible para TalkBack.
# T09 de la auditoria: Item 17.
$ErrorActionPreference = "Stop"

$root = "app\src\main\java\com\vanvault\android\ui"

$total = 0
$missing = 0

Get-ChildItem -Path $root -Recurse -Filter "*.kt" | ForEach-Object {
    $file = $_
    $lines = Get-Content $file.FullName

    for ($i = 0; $i -lt $lines.Count; $i++) {
        if ($lines[$i] -match '(IconButton|IconToggleButton|FilledIconButton)\s*\(') {
            $total++
            $end = [Math]::Min($i + 12, $lines.Count - 1)
            $block = $lines[$i..$end] -join "`n"

            # Si el bloque tiene contentDescription = null -> inaccesible
            if ($block -match 'contentDescription\s*=\s*null') {
                $missing++
                Write-Host "  INACCESIBLE  $($file.Name):$($i + 1)" -ForegroundColor Red
                Write-Host "        $($lines[$i].Trim())" -ForegroundColor DarkGray
            }
        }
    }
}

Write-Host "`nBotones de icono totales: $total" -ForegroundColor Cyan
Write-Host "Sin descripcion (inaccesibles): $missing" -ForegroundColor Yellow
