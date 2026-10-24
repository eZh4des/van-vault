# Reemplaza colores hardcoded por roles semanticos de Material 3.
# M03.4 de la auditoria: 'Roles Semanticos de Color'.
$ErrorActionPreference = "Stop"

$root = "app\src\main\java\com\vanvault\android\ui"

# Mapa global (caso comun). Solo en contextos @Composable.
$globalMap = @{
    'Color\(0xFFFEE2E2\)'      = 'MaterialTheme.colorScheme.primaryContainer'
    'Color\(0xFF410002\)'      = 'MaterialTheme.colorScheme.onPrimaryContainer'
    'Color\(0xFF7F1D1D\)'      = 'MaterialTheme.colorScheme.onPrimaryContainer'
    'Color\(0xFFDCFCE7\)'      = 'MaterialTheme.colorScheme.successContainer'
    'Color\(0xFFFEF3C7\)'      = 'MaterialTheme.colorScheme.tertiaryContainer'
    'Color\(0xFFFECACA\)'      = 'MaterialTheme.colorScheme.errorContainer'
    'Color\(0xFFE4E4E7\)'      = 'MaterialTheme.colorScheme.outlineVariant'
    'Color\(0xFF059669\)'      = 'MaterialTheme.successColor'
    'Color\(0xFFF97316\)'      = 'MaterialTheme.warningColor'
    'Color\(0xFF71717A\)'      = 'MaterialTheme.colorScheme.onSurfaceVariant'
    'Color\(0xFFFACC15\)'      = 'MaterialTheme.colorScheme.tertiary'
    'Color\(0xFFDC2626\)'      = 'MaterialTheme.colorScheme.primary'
}

$files = Get-ChildItem -Path $root -Recurse -Filter "*.kt" |
         Where-Object { $_.FullName -notmatch '\\theme\\' }

foreach ($file in $files) {
    $content = Get-Content $file.FullName -Raw
    $original = $content
    foreach ($k in $globalMap.Keys) {
        $content = $content -replace $k, $globalMap[$k]
    }
    if ($content -ne $original) {
        Set-Content $file.FullName $content -NoNewline
        Write-Host "  actualizado: $($file.Name)" -ForegroundColor Green
    }
}

Write-Host "`nReemplazo global completado." -ForegroundColor Cyan
