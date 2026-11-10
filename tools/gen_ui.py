#!/usr/bin/env python3
"""Generates Lumina's restyled UI nine-patches from Mindustry's raw UI sprites.

Every output keeps the exact size and silhouette of the vanilla sprite, so the
vanilla nine-patch splits and paddings (reused at runtime) stay valid.

usage: gen_ui.py <Mindustry>/core/assets-raw/sprites/ui assets/lumina-ui
"""
import glob
import os
import sys

from PIL import Image

GRAY, ACCENT, WHITE, RED, LIGHT = 'gray', 'accent', 'white', 'red', 'light'

# vanilla border colours -> (kind, restyled line colour)
BANDS = {
    (0x45, 0x45, 0x45): (GRAY, (86, 92, 108)),
    (0xff, 0xd3, 0x7f): (ACCENT, (255, 211, 127)),
    (0xff, 0xff, 0xff): (WHITE, (255, 255, 255)),
    (0xe5, 0x54, 0x54): (RED, (232, 88, 88)),
    (0x98, 0x9a, 0xa4): (LIGHT, (152, 156, 170)),
    (0xc1, 0xc4, 0xcf): (WHITE, (196, 199, 210)),
}

# light comes from the top left
SIDE_LIGHT = {'top': 1.14, 'left': 1.04, 'right': 0.90, 'bottom': 0.80, 'flat': 1.0}


def lerp(a, b, t):
    return a + (b - a) * t


def mix(c1, c2, t):
    return tuple(lerp(c1[i], c2[i], t) for i in range(3))


def clamp8(v):
    return max(0, min(255, int(round(v))))


