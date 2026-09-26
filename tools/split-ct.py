"""Splits 1.21.1 connected-texture sheets into Create Fly's numbered tiles.

Create Fly (since "Update CTModel sprite") no longer reads a <name>_connected.png sheet: each
connection state is its own sprite, <name>_connected/<index>.png, and index 0 is the plain block
texture. The index order comes from Create Fly's CTType classes; the sheet position of each index
comes from Create's old AllCTTypes.getTextureIndex. Both are transcribed below and the mapping was
checked pixel by pixel against Create Fly's own split of Create's sheets (--verify).

Usage:
  python tools/split-ct.py <sheet.png> <omni|rectangle|kryppers> [--write]
  python tools/split-ct.py --verify <old sheet.png> <new tile dir> <type>
Pure Python (zlib only): no Pillow on this machine.
"""
import pathlib
import struct
import sys
import zlib

UP, DOWN, LEFT, RIGHT, TL, TR, BL, BR = (1 << i for i in range(8))


# ---- new index tables (Create Fly CTType subclasses) ----------------------------------------
def omni_new():
    m = {}
    i = 0
    UDL, UDR, ULR, DLR, UDLR = UP | DOWN | LEFT, UP | DOWN | RIGHT, UP | LEFT | RIGHT, DOWN | LEFT | RIGHT, UP | DOWN | LEFT | RIGHT
    TLR, BLR = TL | TR, BL | BR
    order = [UP, DOWN, UP | DOWN, LEFT, UP | LEFT, DOWN | LEFT, UDL, UP | LEFT | TL, DOWN | LEFT | BL,
             RIGHT, UP | RIGHT, DOWN | RIGHT, UDR, UP | RIGHT | TR, DOWN | RIGHT | BR, LEFT | RIGHT, ULR, DLR,
             UDLR, UDLR | TR, UDLR | TL, UDLR | TLR, UDL | BL, UDL | TL, UDL | TL | BL, UDLR | BL,
             UDLR | TR | BL, UDLR | TL | BL, UDLR | TLR | BL, UDR | BR, UDR | TR, UDR | TR | BR, UDLR | BR,
             UDLR | TR | BR, UDLR | TL | BR, UDLR | TLR | BR, ULR | TL, ULR | TR, ULR | TLR, UDLR | BLR,
             UDLR | TR | BLR, UDLR | TL | BLR, UDLR | TLR | BLR, DLR | BL, DLR | BR, DLR | BLR]
    for f in order:
        i += 1
        m[i] = f
    return m


def rect_new():
    order = [DOWN, DOWN | RIGHT, DOWN | LEFT | RIGHT, DOWN | LEFT, UP | DOWN, UP | DOWN | RIGHT,
             UP | DOWN | LEFT | RIGHT, UP | DOWN | LEFT, UP, UP | RIGHT, UP | LEFT | RIGHT, UP | LEFT,
             RIGHT, LEFT | RIGHT, LEFT]
    return {i + 1: f for i, f in enumerate(order)}


# ---- old sheet positions (Create 6.0 / 1.21.1 AllCTTypes) ----------------------------------
def omni_old(f):
    up, down, left, right = bool(f & UP), bool(f & DOWN), bool(f & LEFT), bool(f & RIGHT)
    tl, tr, bl, br = bool(f & TL), bool(f & TR), bool(f & BL), bool(f & BR)
    x = y = 0
    borders = (not up) + (not down) + (not left) + (not right)
    if up: x += 1
    if down: x += 2
    if left: y += 1
    if right: y += 2
    if borders == 0:
        if tr: x += 1
        if tl: x += 2
        if br: y += 2
        if bl: y += 1
    if borders == 1:
        if not right and (tl or bl):
            y, x = 4, -1 + bl + tl * 2
        if not left and (tr or br):
            y, x = 5, -1 + br + tr * 2
        if not down and (tl or tr):
            y, x = 6, -1 + tl + tr * 2
        if not up and (bl or br):
            y, x = 7, -1 + bl + br * 2
    if borders == 2:
        if (up and left and tl) or (down and left and bl) or (up and right and tr) or (down and right and br):
            x += 3
    return x, y


def rect_old(f):
    up, down, left, right = bool(f & UP), bool(f & DOWN), bool(f & LEFT), bool(f & RIGHT)
    x = 2 if left and right else 3 if left else 1 if right else 0
    y = 1 if up and down else 2 if up else 0 if down else 3
    return x, y


TYPES = {
    # type: (sheet grid size, {new index: old (x, y)})
    "omni": (8, {i: omni_old(f) for i, f in omni_new().items()}),
    "rectangle": (4, {i: rect_old(f) for i, f in rect_new().items()}),
    # Not derivable from the two index functions (they agree on the numbers, yet the sheet cells
    # differ): read off Create Fly's split of andesite_cut_layered by pixel comparison.
    "kryppers": (2, {1: (0, 1), 2: (1, 1), 3: (1, 0)}),
}


