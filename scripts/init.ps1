$ErrorActionPreference = 'Stop'
Set-Location (Join-Path $PSScriptRoot '..')
if (Test-Path '.env') { Write-Host '.env já existe; preservado.'; exit 0 }
$text = Get-Content '.env.example' -Raw
foreach ($key in @('DB_PASSWORD','DB_ROOT_PASSWORD','ADMIN_PASSWORD')) {
  $bytes = New-Object byte[] 24
  $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
  $rng.GetBytes($bytes)
  $rng.Dispose()
  $value = [System.BitConverter]::ToString($bytes).Replace('-','').ToLower()
  $text = [regex]::Replace($text, "(?m)^$key=.*$", "$key=$value")
}
[System.IO.File]::WriteAllText((Join-Path (Get-Location) '.env'), $text, (New-Object System.Text.UTF8Encoding($false)))
Write-Host '.env criado. Consulte ADMIN_EMAIL e ADMIN_PASSWORD para entrar no painel.'
