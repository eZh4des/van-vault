# Audita los touch targets de la app.
# T10 de la auditoria: 'Tamano Minimo de Objetivos Tactiles (48dp x 48dp)'.
#
# Regla WCAG 2.2 (criterio 2.5.8):
#   - Todo objetivo interactivo debe medir al menos 48dp x 48dp
#
# Enfoque: para cada .clickable se busca el bloque del modifier chain
# hacia atras (hasta 20 lineas o hasta el inicio de la cadena) y se
# verifica si declara heightIn(min >= 48dp) o size(>= 48dp).
$ErrorActionPreference = "Stop"

$root = "app\src\main\java\com\vanvault\android\ui"
$MIN_DP = 48

$issues = 0
$checked = 0
$report = @()

Get-ChildItem -Path $root -Recurse -Filter "*.kt" | ForEach-Object {
    $file = $_
    $lines = Get-Content $file.FullName

    for ($i = 0; $i -lt $lines.Count; $i++) {
        $line = $lines[$i]

        # Ignorar lineas de comentario (// o bloque /** ... */)
        $trimmed = $line.Trim()
        if ($trimmed.StartsWith("//") -or $trimmed.StartsWith("*") -or
            $trimmed.StartsWith("/*") -or $trimmed -eq "") {
            continue
        }

        # ---- Botones de icono con .size(N.dp) explicito menor a 48dp ----
        if ($line -match '(IconButton|IconToggleButton|FilledIconButton)\s*\(') {
            $checked++
            $end = [Math]::Min($i + 12, $lines.Count - 1)
            $block = $lines[$i..$end] -join "`n"
            if ($block -match '\.size\(\s*(\d+)\s*\.dp') {
                $size = [int]$Matches[1]
                if ($size -lt $MIN_DP) {
                    $issues++
                    $report += "  [ICON-BTN] $($file.Name):$($i + 1)  ->  .size(${size}dp) < ${MIN_DP}dp"
                }
            }
        }

        # ---- .clickable sin altura/tamano minimo explicito ----
        if ($line -match '\.clickable\s*[\({]') {
            $checked++
            # Buscar la cadena de modificadores completa: 20 lineas atras
            # (inicio de la cadena) + 6 adelante (continuacion).
            $start = [Math]::Max(0, $i - 20)
            $endF = [Math]::Min($lines.Count - 1, $i + 6)
            $block = $lines[$start..$endF] -join "`n"

            # Solo cuentan las declaraciones de la MISMA cadena:
            # heightIn(min = Ndp) con N >= 48, .size(Ndp) con N >= 48,
            # o el helper .minTouchTarget() (garantiza 48dp por construccion)
            $ok = $false
            foreach ($m in [regex]::Matches($block, 'heightIn\(\s*min\s*=\s*(\d+)\s*\.dp')) {
                if ([int]$m.Groups[1].Value -ge $MIN_DP) { $ok = $true }
            }
            foreach ($m in [regex]::Matches($block, '\.size\(\s*(\d+)\s*\.dp')) {
                if ([int]$m.Groups[1].Value -ge $MIN_DP) { $ok = $true }
            }
            if ($block -match '\.minTouchTarget\(\)') { $ok = $true }

            if (-not $ok) {
                $issues++
                $report += "  [CLICKABLE] $($file.Name):$($i + 1)  ->  sin heightIn/size/minTouchTarget >= ${MIN_DP}dp"
            }
        }
    }
}

$report | ForEach-Object { Write-Host $_ -ForegroundColor Yellow }
Write-Host "`nElementos interactivos revisados: $checked" -ForegroundColor Cyan
Write-Host "Potenciales problemas: $issues" -ForegroundColor Yellow