def restyle(im):
    w, h = im.size
    src = im.load()
    out = Image.new('RGBA', (w, h), (0, 0, 0, 0))
    dst = out.load()

    def kind(x, y):
        r, g, b, a = src[x, y]
        if a == 0:
            return 'clear'
        if (r, g, b) in BANDS:
            return 'band'
        return 'body'

    kinds = [[kind(x, y) for x in range(w)] for y in range(h)]
    has_body = any(k == 'body' for row in kinds for k in row)
    has_band = any(k == 'band' for row in kinds for k in row)
    solid = not has_body and kinds[h // 2][w // 2] == 'band'

    # an image side is an outer edge when a border runs along it, rather than being cut by it
    def band_frac(cells):
        return sum(1 for c in cells if c == 'band') / len(cells)

    outer = {
        'top': band_frac(kinds[0]) >= 0.5,
        'bottom': band_frac(kinds[h - 1]) >= 0.5,
        'left': band_frac([kinds[y][0] for y in range(h)]) >= 0.5,
        'right': band_frac([kinds[y][w - 1] for y in range(h)]) >= 0.5,
    }

    band_kinds = {BANDS[src[x, y][:3]][0] for y in range(h) for x in range(w) if kinds[y][x] == 'band'}

    def edge_dist(x, y):
        """Distance from a border pixel to the nearest pixel that is not border."""
        best = 99.0
        for yy in range(h):
            for xx in range(w):
                if kinds[yy][xx] != 'band':
                    best = min(best, max(abs(xx - x), abs(yy - y)))
        if outer['top']:
            best = min(best, y + 1)
        if outer['bottom']:
            best = min(best, h - y)
        if outer['left']:
            best = min(best, x + 1)
        if outer['right']:
            best = min(best, w - x)
        return best

    def side(x, y):
        if not has_body and not solid:
            return 'flat'
        dx = (x - (w - 1) / 2) / w
        dy = (y - (h - 1) / 2) / h
        if abs(dy) >= abs(dx):
            return 'top' if dy < 0 else 'bottom'
        return 'left' if dx < 0 else 'right'

    for y in range(h):
        t = y / max(1, h - 1)
        for x in range(w):
            r, g, b, a = src[x, y]
            k = kinds[y][x]
            if k == 'clear':
                continue

            if k == 'body':
                if (r, g, b) == (0x25, 0x25, 0x25):
                    # disabled buttons: flat, slightly cool dark grey
                    col = mix((34, 36, 43), (24, 25, 30), t)
                    alpha = a
                elif a == 255:
                    col = mix((31, 35, 47), (13, 15, 21), t)
                    alpha = 255
                else:
                    col = mix((20, 23, 32), (5, 6, 9), t)
                    alpha = clamp8(a * 1.17)
                if has_band:
                    if ACCENT in band_kinds:
                        col = mix(col, (255, 211, 127), 0.12 * (1 - t))
                    elif WHITE in band_kinds and has_body:
                        col = mix(col, (255, 255, 255), 0.10 * (1 - t))
                dst[x, y] = (clamp8(col[0]), clamp8(col[1]), clamp8(col[2]), alpha)
                continue

            bkind, base = BANDS[(r, g, b)]
            if solid:
                # tintable filled shapes (bars): soft gloss, darker rim
                shade = lerp(1.0, 0.84, t)
                rim = any(
                    not (0 <= x + ox < w and 0 <= y + oy < h) or kinds[y + oy][x + ox] == 'clear'
                    for ox, oy in ((1, 0), (-1, 0), (0, 1), (0, -1))
                )
                if rim:
                    shade *= 0.74
                dst[x, y] = tuple(clamp8(c * shade) for c in base) + (a,)
                continue

            d = edge_dist(x, y)
            if d <= 1:
                factor = 0.30 if bkind == GRAY else 0.42
                col = tuple(c * factor for c in base)
            else:
                col = tuple(c * SIDE_LIGHT[side(x, y)] for c in base)
            dst[x, y] = (clamp8(col[0]), clamp8(col[1]), clamp8(col[2]), a)

    return out


def restyle_check(im, checked):
    """Checkboxes: steel ring, and a glowing accent centre when checked."""
    w, h = im.size
    src = im.load()
    out = Image.new('RGBA', (w, h), (0, 0, 0, 0))
    dst = out.load()
    cx, cy = (w - 1) / 2, (h - 1) / 2

    for y in range(h):
        t = y / (h - 1)
        for x in range(w):
            r, g, b, a = src[x, y]
            if a == 0:
                continue
            if (r, g, b) not in BANDS:
                dst[x, y] = (16, 18, 25, a)
                continue

            centre = max(abs(x - cx), abs(y - cy)) <= 8.5
            bkind, base = BANDS[(r, g, b)]
            if centre and checked:
                col = mix((255, 232, 170), (236, 172, 84), t)
            else:
                rim = any(
                    not (0 <= x + ox < w and 0 <= y + oy < h) or src[x + ox, y + oy][3] == 0
                    for ox, oy in ((1, 0), (-1, 0), (0, 1), (0, -1))
                )
                col = tuple(c * (0.5 if rim else lerp(1.14, 0.82, t)) for c in base)
            dst[x, y] = (clamp8(col[0]), clamp8(col[1]), clamp8(col[2]), a)
    return out


def main():
    src_dir, out_dir = sys.argv[1], sys.argv[2]
    os.makedirs(out_dir, exist_ok=True)
    count = 0

    # plain (non nine-patch) widgets
    for name in ('check-on', 'check-on-over', 'check-off', 'check-over'):
        im = Image.open(os.path.join(src_dir, name + '.png')).convert('RGBA')
        restyle_check(im, name.startswith('check-on')).save(os.path.join(out_dir, name + '.png'))
        count += 1
    for name in ('slider-knob', 'slider-knob-over', 'slider-knob-down'):
        im = Image.open(os.path.join(src_dir, name + '.png')).convert('RGBA')
        restyle(im).save(os.path.join(out_dir, name + '.png'))
        count += 1

    for path in sorted(glob.glob(os.path.join(src_dir, '*.9.png'))):
        im = Image.open(path).convert('RGBA')
        inner = im.crop((1, 1, im.width - 1, im.height - 1))
        if inner.getextrema()[3][1] == 0:
            continue  # fully transparent spacer
        name = os.path.basename(path)[:-len('.9.png')]
        restyle(inner).save(os.path.join(out_dir, name + '.png'))
        count += 1
    print('wrote', count, 'sprites to', out_dir)


if __name__ == '__main__':
    main()
