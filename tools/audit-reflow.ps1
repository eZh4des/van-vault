# Audita el reflow al 200% de tamano de fuente.
# T11 de la auditoria: 'Escalado Dinamico de Texto al 200% y Reflow'.
#
# WCAG 2.2:
#   1.4.4 Resize Text: el texto escala al 200% sin perdida
#   1.4.10 Reflow: sin scroll bidimensional, el contenido no debe
#          requerir scroll en dos direcciones
#
# Detecta (精准, sin falsos positivos):
#   1. [SIN-SCROLL]  La Column RAIZ del Scaffold (la que recibe
#      innerPadding) no tiene verticalScroll / LazyColumn.
#      -> su contenido se corta al 200%.
#   2. [ALTO-FIJO]   Un Button/TextField con .height(N.dp) fijo
#      (<= 64dp) que contiene texto -> el texto se recorta.
#      (Spacer NO cuentan: son separadores, no contienen texto)
#   3. [ROW-FIJA]    Un Row con >= 4 Text y sin FlowRow/horizontalScroll
#      -> no puede reflowear a ancho estrecho.
$ErrorActionPreference = "Stop"

$root = "app\src\main\java\com\vanvault\android\ui"

$issues = 0
$report = @()

Get-ChildItem -Path $root -Recurse -Filter "*.kt" | ForEach-Object {
    $file = $_
    $lines = Get-Content $file.FullName
    $text = $lines -join "`n"

    # --- 1. Column RAIZ del Scaffold sin scroll ---
    $rootCols = [regex]::Matches($text, 'Column\((?:[^()]|\((?:[^()]|\([^()]*\))*\))*?\)\s*\{', 'Singleline')
    foreach ($m in $rootCols) {
        $idx = $m.Index
        $lineNo = ($text.Substring(0, $idx) -split "`n").Count
        $end = [Math]::Min($lines.Count - 1, $lineNo + 12)
        $header = $lines[($lineNo - 1)..$end] -join "`n"

        # Solo la Column que recibe innerPadding (contenido del Scaffold)
        if ($header -notmatch 'innerPadding') { continue }

        # El cuerpo incluye el header (donde esta el modifier con el scroll)
        $bodyStart = [Math]::Max(0, $lineNo - 3)
        $body = $lines[$bodyStart..([Math]::Min($lines.Count - 1, $lineNo + 80))] -join "`n"
        $hasScroll = $body -match 'verticalScroll\(' -or
                      $body -match 'LazyColumn\(' -or
                      $body -match 'LazyVerticalGrid\(' -or
                      $body -match 'ScrollableColumn\(' -or
                      $body -match 'FlowRow\('

        if (-not $hasScroll) {
            # Excepcion: ScannerScreen es una vista de camara a pantalla
            # completa; el reflow de texto no aplica ahi.
            if ($file.Name -eq "ScannerScreen.kt") { continue }
            $issues++
            $report += "  [SIN-SCROLL]  $($file.Name):$lineNo  ->  Column raiz del Scaffold sin scroll"
        }
    }

    # --- 2. Alturas fijas en elementos con texto (excluir Spacer) ---
    for ($i = 0; $i -lt $lines.Count; $i++) {
        $line = $lines[$i]
        $trimmed = $line.Trim()
        if ($trimmed.StartsWith("//") -or $trimmed.StartsWith("*")) { continue }
        if ($trimmed -match '^Spacer\(') { continue }

        if ($line -match '\.height\(\s*(\d+)\s*\.dp') {
            $h = [int]$Matches[1]
            if ($h -gt 64) { continue }

            $end = [Math]::Min($lines.Count - 1, $i + 12)
            $block = $lines[$i..$end] -join "`n"
            # El alto fijo solo es problema si el MISMO elemento contiene texto
            if ($block -match '\bText\(' -or $block -match 'Button\(' -or $block -match 'TextField\(') {
                $issues++
                $report += "  [ALTO-FIJO]   $($file.Name):$($i + 1)  ->  .height(${h}dp) en elemento con texto"
            }
        }
    }

    # --- 3. Row con 4+ Text sin reflow ---
    # Ventana de 20 lineas: suficiente para contar los Text del propio Row
    # sin arrastrar los del siguiente bloque.
    $rowMatches = [regex]::Matches($text, 'Row\((?:[^()]|\((?:[^()]|\([^()]*\))*\))*?\)\s*\{', 'Singleline')
    foreach ($m in $rowMatches) {
        $idx = $m.Index
        $lineNo = ($text.Substring(0, $idx) -split "`n").Count
        $end = [Math]::Min($lines.Count - 1, $lineNo + 20)
        $block = $lines[($lineNo - 1)..$end] -join "`n"

        $textCount = ([regex]::Matches($block, '\bText\(')).Count
        $hasReflow = $block -match 'horizontalScroll\(' -or $block -match 'FlowRow\('

        if ($textCount -ge 4 -and -not $hasReflow) {
            $issues++
            $report += "  [ROW-FIJA]    $($file.Name):$lineNo  ->  Row con $textCount Text sin FlowRow"
        }
    }

    # --- 4. SCROLL BIDIMENSIONAL (WCAG 1.4.10) ---
    # Un horizontalScroll dentro de un contenedor con scroll vertical
    # (verticalScroll / LazyColumn / ScrollableColumn) viola 1.4.10.
    for ($i = 0; $i -lt $lines.Count; $i++) {
        $line = $lines[$i]
        $trimmed = $line.Trim()
        if ($trimmed.StartsWith("//") -or $trimmed.StartsWith("*")) { continue }

        if ($line -match '\.horizontalScroll\(') {
            # Buscar hacia atras hasta 25 lineas un contenedor con scroll vertical
            $start = [Math]::Max(0, $i - 25)
            $before = $lines[$start..$i] -join "`n"
            $hasVertical = $before -match 'LazyColumn\(' -or
                           $before -match 'verticalScroll\(' -or
                           $before -match 'ScrollableColumn\('

            if ($hasVertical) {
                $issues++
                $report += "  [BIDIMENSIONAL] $($file.Name):$($i + 1)  ->  horizontalScroll dentro de scroll vertical (WCAG 1.4.10)"
            }
        }
    }
}

$report | ForEach-Object { Write-Host $_ -ForegroundColor Yellow }
Write-Host "`nProblemas potenciales de reflow: $issues" -ForegroundColor Cyan
