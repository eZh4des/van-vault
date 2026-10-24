# Reemplaza el helper local InlineEmptyState por el componente compartido EmptyState.
# T07 de la auditoria: DRY — un solo EmptyState para toda la app.
$ErrorActionPreference = "Stop"

$p = "app\src\main\java\com\vanvault\android\ui\screens\InventoryScreen.kt"
$c = Get-Content $p -Raw

# 1) Renombrar InlineEmptyState -> EmptyState (del componente compartido)
$c = $c -replace 'InlineEmptyState\(', 'EmptyState('

# 2) Eliminar la definicion local del helper
$c = [regex]::Replace($c, '(?s)\n/\*\*\r?\n \* Empty state inline.*?\n\}\r?\n$', "`r`n")

Set-Content $p $c -NoNewline
Write-Host "  InventoryScreen.kt: InlineEmptyState -> EmptyState" -ForegroundColor Green
