# Agrega los imports de las extensiones de color extendidas (success/warning).
$ErrorActionPreference = "Stop"

$root = "app\src\main\java\com\vanvault\android\ui"

$files = @(
    "components\DashboardKpiCard.kt",
    "components\ResponsiveUsersTable.kt",
    "screens\AdminScreen.kt",
    "screens\DashboardScreen.kt",
    "screens\RoiScreen.kt"
)

foreach ($rel in $files) {
    $p = Join-Path $root $rel
    if (-not (Test-Path $p)) { continue }
    $content = Get-Content $p -Raw
    if ($content -match 'successColor|warningColor') {
        $imports = @()
        if ($content -match 'MaterialTheme\.successColor' -and $content -notmatch 'import com\.vanvault\.android\.ui\.theme\.successColor') {
            $imports += "import com.vanvault.android.ui.theme.successColor"
        }
        if ($content -match 'MaterialTheme\.warningColor' -and $content -notmatch 'import com\.vanvault\.android\.ui\.theme\.warningColor') {
            $imports += "import com.vanvault.android.ui.theme.warningColor"
        }
        if ($imports.Count -gt 0) {
            # Insertar despues del ultimo import
            $lastImport = [regex]::Matches($content, '(?m)^import .*$') | Select-Object -Last 1
            $idx = $lastImport.Index + $lastImport.Length
            $content = $content.Substring(0, $idx) + "`r`n" + ($imports -join "`r`n") + $content.Substring($idx)
            Set-Content $p $content -NoNewline
            Write-Host "  imports agregados: $rel" -ForegroundColor Green
        }
    }
}

Write-Host "`nImports completados." -ForegroundColor Cyan
