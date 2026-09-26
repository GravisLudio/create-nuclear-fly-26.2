#!/bin/bash
# javap contra el classpath exacto del build (build/cp.txt, generado por `gradlew writeClasspath`).
# Uso: tools/jp.sh [-p] <clase> [...]
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
javap -cp "@$(cygpath -w "$ROOT/build/cp.txt")" "$@" 2>/dev/null || \
javap -cp "$(cat "$ROOT/build/cp.txt")" "$@"
