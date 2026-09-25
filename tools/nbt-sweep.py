#!/usr/bin/env python3
"""CompoundTag getters in 26.2 return Optional; the old defaulting behaviour is the *Or variant.

Rewrites `.getInt("key")` -> `.getIntOr("key", 0)` and friends, but only where the key is a string
literal or an UPPER_CASE constant and only in files that import CompoundTag -- ValueInput has the
same method names with different meaning, so anything less specific is left for a human.

    python tools/nbt-sweep.py src/main/java [--write]
"""
import re
import sys
from pathlib import Path

KEY = r'("(?:[^"\\]|\\.)*"|[A-Z][A-Z0-9_]*(?:\.[A-Z][A-Z0-9_]*)?)'
RULES = [
    (rf'\.getInt\({KEY}\)', r'.getIntOr(\1, 0)'),
    (rf'\.getLong\({KEY}\)', r'.getLongOr(\1, 0L)'),
    (rf'\.getFloat\({KEY}\)', r'.getFloatOr(\1, 0f)'),
    (rf'\.getDouble\({KEY}\)', r'.getDoubleOr(\1, 0d)'),
    (rf'\.getBoolean\({KEY}\)', r'.getBooleanOr(\1, false)'),
    (rf'\.getString\({KEY}\)', r'.getStringOr(\1, "")'),
    (rf'\.getShort\({KEY}\)', r'.getShortOr(\1, (short) 0)'),
    (rf'\.getByte\({KEY}\)', r'.getByteOr(\1, (byte) 0)'),
    (rf'\.getCompound\({KEY}\)', r'.getCompoundOrEmpty(\1)'),
    (rf'\.getList\({KEY},\s*[^)]+\)', r'.getListOrEmpty(\1)'),
    # ListTag.getCompound(int)
    (r'\.getCompound\((i|j|k|index|idx)\)', r'.getCompoundOrEmpty(\1)'),
]


def main():
    root = Path(sys.argv[1])
    write = '--write' in sys.argv
    rules = [(re.compile(p), r) for p, r in RULES]
    total = 0
    for f in root.rglob('*.java'):
        s = open(f, encoding='utf-8').read()
        if 'import net.minecraft.nbt.CompoundTag;' not in s and 'import net.minecraft.nbt.*;' not in s:
            continue
        new = s
        n_file = 0
        for rx, rep in rules:
            new, n = rx.subn(rep, new)
            n_file += n
        if n_file:
            total += n_file
            print(f'{n_file:4d} {f}')
            if write:
                open(f, 'w', encoding='utf-8').write(new)
    print('total', total)


if __name__ == '__main__':
    main()
