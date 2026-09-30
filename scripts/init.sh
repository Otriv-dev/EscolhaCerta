#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
if [[ -f .env ]]; then echo ".env já existe; preservado."; exit 0; fi
command -v openssl >/dev/null || { echo "Instale OpenSSL ou use init.ps1"; exit 1; }
cp .env.example .env
for key in DB_PASSWORD DB_ROOT_PASSWORD ADMIN_PASSWORD; do
  value=$(openssl rand -hex 24)
  sed -i.bak "s/^${key}=.*/${key}=${value}/" .env
  rm -f .env.bak
done
chmod 600 .env
echo ".env criado. Consulte ADMIN_EMAIL e ADMIN_PASSWORD para entrar no painel."
