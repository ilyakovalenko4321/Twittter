#!/usr/bin/env bash
set -e

INIT_DIR=/docker-entrypoint-initdb.d

# Запускаем Cassandra в фоне
echo ">>> init: starting Cassandra in background..."
docker-entrypoint.sh cassandra -f &
CASSANDRA_PID=$!

# Ждем, пока Cassandra станет доступной
echo ">>> init: waiting for Cassandra to be ready..."
until cqlsh -e 'DESCRIBE CLUSTER' >/dev/null 2>&1; do
  sleep 5
done

echo ">>> init: Cassandra is ready; executing CQL scripts in $INIT_DIR"
cd "$INIT_DIR"
for f in $(find . -type f -name '*.cql' | sort); do
  echo ">>> init: running $f"
  cqlsh -f "$INIT_DIR/$f"
  echo ">>> init: $f done"
done

# Ждём Cassandra, чтобы контейнер не завершился
wait $CASSANDRA_PID