"""Writes the 26.2 item model definitions (assets/createnuclear/items/<id>.json).

26.2 no longer finds an item's model from models/item/<id>.json by name: every item needs a
definition file. This makes one per top-level models/item/*.json (in src/generated and src/main),
pointing at that model, and replaces the removed `overrides` + `predicate` format for the two
items that used it:

- the anti-radiation suit pieces select on the createnuclear:cloth_color component (its codec
  writes the dye name, or "default"), using the models under models/item/colored/;
- the biome irradiation extractor uses the createnuclear:biome_restore range property
  (charge / max charge, registered in client/CNItemModelProperties).

Usage: python tools/gen-item-models.py [--write]
"""
import json
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
NS = "createnuclear"
MODEL_DIRS = [ROOT / f"src/{s}/resources/assets/{NS}/models/item" for s in ("generated", "main")]
OUT = ROOT / f"src/main/resources/assets/{NS}/items"

DYES = ["white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
        "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"]
EGG_TINT = 0xFFA8FF8C - (1 << 32)  # ARGB as a signed int, as the codec reads it
SUIT = {"default_anti_radiation_" + p: p for p in ("helmet", "chestplate", "leggings", "boots")}


def plain(model):
    return {"type": "minecraft:model", "model": model}


def definition(name):
    if name in SUIT:
        piece = SUIT[name]
        return {"model": {
            "type": "minecraft:select",
            "property": "minecraft:component",
            "component": f"{NS}:cloth_color",
            "cases": [{"when": d, "model": plain(f"{NS}:item/colored/{d}_anti_radiation_{piece}")} for d in DYES],
            "fallback": plain(f"{NS}:item/{name}"),
        }}
    if name == "biome_irradiation_extractor":
        tiers = [(0.25, "quarter"), (0.5, "half"), (0.75, "three_quarters"), (1.0, "full")]
        return {"model": {
            "type": "minecraft:range_dispatch",
            "property": f"{NS}:biome_restore",
            "entries": [{"threshold": t, "model": plain(f"{NS}:item/{name}/{m}")} for t, m in tiers],
            "fallback": plain(f"{NS}:item/{name}"),
        }}
    if name.endswith("_irradiated_spawn_egg"):
        # 26.2 eggs have one texture per entity and no two-colour template. Until the mod has its own
        # egg textures, the model reuses the vanilla egg of the same animal (models/item/<name>.json)
        # and this tints it radioactive green; item/generated gives layer0 tint index 0.
        return {"model": dict(plain(f"{NS}:item/{name}"), tints=[{"type": "minecraft:constant", "value": EGG_TINT}])}
    return {"model": plain(f"{NS}:item/{name}")}


def main():
    write = "--write" in sys.argv
    names = sorted({p.stem for d in MODEL_DIRS if d.is_dir() for p in d.glob("*.json")})
    if write:
        OUT.mkdir(parents=True, exist_ok=True)
    for name in names:
        text = json.dumps(definition(name), indent=2) + "\n"
        if write:
            (OUT / f"{name}.json").write_text(text, encoding="utf-8")
    print(f"{len(names)} item definitions" + ("" if write else " (dry run; pass --write)"))


if __name__ == "__main__":
    main()
