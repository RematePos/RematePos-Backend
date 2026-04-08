#!/usr/bin/env sh
set -eu

MONGO_HOST="${MONGO_HOST:-mongodb}"
MONGO_PORT="${MONGO_PORT:-27017}"
MONGO_USER="${MONGO_DB_USERNAME:-admin}"
MONGO_PASS="${MONGO_DB_PASSWORD:-admin123}"
MIGRATIONS_DIR="${MIGRATIONS_DIR:-/migrations/changelog}"
MIGRATION_DB="${MIGRATION_DB:-db_migrations}"

URI="mongodb://${MONGO_USER}:${MONGO_PASS}@${MONGO_HOST}:${MONGO_PORT}/admin?authSource=admin"

echo "[mongo-migrate] waiting for mongodb at ${MONGO_HOST}:${MONGO_PORT}"
until mongosh "$URI" --quiet --eval "db.adminCommand({ ping: 1 }).ok" >/dev/null 2>&1; do
  sleep 2
done

echo "[mongo-migrate] mongodb is reachable"

for file in "$MIGRATIONS_DIR"/*.js; do
  [ -e "$file" ] || continue
  name=$(basename "$file")

  applied=$(mongosh "$URI" --quiet --eval "db.getSiblingDB(\"${MIGRATION_DB}\").applied_migrations.countDocuments({ _id: \"${name}\" })")
  if [ "$applied" != "0" ]; then
    echo "[mongo-migrate] skip ${name} (already applied)"
    continue
  fi

  echo "[mongo-migrate] apply ${name}"
  mongosh "$URI" --file "$file" --quiet
  mongosh "$URI" --quiet --eval "db.getSiblingDB(\"${MIGRATION_DB}\").applied_migrations.insertOne({ _id: \"${name}\", appliedAt: new Date() })"
done

echo "[mongo-migrate] done"
