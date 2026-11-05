"""Draws the 9-slice UI kit used by the widget-based screens.

    python tools/gen_kit.py

One 64x64 texture holds everything a screen needs at runtime:

    (0, 0)   24x24 panel tile   , 8px insets, stretched to any panel size
    (24, 0)  16x16 button tile  , 4px insets
    (0, 24)  18x18 slot cell    , drawn 1:1 at each slot
    (24, 24) 24x24 tooltip tile , 8px insets

Nothing here is screen-specific, so a screen can be resized without new art.
"""

from __future__ import annotations

import os


from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src", "main", "resources", "assets", "berlit", "textures", "gui", "kit.png")

BG = (46, 50, 60)
FG = (53, 57, 68)
EDGE = (104, 110, 124)
EDGE_HI = (150, 159, 178)

BUTTON_BG = (50, 58, 72)
BUTTON_EDGE = (44, 140, 138)

TOOLTIP_BG = (32, 36, 44)
TOOLTIP_EDGE = (72, 80, 96)

CELL_BG = (53, 57, 68)
CELL_EDGE = (152, 154, 162)


def rounded_rect(image, box, fill, edge, radius=1):
    """Flat fill with a 1px edge and cut corners."""
    draw = ImageDraw.Draw(image)
    x0, y0, x1, y1 = box
    draw.rectangle([x0, y0, x1, y1], fill=fill + (255,))
    draw.line([(x0 + radius, y0), (x1 - radius, y0)], fill=edge + (255,))
    draw.line([(x0 + radius, y1), (x1 - radius, y1)], fill=edge + (255,))
    draw.line([(x0, y0 + radius), (x0, y1 - radius)], fill=edge + (255,))
    draw.line([(x1, y0 + radius), (x1, y1 - radius)], fill=edge + (255,))
    for cx, cy in ((x0, y0), (x1, y0), (x0, y1), (x1, y1)):
        draw.point((cx, cy), fill=fill + (255,))


def main():
    image = Image.new("RGBA", (64, 64), (0, 0, 0, 0))

    rounded_rect(image, (0, 0, 23, 23), BG, EDGE, radius=1)
    # one highlight pixel inside the top edge so large panels read as lit
    ImageDraw.Draw(image).point((4, 1), fill=EDGE_HI + (255,))

    rounded_rect(image, (24, 0, 39, 15), BUTTON_BG, BUTTON_EDGE, radius=1)
    ImageDraw.Draw(image).line([(26, 1), (37, 1)], fill=(58, 100, 100, 255))

    rounded_rect(image, (0, 24, 17, 41), CELL_BG, CELL_EDGE, radius=1)

    rounded_rect(image, (24, 24, 47, 47), TOOLTIP_BG, TOOLTIP_EDGE, radius=1)

    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    image.save(OUT)
    print("written", OUT, image.size)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
