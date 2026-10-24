# Elimina alturas fijas en botones con texto (WCAG 1.4.4 Resize Text).
# T11: al 200% de fuente, un .height(50.dp) recorta el texto del boton.
$ErrorActionPreference = "Stop"

$root = "app\src\main\java\com\vanvault\android\ui\screens"

# .height(50.dp) / .height(56.dp) en botones -> quitar (M3 Button ya
# tiene minHeight de 40dp y crece con el contenido)
$pattern = '(?s)(\.fillMaxWidth\(\)\s*\n\s*)\.height\(\s*\d+\s*\.dp\),'

Get-ChildItem -Path $root -Filter "*.kt" | ForEach-Object {
    $p = $_.FullName
    $c = Get-Content $p -Raw
    $orig = $c

    # Quitar .height(N.dp) cuando va seguido de .fillMaxWidth() en un Button
    $c = [regex]::Replace($c, '(?s)(Button\((?:[^()]|\((?:[^()]|\([^()]*\))*\))*\s*\n\s*onClick\s*=\s*\{[^}]*\},.*?)(\.fillMaxWidth\(\)\s*\n\s*)\.height\(\s*\d+\s*\.dp\),', '$1$2')

    if ($c -ne $orig) {
        Set-Content $p $c -NoNewline
        Write-Host "  actualizado: $($_.Name)" -ForegroundColor Green
    }
}

Write-Host "`nAlturas fijas en botones eliminadas." -ForegroundColor Cyan
