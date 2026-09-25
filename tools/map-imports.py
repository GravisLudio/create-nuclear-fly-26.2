#!/usr/bin/env python3
"""Rewrite Create / Catnip / Ponder / Flywheel imports to their Create Fly packages.

Resolves every import against the class list of the Create Fly jar actually compiled against,
so a mapping is only written when the target class exists. Everything it could not resolve is
reported, never guessed.

    python tools/map-imports.py <create-fly.jar> <src-root> [--write]
"""
import re
import sys
import zipfile
from collections import defaultdict
from pathlib import Path

PREFIXES = [
    ("com.simibubi.create.", ["com.zurrtum.create.", "com.zurrtum.create.client."]),
    ("net.createmod.catnip.", ["com.zurrtum.create.catnip.", "com.zurrtum.create.client.catnip."]),
    ("net.createmod.ponder.", ["com.zurrtum.create.ponder.", "com.zurrtum.create.client.ponder."]),
    ("dev.engine_room.flywheel.", ["com.zurrtum.create.client.flywheel."]),
]

IMPORT_RE = re.compile(r"^import\s+(static\s+)?([\w.]+)(\.\*)?\s*;", re.M)


def load_classes(jar):
    classes = set()
    with zipfile.ZipFile(jar) as z:
        for n in z.namelist():
            if n.endswith(".class"):
                classes.add(n[:-6].replace("/", ".").replace("$", "."))
    by_simple = defaultdict(list)
    for c in classes:
        by_simple[c.rsplit(".", 1)[-1]].append(c)
    return classes, by_simple


def resolve(fqn, classes, by_simple):
    """Return the new FQN, or None. Keeps any trailing member (static import) intact."""
    for old, news in PREFIXES:
        if not fqn.startswith(old):
            continue
        rest = fqn[len(old):]
        parts = rest.split(".")
        # longest prefix of `rest` that is a class, trying each target package
        for new in news:
            for k in range(len(parts), 0, -1):
                cand = new + ".".join(parts[:k])
                if cand in classes:
                    return ".".join([cand] + parts[k:])
        # fall back to a unique simple-name match on the first capitalised segment
        for k, p in enumerate(parts):
            if p[:1].isupper():
                hits = [h for h in by_simple.get(p, []) if h.startswith("com.zurrtum.create.")]
                if len(hits) == 1:
                    return ".".join([hits[0]] + parts[k + 1:])
                return None
        return None
    return fqn  # not ours to map


def main():
    jar, root = sys.argv[1], Path(sys.argv[2])
    write = "--write" in sys.argv
    classes, by_simple = load_classes(jar)
    unresolved = defaultdict(set)
    changed = 0
    for f in root.rglob("*.java"):
        text = f.read_text(encoding="utf-8")

        def sub(m):
            static, fqn, star = m.group(1) or "", m.group(2), m.group(3) or ""
            if not any(fqn.startswith(o) for o, _ in PREFIXES):
                return m.group(0)
            new = resolve(fqn, classes, by_simple)
            if new is None:
                unresolved[fqn].add(str(f.relative_to(root)))
                return m.group(0)
            return f"import {static}{new}{star};"

        new_text = IMPORT_RE.sub(sub, text)
        if new_text != text:
            changed += 1
            if write:
                f.write_text(new_text, encoding="utf-8")
    print(f"files changed: {changed}{'' if write else ' (dry run)'}")
    print(f"unresolved imports: {len(unresolved)}")
    for fqn in sorted(unresolved):
        print(f"  {fqn}  <- {len(unresolved[fqn])} file(s)")


if __name__ == "__main__":
    main()
