#!/usr/bin/env bash
# pulse-server launcher — run from the directory containing pulse-server-all.jar
set -euo pipefail

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAR="$DIR/pulse-server-all.jar"

if [[ ! -f "$JAR" ]]; then
  JAR="$DIR/build/libs/pulse-server-all.jar"
fi

if [[ ! -f "$JAR" ]]; then
  echo "pulse-server-all.jar not found. Build it with: ./gradlew shadowJar" >&2
  exit 1
fi

export PORT="${PORT:-8080}"
export YTDLP_BIN="${YTDLP_BIN:-yt-dlp}"
export YTDLP_TIMEOUT="${YTDLP_TIMEOUT:-90}"

echo "Starting pulse-server on 0.0.0.0:${PORT} (yt-dlp: ${YTDLP_BIN})"
exec java -Xmx512m -jar "$JAR"
