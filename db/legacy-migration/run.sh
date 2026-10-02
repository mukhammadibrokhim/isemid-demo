#!/usr/bin/env bash
# Legacy -> public2 migration runner — runs all files in sequence.
# Usage:  ./run.sh [host] [port] [db] [user]
# Prompts once for DB name, port and password (if not given via args/env).
set -euo pipefail

HOST="${1:-localhost}"
PORT="${2:-}"
DB="${3:-}"
USER="${4:-postgres}"

if [[ -z "$DB" ]]; then
  read -r -p "Database name [isemid]: " db_input
  DB="${db_input:-isemid}"
fi
if [[ -z "$PORT" ]]; then
  read -r -p "Port [5434]: " port_input
  PORT="${port_input:-5434}"
fi
if [[ -z "${PGPASSWORD:-}" ]]; then
  read -rs -p "Password for user $USER: " PGPASSWORD
  echo
  export PGPASSWORD
fi

export PGOPTIONS='-c client_min_messages=warning'   # reduce NOTICE noise
PSQL=(psql -h "$HOST" -p "$PORT" -U "$USER" -d "$DB" -v ON_ERROR_STOP=1 -X -q)

FILES=(
  00-prep.sql
  10-organization.sql
  20-users.sql
  30-patient.sql
  40-form058.sql
  45-form058-1.sql
  50-card.sql
  51-card161.sql
  52-card174.sql
  53-card175.sql
  54-card205.sql
  55-card-tube.sql
  60-act.sql
  61-act-subtypes.sql
  90-finalize.sql
)

cd "$(dirname "$0")"
echo "!!! WARNING: 00-prep TRUNCATEs public2 business tables. Backup ready? (Ctrl-C to abort)"
read -r -p "Continue? [yes/NO] " ans
[[ "$ans" == "yes" ]] || { echo "aborted"; exit 1; }

for f in "${FILES[@]}"; do
  echo "=== $f ==="
  "${PSQL[@]}" -f "$f"
done

echo "DONE. Check public2._migration_skipped."
