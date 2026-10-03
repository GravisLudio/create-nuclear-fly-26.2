#!/usr/bin/env python3
"""Draws the mod's own spawn egg textures (16x16): an egg in radioactive greens with the face of
the animal on it. Original artwork, not derived from Mojang's eggs.

    gen-spawn-eggs.py            dry run: writes only a 16x preview sheet next to this script's output
    gen-spawn-eggs.py --write    writes src/main/resources/assets/createnuclear/textures/item/*.png

Edit the FACES grids to change a face; '.' keeps the shaded egg, any other letter is a PALETTE colour.
"""
import sys
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
OUT = ROOT / "src/main/resources/assets/createnuclear/textures/item"
PREVIEW = Path(sys.argv[sys.argv.index("--preview") + 1]) if "--preview" in sys.argv else None

PALETTE = {
    "O": (0x16, 0x30, 0x0E),  # outline
    "D": (0x2C, 0x6A, 0x1B),  # shadow
    "M": (0x4E, 0xA5, 0x2C),  # body
    "L": (0x86, 0xDC, 0x4A),  # light
    "H": (0xC6, 0xFF, 0x86),  # highlight
    "K": (0x0C, 0x16, 0x08),  # dark features
    "Y": (0xF0, 0xFF, 0x5C),  # glow
    "W": (0xD8, 0xF5, 0xB0),  # pale fur / feathers
    "G": (0x9B, 0xBF, 0x78),  # grey-green fur
    "P": (0xC8, 0x7A, 0xA0),  # nose
    "B": (0xE3, 0xB0, 0x2C),  # beak
    "R": (0xC6, 0x3A, 0x2A),  # wattle
}

# Width of the egg on rows 1..14, symmetric about the line between columns 7 and 8.
WIDTHS = [4, 6, 8, 8, 10, 10, 12, 12, 12, 12, 10, 10, 8, 6]


def base_egg():
    """The shaded egg, as {(x, y): letter}."""
    inside = set()
    for i, w in enumerate(WIDTHS):
        y = i + 1
        for x in range(8 - w // 2, 8 + w // 2):
            inside.add((x, y))
    px = {}
    for (x, y) in inside:
        edge = any((x + dx, y + dy) not in inside for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
        if edge:
            px[(x, y)] = "O"
            continue
        d = ((x - 5.2) ** 2 + (y - 4.2) ** 2) ** 0.5  # light from the top left
        px[(x, y)] = "H" if d < 2.2 else "L" if d < 4.8 else "M" if d < 8.2 else "D"
    return px


# Glowing specks on the lower half: the family resemblance between the three eggs.
SPECKS = {(5, 11): "Y", (10, 12): "Y", (8, 13): "L", (4, 9): "L", (11, 10): "L"}

FACES = {
    "wolf": [
        "...O........O...",
        "..OGO......OGO..",
        "..OGDO....ODGO..",
        "..OGGDO..ODGGO..",
        "...GGGGGGGGGG...",
        "....GGGGGGGG....",
        "....GKYGGYKG....",
        "....GGWWWWGG....",
        ".....GWKKWG.....",
        ".....WWWWWW.....",
        "......WKKW......",
        "................",
        "................",
        "................",
        "................",
        "................",
    ],
    "cat": [
        "................",
        "...O........O...",
        "..OGO......OGO..",
        "..OGGO....OGGO..",
        "...GGGGGGGGGG...",
        "....GGGGGGGG....",
        "....GKYGGYKG....",
        "....GGGGGGGG....",
        "...WGGGPPGGGW...",
        "....WGGKKGGW....",
        ".....WWGGWW.....",
        "................",
        "................",
        "................",
        "................",
        "................",
    ],
    "chicken": [
        "................",
        ".......RR.......",
        "......RRRR......",
        "......WWWW......",
        ".....WWWWWW.....",
        "....WWKWWKWW....",
        "....WWYWWYWW....",
        "....WWWBBWWW....",
        ".....WWBBWW.....",
        ".....WWRRWW.....",
        "......WRRW......",
        ".......RR.......",
        "................",
        "................",
        "................",
        "................",
    ],
}


def draw(name):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = base_egg()
    for pos, ch in SPECKS.items():
        if pos in px and px[pos] != "O":
            px[pos] = ch
    for y, row in enumerate(FACES[name]):
        assert len(row) == 16, (name, y, len(row))
        for x, ch in enumerate(row):
            if ch != ".":
                px[(x, y)] = ch
    for (x, y), ch in px.items():
        img.putpixel((x, y), PALETTE[ch] + (255,))
    return img


def main():
    write = "--write" in sys.argv
    eggs = {n: draw(n) for n in FACES}
    if write:
        OUT.mkdir(parents=True, exist_ok=True)
        for n, img in eggs.items():
            img.save(OUT / f"{n}_irradiated_spawn_egg.png")
    if PREVIEW:
        scale, pad = 16, 16
        sheet = Image.new("RGBA", (len(eggs) * (16 * scale + pad) + pad, 16 * scale + 2 * pad), (110, 110, 120, 255))
        for i, img in enumerate(eggs.values()):
            big = img.resize((16 * scale, 16 * scale), Image.NEAREST)
            sheet.paste(big, (pad + i * (16 * scale + pad), pad), big)
        sheet.save(PREVIEW)
    print(f"{len(eggs)} eggs" + (f" written to {OUT}" if write else " (dry run; pass --write)"))


if __name__ == "__main__":
    main()
