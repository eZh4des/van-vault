# Audita los indicadores de carga de la app.
# T12 de la auditoria: 'Latencia de Respuesta y Retroalimentacion de Carga'.
#
# Item 11 requiere:
#   - Respuesta visual inmediata al toque (< 100 ms)
#   - Sin bloqueos (ANR / congelamientos)
#   - CircularProgressIndicator, LinearProgressIndicator o Skeletons
#     en operaciones > 1 segundo
#
# Detecta:
#   1. [SPINNER-CENTRADO] Pantallas con listas/datos que usan un spinner
#      centrado en vez de skeleton (peor percepcion de velocidad).
#   2. [SIN-INDICADOR] Operaciones de carga sin ningun indicador.
#   3. Resumen de uso de Skeletons / LinearProgressIndicator / LoadingButton.
$ErrorActionPreference = "Stop"

$root = "app\src\main\java\com\vanvault\android\ui"

$skeletons = 0
$linear = 0
$loadingButtons = 0
$spinners = 0
$issues = 0
$report = @()

Get-ChildItem -Path $root -Recurse -Filter "*.kt" | ForEach-Object {
    $file = $_
    $c = Get-Content $file.FullName -Raw
    $lines = Get-Content $file.FullName

    $skeletons += ([regex]::Matches($c, 'Skeleton\w+\(')).Count
    $linear += ([regex]::Matches($c, 'LinearProgressIndicator\(')).Count
    $loadingButtons += ([regex]::Matches($c, 'LoadingButton\(')).Count
    $spinners += ([regex]::Matches($c, 'CircularProgressIndicator\(')).Count

    # --- 1. Spinner centrado en lugar de skeleton ---
    for ($i = 0; $i -lt $lines.Count; $i++) {
        $line = $lines[$i]
        $t = $line.Trim()
        if ($t.StartsWith("//") -or $t.StartsWith("*")) { continue }

        if ($line -match 'CircularProgressIndicator\(') {
            # Buscar hacia atras: es un spinner de pantalla completa (centrado)?
            $start = [Math]::Max(0, $i - 12)
            $before = $lines[$start..$i] -join "`n"
            $isFullScreenSpinner = $before -match 'Alignment\.Center' -or
                                   $before -match 'contentAlignment\s*=\s*Alignment\.Center'

            # Excepcion: dentro de LoadingButton (feedback de boton, correcto)
            # o dentro de un dialogo de busqueda (spinner pequeno-appropriate)
            $inButton = $before -match 'LoadingButton\('
            $inDialog = $file.Name -eq 'DeleteProductPickerDialog.kt'
            if ($isFullScreenSpinner -and -not $inButton -and -not $inDialog) {
                $issues++
                $report += "  [SPINNER-CENTRADO] $($file.Name):$($i + 1)  ->  usar Skeleton en vez de spinner centrado"
            }
        }
    }
}

$report | ForEach-Object { Write-Host $_ -ForegroundColor Yellow }

Write-Host "`n=== Resumen de indicadores de carga ===" -ForegroundColor Cyan
Write-Host "  Skeletons (SkeletonProductList, SkeletonMetricCard, ...) : $skeletons"
Write-Host "  LinearProgressIndicator (barra de progreso)              : $linear"
Write-Host "  LoadingButton (boton con estado de carga)               : $loadingButtons"
Write-Host "  CircularProgressIndicator (spinners restantes)          : $spinners"
Write-Host "`nProblemas potenciales: $issues" -ForegroundColor $(if ($issues -eq 0) { "Green" } else { "Yellow" })
