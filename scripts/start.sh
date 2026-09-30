#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
bash scripts/init.sh
docker compose up -d --build
echo "Site: http://localhost:$(sed -n 's/^APP_PORT=//p' .env) | Painel: /login"
