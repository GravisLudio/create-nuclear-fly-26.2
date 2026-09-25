#!/usr/bin/env python3
"""Adjust the import block of Java files.

    python tools/imp.py FILE... [-d REGEX]... [-a FQCN]...

-d drops every import line whose imported name matches REGEX (re.search).
-a adds `import FQCN;` unless already present. Static imports: pass "static a.b.C.m".
The file must exist; files are rewritten only when something changed.
"""
import re
import sys


def main(argv):
    files, drops, adds = [], [], []
    i = 0
    while i < len(argv):
        a = argv[i]
        if a == '-d':
            drops.append(re.compile(argv[i + 1])); i += 2
        elif a == '-a':
            adds.append(argv[i + 1]); i += 2
        else:
            files.append(a); i += 1
    for f in files:
        src = open(f, encoding='utf-8').read()
        lines = src.split('\n')
        out = []
        for line in lines:
            m = re.match(r'^import\s+(static\s+)?([\w.*]+)\s*;', line)
            if m and any(d.search(m.group(2)) for d in drops):
                continue
            out.append(line)
        existing = {re.sub(r'\s+', ' ', l.strip()) for l in out if l.startswith('import ')}
        new = [f'import {a};' for a in adds if f'import {a};' not in existing]
        if new:
            # after the last import, or after the package line
            idx = max((k for k, l in enumerate(out) if l.startswith('import ')), default=None)
            if idx is None:
                idx = next(k for k, l in enumerate(out) if l.startswith('package '))
                out[idx + 1:idx + 1] = [''] + new
            else:
                out[idx + 1:idx + 1] = new
        res = '\n'.join(out)
        if res != src:
            open(f, 'w', encoding='utf-8').write(res)
            print('updated', f)


if __name__ == '__main__':
    main(sys.argv[1:])
