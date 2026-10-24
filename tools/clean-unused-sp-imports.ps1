# Elimina imports de 'sp' no utilizados (tras migrar a estilos M3).
$ErrorActionPreference = "Stop"

$root = "app\src\main\java\com\vanvault\android\ui"
$files = Get-ChildItem -Path $root -Recurse -Filter "*.kt"

foreach ($f in $files) {
    $content = Get-Content $f.FullName -Raw
    # Solo tema/Color.kt y theme/Type.kt deben conservar 'sp' (definen la escala)
    if ($f.FullName -match '\\theme\\') { continue }

    # Detectar uso real de '.sp' (ej. 16.sp) fuera del import
    $body = $content -replace "(?m)^import androidx\.compose\.ui\.unit\.sp\r?\n", ""
    if ($body -notmatch '\d+\.sp') {
        $content = $content -replace "(?m)^import androidx\.compose\.ui\.unit\.sp\r?\n", ""
        Set-Content $f.FullName $content -NoNewline
        Write-Host "  import sp eliminado: $($f.Name)" -ForegroundColor Green
    }
}

Write-Host "`nImports limpiados." -ForegroundColor Cyan
