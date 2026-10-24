# Aplica semantica agrupada (mergeDescendants) a las Cards/Surfaces clickeables.
# T09 de la auditoria: Item 17 — TalkBack debe anunciar cada elemento
# interactivo como una unidad, no como multiples nodos hijos.
$ErrorActionPreference = "Stop"

$edits = @(
    @{
        File = "app\src\main\java\com\vanvault\android\ui\screens\ProfileScreen.kt"
        Old  = @'
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
'@
        New  = @'
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            // T09: agrupar la fila en un solo nodo accesible
            .merged(
                description = title,
                state = if (isDestructive) "Accion destructiva" else null
            )
'@
        Import = "import com.vanvault.android.ui.util.A11y.merged"
    },
    @{
        File = "app\src\main\java\com\vanvault\android\ui\screens\HelpScreen.kt"
        Old  = @'
            .clickable { expanded = !expanded }
'@
        New  = @'
            .clickable { expanded = !expanded }
            // T09: anunciar el estado expandido/contraido
            .merged(
                description = title,
                state = if (expanded) "Expandido" else "Contraido"
            )
'@
        Import = "import com.vanvault.android.ui.util.A11y.merged"
    },
    @{
        File = "app\src\main\java\com\vanvault\android\ui\screens\LanguageScreen.kt"
        Old  = @'
            .clickable { onSelect() }
'@
        New  = @'
            .clickable { onSelect() }
            // T09: indicar el idioma seleccionado
            .merged(
                description = language.displayName,
                state = if (isSelected) "Seleccionado" else null
            )
'@
        Import = "import com.vanvault.android.ui.util.A11y.merged"
    },
    @{
        File = "app\src\main\java\com\vanvault\android\ui\screens\EditProductScreen.kt"
        Old  = @'
                                .clickable { viewModel.onSelectFromList(product) },
'@
        New  = @'
                                .clickable { viewModel.onSelectFromList(product) }
                                // T09: anunciar el producto como una unidad
                                .merged(
                                    description = "${product.name}, SKU ${product.modelOrBarcode}, ${product.quantity} unidades, ${product.unitPrice.toDisplayString()}"
                                ),
'@
        Import = "import com.vanvault.android.ui.util.A11y.merged"
    }
)

foreach ($e in $edits) {
    $p = $e.File
    if (-not (Test-Path $p)) { Write-Host "  NO EXISTE: $($e.File)" -ForegroundColor Red; continue }
    $c = Get-Content $p -Raw

    if ($c -notmatch [regex]::Escape($e.Old.TrimEnd())) {
        Write-Host "  PATTERN NO ENCONTRADO: $($e.File)" -ForegroundColor Yellow
        continue
    }

    $c = $c.Replace($e.Old.TrimEnd(), $e.New.TrimEnd())

    if ($c -notmatch [regex]::Escape($e.Import)) {
        $lines = $c -split "`r?`n"
        $lastImport = ($lines | Select-String -Pattern '^import ' | Select-Object -Last 1)
        $idx = $lastImport.LineNumber - 1
        $newLines = @()
        $newLines += $lines[0..$idx]
        $newLines += $e.Import
        if ($idx -lt ($lines.Count - 1)) { $newLines += $lines[($idx + 1)..($lines.Count - 1)] }
        $c = $newLines -join "`r`n"
    }

    Set-Content $p $c -NoNewline
    Write-Host "  actualizado: $($e.File)" -ForegroundColor Green
}

Write-Host "`nSemantica agrupada aplicada." -ForegroundColor Cyan
