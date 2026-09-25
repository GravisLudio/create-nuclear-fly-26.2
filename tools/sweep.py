#!/usr/bin/env python3
"""Mechanical 1.21.1 -> 26.2 renames that are safe to apply everywhere.

Each rule is a regex with a count reported before writing, so a rule that matches nothing (or far
too much) is visible. Run with --write to apply.

    python tools/sweep.py src/main/java [--write]
"""
import re
import sys
from pathlib import Path

RULES = [
    # Annotation gone from net.minecraft
    (r'^import net\.minecraft\.MethodsReturnNonnullByDefault;\n', ''),
    (r'^\s*@MethodsReturnNonnullByDefault\s*\n', ''),
    (r'@MethodsReturnNonnullByDefault\s*', ''),
    # Level.isClientSide is a private field now, read through the getter
    (r'\.isClientSide\b(?!\s*\()', '.isClientSide()'),
    # Level.random is protected; the getter is public
    (r'\b((?:p?[lL]evel|world|worldIn|pLevel|serverLevel|serverlevel|clientLevel|this\.level|level\(\)|getLevel\(\)|entity\.level\(\)|player\.level\(\))\.)random\b(?!\s*\()', r'\1getRandom()'),
    # Registry lookups
    (r'\.registryOrThrow\(', '.lookupOrThrow('),
    (r'\.getHolderOrThrow\(', '.getOrThrow('),
    # ResourceKey.location() -> identifier()
    (r'(Key\(\)|[kK]ey)\.location\(\)', r'\1.identifier()'),
    # ChunkPos is a record
    (r'new ChunkPos\(([^,()]+(?:\([^()]*\))?)\)', r'ChunkPos.containing(\1)'),
    # Heights
    (r'\.getMinBuildHeight\(\)', '.getMinY()'),
    (r'\.getMaxBuildHeight\(\)', '.getMaxY() + 1'),
]


def main():
    root = Path(sys.argv[1])
    write = '--write' in sys.argv
    compiled = [(re.compile(p, re.M), r) for p, r in RULES]
    totals = [0] * len(RULES)
    for f in root.rglob('*.java'):
        s = open(f, encoding='utf-8').read()
        new = s
        for k, (rx, rep) in enumerate(compiled):
            new, n = rx.subn(rep, new)
            totals[k] += n
        if new != s and write:
            open(f, 'w', encoding='utf-8').write(new)
    for (p, _), n in zip(RULES, totals):
        print(f'{n:5d}  {p}')


if __name__ == '__main__':
    main()
