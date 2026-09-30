$ErrorActionPreference = 'Stop'
Set-Location (Join-Path $PSScriptRoot '..')
& (Join-Path $PSScriptRoot 'init.ps1')
docker compose up -d --build
if ($LASTEXITCODE -ne 0) { throw 'Falha ao iniciar. Consulte docker compose logs.' }
Write-Host 'Site: http://localhost:8080 | Painel: http://localhost:8080/login'
