<#
.SYNOPSIS
    Verifica el contraste WCAG 2.2 de la paleta VanVault.
    Genera evidencia para el Item 16 de la auditoria.

.DESCRIPTION
    Calcula el ratio de contraste (WCAG 2.1 formula) para los pares de
    roles semanticos del tema Light y Dark, y valida contra los umbrales:
      - Texto normal:      >= 4.5:1  (criterio 1.4.3)
      - Texto grande:      >= 3.0:1  (1.4.3 excepcion)
      - UI / iconos:       >= 3.0:1  (criterio 1.4.11)

.EXAMPLE
    .\tools\check-contrast.ps1
    .\tools\check-contrast.ps1 -Json
#>

[CmdletBinding()]
param(
    [switch]$Json = $false
)

$ErrorActionPreference = "Stop"

# ---------- Conversion sRGB -> linear ----------
function Get-LinearChannel {
    param([double]$Channel)
    if ($Channel -le 0.03928) { return $Channel / 12.92 }
    return [Math]::Pow((($Channel + 0.055) / 1.055), 2.4)
}

function Get-RelativeLuminance {
    param([string]$Hex)
    $h = $Hex.TrimStart('#')
    if ($h.Length -eq 3) { $h = "$h$h$h"[0..2] -join '' }
    $r = [Convert]::ToInt32($h.Substring(0, 2), 16) / 255.0
    $g = [Convert]::ToInt32($h.Substring(2, 2), 16) / 255.0
    $b = [Convert]::ToInt32($h.Substring(4, 2), 16) / 255.0
    return 0.2126 * (Get-LinearChannel $r) + 0.7152 * (Get-LinearChannel $g) + 0.0722 * (Get-LinearChannel $b)
}

function Get-ContrastRatio {
    param([string]$A, [string]$B)
    $l1 = Get-RelativeLuminance $A
    $l2 = Get-RelativeLuminance $B
    $lighter = [Math]::Max($l1, $l2)
    $darker = [Math]::Min($l1, $l2)
    return [Math]::Round(($lighter + 0.05) / ($darker + 0.05), 2)
}

# ---------- Paleta (de app/src/main/java/.../theme/Color.kt) ----------
$light = @{
    onPrimary = "#FFFFFF"; primary = "#DC2626"
    onPrimaryContainer = "#410002"; primaryContainer = "#FEE2E2"
    onSecondary = "#FFFFFF"; secondary = "#775652"
    onSecondaryContainer = "#2C1512"; secondaryContainer = "#FFDAD6"
    onTertiary = "#FFFFFF"; tertiary = "#725B2E"
    onTertiaryContainer = "#281800"; tertiaryContainer = "#FFDEA6"
    onError = "#FFFFFF"; error = "#BA1A1A"
    onErrorContainer = "#410002"; errorContainer = "#FFDAD6"
    onSuccess = "#FFFFFF"; success = "#146C2E"
    onSuccessContainer = "#072711"; successContainer = "#C4EED0"
    onWarning = "#FFFFFF"; warning = "#8F4E00"
    onWarningContainer = "#2E1500"; warningContainer = "#FFDCC1"
    onBackground = "#1C1B1F"; background = "#FFFFFF"
    onSurface = "#1C1B1F"; surface = "#FFFFFF"
    onSurfaceVariant = "#534341"; surfaceVariant = "#F5DDDA"
    outline = "#857371"; outlineVariant = "#D8C2C0"
    surfaceContainerLowest = "#FFFFFF"; surfaceContainerLow = "#FFF1F0"
    surfaceContainer = "#FCEAE8"; surfaceContainerHigh = "#F7E4E2"
    surfaceContainerHighest = "#F1DFDD"
    inverseSurface = "#313030"; inverseOnSurface = "#F4F0EF"
    inversePrimary = "#FFB4AB"
}

$dark = @{
    onPrimary = "#690005"; primary = "#FFB4AB"
    onPrimaryContainer = "#FFDAD6"; primaryContainer = "#93000A"
    onSecondary = "#442927"; secondary = "#E7BDB8"
    onSecondaryContainer = "#FFDAD6"; secondaryContainer = "#5D3F3C"
    onTertiary = "#402D04"; tertiary = "#DEC287"
    onTertiaryContainer = "#FFDEA6"; tertiaryContainer = "#594419"
    onError = "#690005"; error = "#FFB4AB"
    onErrorContainer = "#FFDAD6"; errorContainer = "#93000A"
    onSuccess = "#00390F"; success = "#6DD58C"
    onSuccessContainer = "#C4EED0"; successContainer = "#0F5223"
    onWarning = "#4A2800"; warning = "#FFB77B"
    onWarningContainer = "#FFDCC1"; warningContainer = "#6D3A00"
    onBackground = "#F1DFDD"; background = "#1A1110"
    onSurface = "#F1DFDD"; surface = "#1A1110"
    onSurfaceVariant = "#D8C2C0"; surfaceVariant = "#534341"
    outline = "#A08C8A"; outlineVariant = "#534341"
    surfaceContainerLowest = "#140C0B"; surfaceContainerLow = "#231918"
    surfaceContainer = "#271D1C"; surfaceContainerHigh = "#322826"
    surfaceContainerHighest = "#3D3231"
    inverseSurface = "#F1DFDD"; inverseOnSurface = "#313030"
    inversePrimary = "#DC2626"
}

