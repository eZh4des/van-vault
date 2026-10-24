# Convierte fontSize hardcoded en estilos de la escala M3.
# T04 de la auditoria: 'Jerarquia Tipografica y Unidades Escalables (sp)'.
#
# Reglas:
#  - Si el bloque Text() ya tiene `style = ...`, solo se QUITA el fontSize
#    (el estilo ya define el tamano).
#  - Si NO tiene style, se reemplaza `fontSize = NN.sp` por
#    `style = MaterialTheme.typography.<estilo>`.
#  - El estilo se elige por el tamano y el contexto (fontWeight).
$ErrorActionPreference = "Stop"

$root = "app\src\main\java\com\vanvault\android\ui\screens"

# Tamano -> estilo M3 por defecto
$sizeMap = @{
    '10' = 'labelSmall'
    '11' = 'labelSmall'
    '12' = 'labelMedium'
    '14' = 'bodyMedium'
    '16' = 'bodyLarge'
    '18' = 'titleMedium'
    '20' = 'titleLarge'
    '22' = 'titleLarge'
    '24' = 'headlineSmall'
    '28' = 'headlineMedium'
    '32' = 'headlineLarge'
    '36' = 'displaySmall'
}

# Contexto: si hay fontWeight Bold y tamano >= 24, es un titulo de pantalla.
function Get-Style {
    param([int]$Size, [string]$Block)
    if ($Size -ge 24 -and $Block -match 'Bold') {
        if ($Size -ge 32) { return 'headlineLarge' }
        if ($Size -ge 28) { return 'headlineMedium' }
        return 'headlineSmall'
    }
    # Tamanos grandes sin Bold -> titleLarge / titleMedium
    if ($Size -ge 20) { return 'titleLarge' }
    if ($Size -eq 18) { return 'titleMedium' }
    return $sizeMap["$Size"]
}

$files = Get-ChildItem -Path $root -Filter "*.kt"

foreach ($file in $files) {
    $content = Get-Content $file.FullName -Raw
    if ($content -notmatch 'fontSize = \d+\.sp') { continue }

    $lines = $content -split "`r?`n"
    $out = New-Object System.Collections.Generic.List[string]
    $i = 0
    $changed = 0

    while ($i -lt $lines.Count) {
        $line = $lines[$i]

        if ($line -match 'fontSize = (\d+)\.sp') {
            $size = [int]$Matches[1]
            # Reconstruir el bloque actual (hacia atras hasta 'Text(' o '.copy(')
            $block = $lines[[Math]::Max(0, $i - 12)..$i] -join "`n"

            # Si el bloque ya tiene style= -> solo quitar fontSize
            if ($block -match 'style\s*=') {
                # quitar la linea del fontSize (puede tener coma)
                $out.Add(($line -replace 'fontSize = \d+\.sp,\s*', '' -replace ',\s*fontSize = \d+\.sp', ''))
                $changed++
                $i++
                continue
            }

            # Si no hay style, convertir a style = typography.X
            $style = Get-Style -Size $size -Block $block
            $newLine = $line -replace "fontSize = $size\.sp", "style = MaterialTheme.typography.$style"
            $out.Add($newLine)
            $changed++
            $i++
            continue
        }

        $out.Add($line)
        $i++
    }

    if ($changed -gt 0) {
        Set-Content $file.FullName ($out -join "`r`n") -NoNewline
        Write-Host "  $($file.Name): $changed reemplazos" -ForegroundColor Green
    }
}

Write-Host "`nTipografia migrada a estilos M3." -ForegroundColor Cyan
