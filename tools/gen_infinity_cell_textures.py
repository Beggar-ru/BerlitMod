"""Generates the 32x32 dark Berlit infinity cell textures.

Both cells follow the layout of AE2's 32x32 storage-cell icon: an isometric
housing with a diamond top face, darker side faces and a glowing screen.  The
colour scheme is the dark Berlit GUI palette with turquoise accents:

* infinity_cell.png       - item cell, screen with four storage pips;
* infinity_fluid_cell.png - fluid cell, screen with a droplet.

Run from the project root: python tools/gen_infinity_cell_textures.py
"""

import os

from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src", "main", "resources", "assets", "berlit", "textures", "items")

SIZE = 32

COLORS = {
    "outline": (8, 10, 13, 255),
    "top": (80, 90, 110, 255),
    "top_hl": (108, 121, 147, 255),
    "top_dk": (64, 72, 89, 255),
    "left": (60, 67, 82, 255),
    "left_dk": (46, 51, 63, 255),
    "right": (46, 52, 64, 255),
    "right_dk": (36, 40, 50, 255),
    "bevel": (152, 168, 196, 255),
    "bevel_dk": (22, 26, 33, 255),
    "screen_bg": (9, 16, 18, 255),
    # Berlit GUI turquoise.
    "screen_edge": (44, 140, 138, 255),
    "screen_in": (15, 50, 51, 255),
    "icon": (111, 233, 224, 255),
    "icon_hl": (200, 255, 251, 255),
    "icon_dk": (28, 100, 98, 255),
    "fluid": (127, 227, 255, 255),
    "fluid_hl": (220, 250, 255, 255),
    "fluid_dk": (38, 120, 150, 255),
    "stem": (30, 35, 44, 255),
    "stem_hl": (68, 76, 92, 255),
    "stem_dk": (17, 20, 25, 255),
}


def _cell(fluid):
    image = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)
    c = COLORS

    # Hexagonal isometric housing.
    draw.polygon([(16, 1), (28, 12), (28, 19), (16, 30), (4, 19), (4, 12)], fill=c["outline"])
    draw.polygon([(16, 2), (27, 12), (27, 19), (16, 29), (5, 19), (5, 12)], fill=c["top"])
    draw.polygon([(16, 2), (27, 12), (16, 22), (5, 12)], fill=c["top"])
    draw.polygon([(5, 12), (16, 22), (16, 29), (5, 19)], fill=c["left"])
    draw.polygon([(27, 12), (16, 22), (16, 29), (27, 19)], fill=c["right"])

    # Facet lighting.
    draw.line([(16, 2), (5, 12)], fill=c["top_hl"])
    draw.line([(5, 12), (16, 22)], fill=c["top_hl"])
    draw.line([(27, 12), (16, 22)], fill=c["top_dk"])
    draw.line([(5, 12), (5, 19)], fill=c["bevel"])
    draw.line([(5, 19), (16, 29)], fill=c["left_dk"])
    draw.line([(27, 12), (27, 19)], fill=c["bevel_dk"])
    draw.line([(27, 19), (16, 29)], fill=c["right_dk"])
    draw.line([(5, 12), (16, 22)], fill=c["bevel_dk"])
    draw.line([(16, 22), (16, 29)], fill=c["bevel_dk"])

    # Diamond panel line on the top face.
    draw.line([(16, 5), (9, 12)], fill=c["top_hl"])
    draw.line([(9, 12), (16, 19)], fill=c["top_dk"])
    draw.line([(16, 19), (23, 12)], fill=c["top_dk"])
    draw.line([(23, 12), (16, 5)], fill=c["top_dk"])

    # Screen: dark bezel, turquoise frame, dark glass.
    draw.rectangle([10, 7, 21, 16], fill=c["screen_bg"])
    draw.rectangle([11, 8, 20, 15], fill=c["screen_edge"])
    draw.rectangle([12, 9, 19, 14], fill=c["screen_in"])

    if fluid:
        droplet = (
            (16, 9), (16, 10),
            (15, 11), (16, 11), (17, 11),
            (14, 12), (15, 12), (16, 12), (17, 12), (18, 12),
            (14, 13), (15, 13), (16, 13), (17, 13), (18, 13),
            (15, 14), (16, 14), (17, 14),
        )
        for px, py in droplet:
            image.putpixel((px, py), c["fluid"])
        image.putpixel((15, 11), c["fluid_hl"])
        image.putpixel((15, 12), c["fluid_hl"])
        image.putpixel((17, 13), c["fluid_dk"])
        image.putpixel((18, 13), c["fluid_dk"])
    else:
        for bx, by in ((13, 10), (17, 10), (13, 13), (17, 13)):
            draw.rectangle([bx, by, bx + 1, by + 1], fill=c["icon"])
            image.putpixel((bx, by), c["icon_hl"])
            image.putpixel((bx + 1, by + 1), c["icon_dk"])

    # Bottom stem with a small turquoise status line.
    draw.rectangle([14, 22, 17, 29], fill=c["stem"])
    draw.line([(14, 22), (14, 29)], fill=c["stem_hl"])
    draw.line([(17, 22), (17, 29)], fill=c["stem_dk"])
    draw.line([(13, 29), (18, 29)], fill=c["stem_hl"])
    draw.rectangle([14, 29, 17, 30], fill=c["outline"])
    image.putpixel((15, 28), c["screen_edge"])
    image.putpixel((16, 28), c["screen_edge"])

    return image


def main():
    os.makedirs(OUT, exist_ok=True)
    _cell(fluid=False).save(os.path.join(OUT, "infinity_cell.png"))
    _cell(fluid=True).save(os.path.join(OUT, "infinity_fluid_cell.png"))
    print("generated 2 infinity cell textures at 32x32")


if __name__ == "__main__":
    main()