# Pares a verificar: @("rolFondo", "rolTexto", "minRatio", "descripcion")
$pairs = @(
    @("surface", "onSurface", 4.5, "Texto principal sobre superficie"),
    @("surface", "onSurfaceVariant", 4.5, "Texto secundario sobre superficie"),
    @("background", "onBackground", 4.5, "Texto principal sobre fondo"),
    @("primary", "onPrimary", 4.5, "Texto sobre boton primario"),
    @("primaryContainer", "onPrimaryContainer", 4.5, "Texto sobre contenedor primario"),
    @("secondary", "onSecondary", 4.5, "Texto sobre secundario"),
    @("secondaryContainer", "onSecondaryContainer", 4.5, "Texto sobre contenedor secundario"),
    @("tertiary", "onTertiary", 4.5, "Texto sobre terciario"),
    @("tertiaryContainer", "onTertiaryContainer", 4.5, "Texto sobre contenedor terciario"),
    @("error", "onError", 4.5, "Texto sobre error"),
    @("errorContainer", "onErrorContainer", 4.5, "Texto sobre contenedor de error"),
    @("success", "onSuccess", 4.5, "Texto sobre success"),
    @("successContainer", "onSuccessContainer", 4.5, "Texto sobre contenedor success"),
    @("warning", "onWarning", 4.5, "Texto sobre warning"),
    @("warningContainer", "onWarningContainer", 4.5, "Texto sobre contenedor warning"),
    @("inverseSurface", "inverseOnSurface", 4.5, "Texto sobre superficie inversa"),
    @("primary", "surface", 3.0, "Boton primario vs superficie (UI)"),
    @("outline", "surface", 3.0, "Borde vs superficie (UI)"),
    @("error", "surface", 3.0, "Color de error vs superficie (UI)"),
    @("success", "surface", 3.0, "Color success vs superficie (UI)"),
    @("warning", "surface", 3.0, "Color warning vs superficie (UI)"),
    @("onSurfaceVariant", "surfaceContainerHigh", 4.5, "Texto secundario vs surfaceContainerHigh"),
    @("onSurface", "surfaceContainerHigh", 4.5, "Texto principal vs surfaceContainerHigh"),
    @("onSurface", "surfaceContainerHighest", 4.5, "Texto principal vs surfaceContainerHighest")
)

function Test-Scheme {
    param([hashtable]$Scheme, [string]$SchemeName)
    $results = @()
    $failures = 0
    foreach ($p in $pairs) {
        $bg = $Scheme[$p[0]]; $fg = $Scheme[$p[1]]; $min = [double]$p[2]; $desc = $p[3]
        $ratio = Get-ContrastRatio $bg $fg
        $pass = $ratio -ge $min
        if (-not $pass) { $failures++ }
        $results += [PSCustomObject]@{
            Scheme = $SchemeName
            Background = $p[0]
            Foreground = $p[1]
            Ratio = $ratio
            Min = $min
            Pass = $pass
            Description = $desc
        }
    }
    return @{ Results = $results; Failures = $failures }
}

$lightResult = Test-Scheme -Scheme $light -SchemeName "LIGHT"
$darkResult = Test-Scheme -Scheme $dark -SchemeName "DARK"
$all = @($lightResult.Results) + @($darkResult.Results)

if ($Json) {
    $all | ConvertTo-Json -Depth 4
    exit ($lightResult.Failures + $darkResult.Failures)
}

Write-Host "`n=== VERIFICACION DE CONTRASTE WCAG 2.2 - VanVault ===" -ForegroundColor Cyan
Write-Host "Item 16 de la auditoria: contraste de color`n" -ForegroundColor DarkGray

foreach ($scheme in @(@{n="LIGHT"; r=$lightResult}, @{n="DARK"; r=$darkResult})) {
    Write-Host "--- TEMA $($scheme.n) ---" -ForegroundColor Yellow
    $scheme.r.Results | ForEach-Object {
        $status = if ($_.Pass) { "PASS" } else { "FAIL" }
        $color = if ($_.Pass) { "Green" } else { "Red" }
        Write-Host ("  [{0}] {1,-22} vs {2,-20} {3,6}:1  (min {4}:1)  {5}" -f `
            $status, $_.Background, $_.Foreground, $_.Ratio, $_.Min, $_.Description) -ForegroundColor $color
    }
    Write-Host ""
}

$totalFail = $lightResult.Failures + $darkResult.Failures
if ($totalFail -eq 0) {
    Write-Host "RESULTADO: TODOS los pares cumplen WCAG 2.2 AA" -ForegroundColor Green
    Write-Host "Evidencia lista para el Item 16 de la auditoria." -ForegroundColor Green
    exit 0
} else {
    Write-Host "RESULTADO: $totalFail pares fallan el contraste" -ForegroundColor Red
    exit 1
}
