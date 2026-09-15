#!/usr/bin/env bash
# Legacy -> public2 ko'chirish — barcha fayllar ketma-ket.
# Foydalanish:  PGPASSWORD=... ./run.sh [host] [port] [db] [user]
set -euo pipefail

HOST="${1:-localhost}"
PORT="${2:-5434}"
DB="${3:-isemid}"
USER="${4:-postgres}"

export PGOPTIONS='-c client_min_messages=warning'   # NOTICE shovqinini kamaytirish
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
echo "!!! DIQQAT: 00-prep public2 biznes jadvallarini TOZALAYDI. Backup bormi? (Ctrl-C to'xtatish)"
read -r -p "Davom etilsinmi? [yes/NO] " ans
[[ "$ans" == "yes" ]] || { echo "bekor qilindi"; exit 1; }

for f in "${FILES[@]}"; do
  echo "=== $f ==="
  "${PSQL[@]}" -f "$f"
done

echo "TUGADI. public2._migration_skipped ni ko'rib chiqing."
