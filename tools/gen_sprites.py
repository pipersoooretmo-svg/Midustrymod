#!/usr/bin/env python3
"""Generates Lumina's own sprites: the soft glow used by the menu backdrop and the mod icon.

usage: gen_sprites.py <repo root>
"""
import math
import os
import sys

from PIL import Image, ImageDraw, ImageFilter


def glow(size=64):
    im = Image.new('RGBA', (size, size), (255, 255, 255, 0))
    px = im.load()
    c = (size - 1) / 2
    for y in range(size):
        for x in range(size):
            d = min(1.0, math.hypot(x - c, y - c) / c)
            a = (1 - d) ** 2.2
            px[x, y] = (255, 255, 255, int(round(a * 255)))
    return im


def icon(size=128, scale=4):
    s = size * scale
    base = Image.new('RGBA', (s, s), (0, 0, 0, 0))
    d = ImageDraw.Draw(base)

    # dark chamfered tile with a gold rim, matching the in-game theme
    ch = s * 0.16
    m = s * 0.04
    shape = [(m + ch, m), (s - m - ch, m), (s - m, m + ch), (s - m, s - m - ch),
             (s - m - ch, s - m), (m + ch, s - m), (m, s - m - ch), (m, m + ch)]
    d.polygon(shape, fill=(255, 211, 127, 255))
    inset = s * 0.05
    inner = [(x + (inset if x < s / 2 else -inset), y + (inset if y < s / 2 else -inset)) for x, y in shape]
    d.polygon(inner, fill=(17, 20, 29, 255))

    # glowing four-pointed star
    star = Image.new('RGBA', (s, s), (0, 0, 0, 0))
    sd = ImageDraw.Draw(star)
    c = s / 2
    long, short, w = s * 0.36, s * 0.2, s * 0.055
    for length, ang in ((long, 0), (long, 90), (long, 180), (long, 270), (short, 45), (short, 135), (short, 225), (short, 315)):
        a = math.radians(ang)
        tip = (c + math.cos(a) * length, c + math.sin(a) * length)
        left = (c + math.cos(a + math.pi / 2) * w, c + math.sin(a + math.pi / 2) * w)
        right = (c + math.cos(a - math.pi / 2) * w, c + math.sin(a - math.pi / 2) * w)
        sd.polygon([tip, left, right], fill=(255, 226, 160, 255))
    sd.ellipse([c - w * 1.5, c - w * 1.5, c + w * 1.5, c + w * 1.5], fill=(255, 250, 235, 255))

    halo = star.filter(ImageFilter.GaussianBlur(s * 0.05))
    halo_px = halo.load()
    for y in range(s):
        for x in range(s):
            r, g, b, a = halo_px[x, y]
            halo_px[x, y] = (246, 128, 33, min(255, int(a * 1.5)))

    # keep the glow inside the tile
    mask = Image.new('L', (s, s), 0)
    ImageDraw.Draw(mask).polygon(inner, fill=255)
    base.paste(Image.alpha_composite(base, halo), (0, 0), mask)
    base = Image.alpha_composite(base, star)
    return base.resize((size, size), Image.LANCZOS)


def main():
    root = sys.argv[1]
    os.makedirs(os.path.join(root, 'assets', 'sprites', 'ui'), exist_ok=True)
    # placed under sprites/ui so it lands on the UI atlas page and batches with the interface
    glow().save(os.path.join(root, 'assets', 'sprites', 'ui', 'glow.png'))
    icon().save(os.path.join(root, 'icon.png'))
    print('wrote glow.png and icon.png')


if __name__ == '__main__':
    main()
