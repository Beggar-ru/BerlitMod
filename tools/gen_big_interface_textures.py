"""Builds the ME Interface 18/27/36 block faces in the Berlit palette.

Each block gets the same casing with a tier strip at the bottom: one, two or three cyan pips, so
they stay distinguishable at 16x16 while reading as one family.
"""

from __future__ import annotations

import os

from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
BLOCKS = os.path.join(ROOT, "src", "main", "resources", "assets", "berlit", "textures", "blocks")

CASING = (46, 51, 64)
EDGE = (20, 23, 29)
INNER = (54, 59, 72)
CYAN = (44, 140, 138)
LIGHT = (111, 233, 224)

TIERS = (("big_interface_18", 1), ("big_interface_27", 2), ("big_interface_36", 3))


def face(pips):
    image = Image.new("RGBA", (16, 16), CASING + (255,))
    draw = ImageDraw.Draw(image)

    draw.rectangle([0, 0, 15, 15], outline=EDGE + (255,))
    draw.rectangle([2, 2, 13, 13], fill=INNER + (255,), outline=CYAN + (255,))

    # corner brackets, like the GUI frames
    for x, y, dx, dy in ((3, 3, 1, 1), (12, 3, -1, 1), (3, 12, 1, -1), (12, 12, -1, -1)):
        draw.line([(x, y), (x + dx * 3, y)], fill=LIGHT + (255,))
        draw.line([(x, y), (x, y + dy * 3)], fill=LIGHT + (255,))

    # interface slot hint: a small port in the middle
    draw.rectangle([6, 5, 9, 8], fill=(28, 32, 40, 255), outline=CYAN + (255,))
    draw.point((7, 6), fill=LIGHT + (255,))

    # tier pips along the bottom edge
    start = 8 - (pips * 3 - 1) // 2
    for index in range(pips):
        x = start + index * 3
        draw.rectangle([x, 10, x + 1, 11], fill=LIGHT + (255,))

    return image


def main():
    os.makedirs(BLOCKS, exist_ok=True)
    for name, pips in TIERS:
        face(pips).save(os.path.join(BLOCKS, name + ".png"))
    print("written 3 big interface faces")


if __name__ == "__main__":
    main()
