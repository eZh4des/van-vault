# Agrega el import de ScrollableColumn donde falte.
$ErrorActionPreference = "Stop"

$root = "app\src\main\java\com\vanvault\android\ui\screens"
$importLine = "import com.vanvault.android.ui.components.ScrollableColumn"

Get-ChildItem -Path $root -Filter "*.kt" | ForEach-Object {
    $p = $_.FullName
    $c = Get-Content $p -Raw

    # Solo si usa ScrollableColumn( y no tiene el import
    if ($c -match 'ScrollableColumn\(' -and $c -notmatch [regex]::Escape($importLine)) {
        $lines = $c -split "`r?`n"
        # Insertar despues del ultimo import
        $lastIdx = -1
        for ($i = 0; $i -lt $lines.Count; $i++) {
            if ($lines[$i] -match '^import ') { $lastIdx = $i }
        }
        if ($lastIdx -ge 0) {
            $new = @()
            $new += $lines[0..$lastIdx]
            $new += $importLine
            if ($lastIdx -lt ($lines.Count - 1)) { $new += $lines[($lastIdx + 1)..($lines.Count - 1)] }
            Set-Content $p ($new -join "`r`n") -NoNewline
            Write-Host "  import agregado: $($_.Name)" -ForegroundColor Green
        }
    }
}
Write-Host "`nImports completados." -ForegroundColor Cyan
