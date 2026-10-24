# Migra las llamadas de ConfirmDeleteDialog a ConfirmActionDialog.
# T08: un solo componente generico para todas las acciones destructivas.
$ErrorActionPreference = "Stop"

$root = "app\src\main\java\com\vanvault\android\ui\screens"

Get-ChildItem -Path $root -Filter "*.kt" | ForEach-Object {
    $c = Get-Content $_.FullName -Raw
    $orig = $c

    $c = $c -replace 'ConfirmDeleteDialog\(', 'ConfirmActionDialog('
    $c = $c -replace 'import com\.vanvault\.android\.ui\.components\.ConfirmDeleteDialog', 'import com.vanvault.android.ui.components.ConfirmActionDialog'

    # El dialog de logout no es destructivo de datos: usar color primario
    if ($_.Name -in @('ProfileScreen.kt', 'MyAccountScreen.kt')) {
        $c = $c -replace '(?s)(ConfirmActionDialog\(\s*\n\s*showDialog = true,)', '$1'
        $c = $c -replace '(?s)(ConfirmActionDialog\((?:(?!\)).)*?)(onConfirm = \{)', '$1isDestructive = false,
            $2'
    }

    if ($c -ne $orig) {
        Set-Content $_.FullName $c -NoNewline
        Write-Host "  actualizado: $($_.Name)" -ForegroundColor Green
    }
}

Write-Host "`nMigracion a ConfirmActionDialog completada." -ForegroundColor Cyan
