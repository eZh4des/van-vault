# Migra las Column raiz del Scaffold (sin scroll) a ScrollableColumn.
# T11: WCAG 1.4.4 / 1.4.10 — Reflow al 200% de fuente.
$ErrorActionPreference = "Stop"

$root = "app\src\main\java\com\vanvault\android\ui\screens"

$targets = @(
    "AddProductScreen.kt",
    "AppearanceScreen.kt",
    "AvailableScreen.kt",
    "ForgotPasswordScreen.kt",
    "HelpScreen.kt",
    "HomeScreen.kt",
    "InventoryScreen.kt",
    "LanguageScreen.kt",
    "LoginScreen.kt",
    "NotificationsScreen.kt",
    "ProfileScreen.kt",
    "RegisterScreen.kt",
    "ScannerScreen.kt",
    "SoldScreen.kt"
)

foreach ($name in $targets) {
    $p = Join-Path $root $name
    if (-not (Test-Path $p)) { continue }

    $c = Get-Content $p -Raw
    $orig = $c

    # Reemplazar el bloque Column(modifier = ...padding(innerPadding)...) {
    # por ScrollableColumn(modifier = Modifier.padding(innerPadding), ...) {
    $pattern = '(?s)Column\(\s*modifier = Modifier\s*\.fillMaxSize\(\)\s*\.padding\(innerPadding\)(?<rest>[^)]*)\),'

    $c = [regex]::Replace($c, $pattern, {
        param($m)
        "ScrollableColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)$($m.Groups['rest'].Value)),"
    })

    if ($c -ne $orig) {
        # Agregar el import
        if ($c -notmatch 'import com\.vanvault\.android\.ui\.components\.ScrollableColumn') {
            $c = $c -replace "(?m)^(import com\.vanvault\.android\.ui\.)", "import com.vanvault.android.ui.components.ScrollableColumn`r`n`$1"
        }
        Set-Content $p $c -NoNewline
        Write-Host "  migrado: $name" -ForegroundColor Green
    } else {
        Write-Host "  sin cambio: $name" -ForegroundColor DarkGray
    }
}

Write-Host "`nMigracion a ScrollableColumn completada." -ForegroundColor Cyan
