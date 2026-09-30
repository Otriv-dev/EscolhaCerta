#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
bash scripts/init.sh
set -a
source .env
set +a
docker compose -f docker-compose.yml -f docker-compose.local.yml up -d --wait mysql
mvn spring-boot:run
