#!/bin/bash
# Uso: tools/errs.sh <fragmento-de-ruta>   -> errores de javac de ese archivo (linea: mensaje + simbolo)
f="${1//\//\\}"
awk -v pat="$1" '
  /error:/ { show = (index($0, pat) > 0); if (show) { sub(/.*createnuclear[\\/]/, ""); print; } next }
  show && /symbol:|location:|required:|found:|reason:/ { print "      " $0 }
' build/compile.txt
