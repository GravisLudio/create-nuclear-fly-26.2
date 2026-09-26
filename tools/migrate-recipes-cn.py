"""Migrates the recipe types Connected's migrate-recipes.js does not cover to 26.2 / Create Fly.

Shapes read off Create Fly's data/create/recipe and the 26.2 vanilla jar:
- ingredients are bare strings ("ns:id" / "#ns:tag");
- single-input Create processing (crushing, splashing, and our enriched / snow_powder) takes
  "ingredient", not an "ingredients" list;
- fluids live in "fluid_ingredients" / "fluid_results", counted in droplets (81 per mB);
  a NeoForge tag fluid ingredient becomes {"type": "fluid_tag", "fluid_tag": "#ns:tag"};
- smithing: template/base/addition are strings, an empty template is dropped;
- smelting/blasting: "ingredient" is a string.

Usage: python tools/migrate-recipes-cn.py <recipe dir> [--write]
"""
import json
import pathlib
import sys

MB = 81
SINGLE = {"create:crushing", "create:splashing", "createnuclear:enriched", "createnuclear:snow_powder"}
MULTI = {"create:mixing", "create:compacting"}


def ing(v):
    if isinstance(v, dict):
        if isinstance(v.get("item"), str):
            return v["item"]
        if isinstance(v.get("tag"), str):
            return "#" + v["tag"]
    return v


def is_fluid(v):
    return isinstance(v, dict) and "amount" in v


def fluid_ing(v):
    if v.get("type") == "neoforge:tag":
        return {"type": "fluid_tag", "amount": v["amount"] * MB, "fluid_tag": "#" + v["tag"]}
    if "fluid" in v:
        return {"type": "fluid_stack", "amount": v["amount"] * MB, "fluid": v["fluid"]}
    raise ValueError(f"unknown fluid ingredient {v}")


def migrate(r):
    t = r.get("type")
    if t in SINGLE and "ingredients" in r:
        items = r.pop("ingredients")
        assert len(items) == 1, items
        r["ingredient"] = ing(items[0])
    elif t in MULTI and "ingredients" in r:
        items = r.pop("ingredients")
        fluids = [fluid_ing(i) for i in items if is_fluid(i)]
        r["ingredients"] = [ing(i) for i in items if not is_fluid(i)]
        if not r["ingredients"]:
            del r["ingredients"]
        if fluids:
            r["fluid_ingredients"] = fluids
    elif t == "create:mechanical_crafting":
        r["key"] = {k: ing(v) for k, v in r["key"].items()}
    elif t in ("minecraft:smelting", "minecraft:blasting", "minecraft:smoking", "minecraft:campfire_cooking"):
        r["ingredient"] = ing(r["ingredient"])
    elif t == "minecraft:smithing_transform":
        for k in ("template", "base", "addition"):
            if k in r:
                if r[k] == [] or r[k] is None:
                    del r[k]
                else:
                    r[k] = ing(r[k])
    else:
        return False
    if t in MULTI and "results" in r:
        results = r.pop("results")
        fluids = [{"amount": x["amount"] * MB, "id": x["id"]} for x in results if is_fluid(x)]
        items = [x for x in results if not is_fluid(x)]
        if items:
            r["results"] = items
        if fluids:
            r["fluid_results"] = fluids
    return True


def main():
    root = pathlib.Path(sys.argv[1])
    write = "--write" in sys.argv
    n = 0
    for p in sorted(root.rglob("*.json")):
        r = json.loads(p.read_text(encoding="utf-8"))
        if isinstance(r, dict) and migrate(r):
            n += 1
            if write:
                p.write_text(json.dumps(r, indent=2) + "\n", encoding="utf-8")
    print(f"{n} recipes {'migrated' if write else 'would change'}")


if __name__ == "__main__":
    main()
