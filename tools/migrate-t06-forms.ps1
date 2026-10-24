# Migra los formularios de RegisterScreen, ForgotPasswordScreen y EditProductScreen
# al componente M3TextField (T06).
$ErrorActionPreference = "Stop"

$root = "app\src\main\java\com\vanvault\android\ui\screens"

# ---------- RegisterScreen ----------
$p = Join-Path $root "RegisterScreen.kt"
$c = Get-Content $p -Raw

# Nombre
$c = [regex]::Replace($c, '(?s)                // Nombre Field.*?\n                \}\n\n                Spacer\(modifier = Modifier\.height\(20\.dp\)\)', @'
                // Nombre Field
                M3TextField(
                    value = state.name,
                    onValueChange = viewModel::onNameChange,
                    label = "Nombre completo",
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                    supportingText = "Ej. Juan Pérez"
                )

                Spacer(modifier = Modifier.height(20.dp))
'@)

# Email
$c = [regex]::Replace($c, '(?s)                // Email Field.*?\n                \}\n\n                Spacer\(modifier = Modifier\.height\(20\.dp\)\)', @'
                // Email Field
                M3TextField(
                    value = state.email,
                    onValueChange = viewModel::onEmailChange,
                    label = "Correo electrónico",
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                    supportingText = "Ej. correo@ejemplo.com"
                )

                Spacer(modifier = Modifier.height(20.dp))
'@)

# Password
$c = [regex]::Replace($c, '(?s)                // Password Field.*?\n                \}\n\n                Spacer\(modifier = Modifier\.height\(20\.dp\)\)', @'
                // Password Field
                M3TextField(
                    value = state.password,
                    onValueChange = viewModel::onPasswordChange,
                    label = "Contraseña",
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next,
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
                    supportingText = "Mín. 8 caracteres, mayúscula, minúscula y número"
                )

                Spacer(modifier = Modifier.height(20.dp))
'@)

Set-Content $p $c -NoNewline
Write-Host "  RegisterScreen.kt migrado" -ForegroundColor Green

# ---------- ForgotPasswordScreen ----------
$p = Join-Path $root "ForgotPasswordScreen.kt"
$c = Get-Content $p -Raw
$c = [regex]::Replace($c, '(?s)                // Email Field.*?\n                \}\n', @'
                // Email Field
                M3TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Correo electrónico",
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done,
                    supportingText = "Te enviaremos un enlace de recuperación"
                )
'@)
Set-Content $p $c -NoNewline
Write-Host "  ForgotPasswordScreen.kt migrado" -ForegroundColor Green

Write-Host "`nMigracion T06 aplicada." -ForegroundColor Cyan
