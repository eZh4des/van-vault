# Convierte accentColorFor a @Composable y corrige el rol ERROR.
$ErrorActionPreference = "Stop"

$root = "app\src\main\java\com\vanvault\android\ui"

$targets = @(
    "components\DashboardKpiCard.kt",
    "screens\DashboardScreen.kt"
)

foreach ($rel in $targets) {
    $p = Join-Path $root $rel
    $content = Get-Content $p -Raw

    # 1) Marcar accentColorFor como @Composable
    $content = $content -replace 'private fun accentColorFor\(', '@Composable
private fun accentColorFor('

    # 2) El rol ERROR debe mapear a colorScheme.error (no primary)
    $content = $content -replace 'KpiMetric\.AccentColor\.ERROR -> MaterialTheme\.colorScheme\.primary', 'KpiMetric.AccentColor.ERROR -> MaterialTheme.colorScheme.error'

    Set-Content $p $content -NoNewline
    Write-Host "  corregido: $rel" -ForegroundColor Green
}

Write-Host "`nListo." -ForegroundColor Cyan