# ---- minimal PNG io (8-bit, any colour type read; RGBA written) -----------------------------
def read_png(path):
    data = pathlib.Path(path).read_bytes()
    assert data[:8] == b"\x89PNG\r\n\x1a\n", path
    pos, idat, plte, trns = 8, b"", None, None
    while pos < len(data):
        n, t = struct.unpack(">I4s", data[pos:pos + 8])
        body = data[pos + 8:pos + 8 + n]
        if t == b"IHDR":
            w, h, depth, ctype, _, _, interlace = struct.unpack(">IIBBBBB", body)
        elif t == b"PLTE":
            plte = body
        elif t == b"tRNS":
            trns = body
        elif t == b"IDAT":
            idat += body
        pos += 12 + n
    assert interlace == 0, "interlaced PNG"
    raw = zlib.decompress(idat)
    if ctype == 3:
        bpp_bits = depth
        ch = 1
    else:
        assert depth == 8, f"{path}: bit depth {depth}"
        ch = {0: 1, 2: 3, 4: 2, 6: 4}[ctype]
        bpp_bits = 8 * ch
    stride = (w * bpp_bits + 7) // 8
    bpp = max(1, bpp_bits // 8)
    rows, prev, p = [], bytearray(stride), 0
    for _ in range(h):
        ft = raw[p]; line = bytearray(raw[p + 1:p + 1 + stride]); p += 1 + stride
        for i in range(stride):
            a = line[i - bpp] if i >= bpp else 0
            b = prev[i]
            c = prev[i - bpp] if i >= bpp else 0
            if ft == 1: line[i] = (line[i] + a) & 255
            elif ft == 2: line[i] = (line[i] + b) & 255
            elif ft == 3: line[i] = (line[i] + (a + b) // 2) & 255
            elif ft == 4:
                pa, pb, pc = abs(b - c), abs(a - c), abs(a + b - 2 * c)
                line[i] = (line[i] + (a if pa <= pb and pa <= pc else b if pb <= pc else c)) & 255
        rows.append(line); prev = line
    px = []
    for line in rows:
        row = []
        for x in range(w):
            if ctype == 3:
                bit = x * depth
                idx = (line[bit // 8] >> (8 - depth - bit % 8)) & ((1 << depth) - 1)
                r, g, b = plte[idx * 3:idx * 3 + 3]
                a = trns[idx] if trns and idx < len(trns) else 255
            elif ctype == 6: r, g, b, a = line[x * 4:x * 4 + 4]
            elif ctype == 2: r, g, b = line[x * 3:x * 3 + 3]; a = 255
            elif ctype == 4: r = g = b = line[x * 2]; a = line[x * 2 + 1]
            else: r = g = b = line[x]; a = 255
            row.append((r, g, b, a))
        px.append(row)
    return w, h, px


def write_png(path, px):
    h, w = len(px), len(px[0])
    raw = b"".join(b"\x00" + bytes(c for p in row for c in p) for row in px)
    def chunk(t, b):
        return struct.pack(">I", len(b)) + t + b + struct.pack(">I", zlib.crc32(t + b) & 0xFFFFFFFF)
    out = b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", struct.pack(">IIBBBBB", w, h, 8, 6, 0, 0, 0))
    out += chunk(b"IDAT", zlib.compress(raw, 9)) + chunk(b"IEND", b"")
    pathlib.Path(path).write_bytes(out)


def tile(px, grid, x, y):
    t = len(px) // grid
    return [row[x * t:(x + 1) * t] for row in px[y * t:(y + 1) * t]]


def main():
    args = [a for a in sys.argv[1:] if not a.startswith("--")]
    if "--verify" in sys.argv:
        sheet, tiledir, kind = args
        grid, mapping = TYPES[kind]
        _, _, px = read_png(sheet)
        bad = 0
        for i, (x, y) in mapping.items():
            _, _, want = read_png(pathlib.Path(tiledir) / f"{i}.png")
            got = tile(px, grid, x, y)
            same = all((a[3] == 0 and b[3] == 0) or a == b for ra, rb in zip(got, want) for a, b in zip(ra, rb))
            if not same:
                bad += 1
                print(f"MISMATCH index {i} <- sheet ({x},{y})")
        print(f"{len(mapping) - bad}/{len(mapping)} tiles match")
        sys.exit(1 if bad else 0)
    sheet, kind = args
    grid, mapping = TYPES[kind]
    _, _, px = read_png(sheet)
    out = pathlib.Path(sheet).with_suffix("")  # <name>_connected/
    if "--write" in sys.argv:
        out.mkdir(exist_ok=True)
        for i, (x, y) in mapping.items():
            write_png(out / f"{i}.png", tile(px, grid, x, y))
    print(f"{len(mapping)} tiles -> {out}" + ("" if "--write" in sys.argv else " (dry run)"))


if __name__ == "__main__":
    main()
