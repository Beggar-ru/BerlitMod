"""Generates the solid matter item icons and generator casings (8 kinds each)."""

from __future__ import annotations

import os

from PIL import Image, ImageDraw

ROOT = r"C:\main_mod\src\main\resources\assets\berlit\textures"
OUT_ITEMS = os.path.join(ROOT, "items")
OUT_BLOCKS = os.path.join(ROOT, "blocks")

MATTER = [
    ("standard", (232, 236, 242)),
    ("violet", (168, 124, 224)),
    ("turquoise", (79, 214, 205)),
    ("emerald", (91, 217, 122)),
    ("azure", (91, 143, 224)),
    ("amber", (232, 180, 79)),
    ("crimson", (224, 96, 91)),
    ("radiant", (255, 242, 176)),
]

OUTLINE = (18, 21, 26)
CASING = (46, 51, 64)
CASING_EDGE = (20, 23, 29)
CASING_INNER = (54, 59, 72)
CYAN = (44, 140, 138)


def shade(colour, factor):
    return tuple(max(0, min(255, int(channel * factor))) for channel in colour)


def draw_crystal(image, colour):
    """16x16 rhombus with a light top-left facet and a dark bottom-right one."""
    draw = ImageDraw.Draw(image)
    centre_x, centre_y, radius = 8, 8, 6
    body = colour
    light = shade(colour, 1.25)
    dark = shade(colour, 0.62)

    for y in range(16):
        for x in range(16):
            distance = abs(x - centre_x) + abs(y - centre_y)
            if distance > radius:
                continue
            if distance == radius:
                draw.point((x, y), fill=OUTLINE + (255,))
            elif x - centre_x < y - centre_y:
                draw.point((x, y), fill=dark + (255,))
            elif x - centre_x > y - centre_y:
                draw.point((x, y), fill=light + (255,))
            else:
                draw.point((x, y), fill=body + (255,))

    # small sparkle on the upper facet
    draw.point((6, 5), fill=(255, 255, 255, 230))
    draw.point((7, 4), fill=(255, 255, 255, 160))


def draw_casing(image, colour):
    """16x16 machine casing with a coloured core, matching the Berlit slate palette."""
    draw = ImageDraw.Draw(image)
    draw.rectangle([0, 0, 15, 15], fill=CASING + (255,))
    draw.rectangle([0, 0, 15, 15], outline=CASING_EDGE + (255,))
    draw.rectangle([2, 2, 13, 13], fill=CASING_INNER + (255,))

    # cyan rule along the top edge
    draw.line([(1, 1), (14, 1)], fill=CYAN + (255,))

    # coloured core
    for y in range(16):
        for x in range(16):
            distance = abs(x - 8) + abs(y - 8)
            if distance > 3:
                continue
            if distance == 3:
                draw.point((x, y), fill=CASING_EDGE + (255,))
            elif x - 8 < y - 8:
                draw.point((x, y), fill=shade(colour, 0.7) + (255,))
            else:
                draw.point((x, y), fill=colour + (255,))

    draw.point((7, 6), fill=(255, 255, 255, 200))


def main():
    os.makedirs(OUT_ITEMS, exist_ok=True)
    os.makedirs(OUT_BLOCKS, exist_ok=True)

    for name, colour in MATTER:
        item = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
        draw_crystal(item, colour)
        item.save(os.path.join(OUT_ITEMS, f"matter_{name}.png"))

        block = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
        draw_casing(block, colour)
        block.save(os.path.join(OUT_BLOCKS, f"matter_generator_{name}.png"))

    print(f"written 8 item icons and 8 casings")


if __name__ == "__main__":
    main()
