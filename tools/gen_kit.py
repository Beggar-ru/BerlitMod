"""Draws the 9-slice UI kit in the machine theme (slate + cyan).

    python tools/gen_kit.py

One 64x64 texture holds everything a widget screen needs at runtime:

    (0, 0)   24x24 panel   , 8px insets - dark rim, cyan band, inner shadow
    (24, 0)  16x16 button  , 4px insets - dark plate, cyan edge
    (0, 24)  18x18 cell    , drawn 1:1 per slot - bevel like the machine slots
    (24, 24) 24x24 tooltip , 8px insets

The panel's border ring matches gui_theme's machine panels; the interior is flat
because a 9-slice stretches one patch, so the Gui lays a gradient over it.
"""

from __future__ import annotations

import os

from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src", "main", "resources", "assets", "berlit", "textures", "gui", "kit.png")

DARK_RIM = (12, 14, 18)
CYAN = (44, 140, 138)
CYAN_LIGHT = (111, 233, 224)
SHADOW_1 = (36, 40, 50)
SHADOW_2 = (44, 48, 60)
FILL = (48, 52, 63)

BUTTON_FILL = (50, 58, 72)
BUTTON_HI = (74, 82, 100)

CELL_FILL = (58, 63, 75)
CELL_DARK = (30, 34, 42)
CELL_LIGHT = (86, 96, 116)

TOOLTIP_FILL = (28, 32, 40)


def draw_panel(image, x0, y0, size):
    """24x24 panel *ring* only: the middle stays transparent.

    A 9-slice stretches one patch, so a filled tile would flatten the gradient the
    machine panels have. The Gui paints the gradient first and then lays this ring
    on top, which keeps the frame identical to gui_theme's machine panels.
    """
    draw = ImageDraw.Draw(image)
    top_colours = (DARK_RIM, CYAN, CYAN, DARK_RIM, SHADOW_1, SHADOW_2)
    bottom_colours = (DARK_RIM, CYAN, CYAN, DARK_RIM, (70, 77, 92), (58, 64, 78))

    for offset in range(6):
        top = top_colours[offset]
        bottom = bottom_colours[offset]
        draw.line([(x0 + offset, y0 + offset), (x0 + size - 1 - offset, y0 + offset)], fill=top + (255,))
        draw.line([(x0 + offset, y0 + offset), (x0 + offset, y0 + size - 1 - offset)], fill=top + (255,))
        draw.line(
            [(x0 + offset, y0 + size - 1 - offset), (x0 + size - 1 - offset, y0 + size - 1 - offset)],
            fill=bottom + (255,))
        draw.line(
            [(x0 + size - 1 - offset, y0 + offset), (x0 + size - 1 - offset, y0 + size - 1 - offset)],
            fill=bottom + (255,))


def draw_button(image, x0, y0, size):
    draw = ImageDraw.Draw(image)
    draw.rectangle([x0, y0, x0 + size - 1, y0 + size - 1], fill=BUTTON_FILL + (255,))
    draw.rectangle([x0, y0, x0 + size - 1, y0 + size - 1], outline=CYAN + (255,))
    draw.line([(x0 + 1, y0 + 1), (x0 + size - 2, y0 + 1)], fill=BUTTON_HI + (255,))
    draw.line([(x0 + 1, y0 + size - 2), (x0 + size - 2, y0 + size - 2)], fill=DARK_RIM + (255,))


def draw_cell(image, x0, y0, size):
    draw = ImageDraw.Draw(image)
    draw.rectangle([x0, y0, x0 + size - 1, y0 + size - 1], fill=CELL_FILL + (255,))
    draw.line([(x0, y0), (x0 + size - 1, y0)], fill=CELL_DARK + (255,))
    draw.line([(x0, y0), (x0, y0 + size - 1)], fill=CELL_DARK + (255,))
    draw.line([(x0, y0 + size - 1), (x0 + size - 1, y0 + size - 1)], fill=CELL_LIGHT + (255,))
    draw.line([(x0 + size - 1, y0), (x0 + size - 1, y0 + size - 1)], fill=CELL_LIGHT + (255,))


def draw_tooltip(image, x0, y0, size):
    draw = ImageDraw.Draw(image)
    draw.rectangle([x0, y0, x0 + size - 1, y0 + size - 1], fill=TOOLTIP_FILL + (255,))
    draw.rectangle([x0, y0, x0 + size - 1, y0 + size - 1], outline=DARK_RIM + (255,))
    draw.rectangle([x0 + 1, y0 + 1, x0 + size - 2, y0 + size - 2], outline=CYAN + (255,))


def main():
    image = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    draw_panel(image, 0, 0, 24)
    draw_button(image, 24, 0, 16)
    draw_cell(image, 0, 24, 18)
    draw_tooltip(image, 24, 24, 24)

    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    image.save(OUT)
    print("written", OUT, image.size)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
