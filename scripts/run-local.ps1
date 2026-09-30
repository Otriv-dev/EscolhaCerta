$ErrorActionPreference = 'Stop'
Set-Location (Join-Path $PSScriptRoot '..')
& (Join-Path $PSScriptRoot 'init.ps1')
Get-Content '.env' | ForEach-Object { if ($_ -match '^([A-Z][A-Z0-9_]*)=(.*)$') { [Environment]::SetEnvironmentVariable($matches[1], $matches[2], 'Process') } }
docker compose -f docker-compose.yml -f docker-compose.local.yml up -d --wait mysql
if ($LASTEXITCODE -ne 0) { throw 'Falha ao iniciar MySQL' }
mvn spring-boot:run
