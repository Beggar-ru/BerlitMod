import os

import sys

from PIL import Image, ImageDraw

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import digital_faces  # noqa: E402

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
BLOCKS = os.path.join(ROOT, "src", "main", "resources", "assets", "berlit", "textures", "blocks")
DIGITAL_HAND = os.path.join(ROOT, "docs", "reference", "ic2", "red", "hand")




DARK = (22, 25, 29, 255)
EDGE = (10, 11, 13, 255)
MID = (52, 60, 67, 255)
LIGHT = (105, 118, 128, 255)
GOLD = (212, 172, 74, 255)
CYAN = (79, 214, 205, 255)
RED = (214, 62, 62, 255)


def frame(fill):
    im = Image.new("RGBA", (16, 16), DARK)
    d = ImageDraw.Draw(im)
    d.rectangle((0, 0, 15, 15), outline=EDGE)
    d.rectangle((1, 1, 14, 14), outline=MID)
    d.rectangle((2, 2, 13, 13), fill=fill)
    return im, d


def side(accent):
    im, d = frame((36, 42, 47, 255))
    for y in (4, 6, 9, 11):
        d.line((3, y, 12, y), fill=MID)
    d.rectangle((4, 3, 5, 12), fill=LIGHT)
    d.rectangle((10, 3, 11, 12), fill=LIGHT)
    d.rectangle((3, 4, 3, 11), fill=accent)
    d.rectangle((12, 4, 12, 11), fill=accent)
    d.rectangle((6, 7, 9, 8), fill=accent)
    return im


def top(accent, grid=False):
    im, d = frame((36, 42, 47, 255))
    d.rectangle((4, 4, 11, 11), fill=(27, 32, 37, 255))
    d.rectangle((5, 5, 10, 10), outline=accent)
    d.rectangle((7, 7, 8, 8), fill=accent)
    if grid:
        for x in (5, 8, 10):
            d.line((x, 5, x, 10), fill=(90, 74, 38, 255))
        for y in (5, 8, 10):
            d.line((5, y, 10, y), fill=(90, 74, 38, 255))
    return im


def bottom(accent):
    im, d = frame((31, 36, 41, 255))
    d.rectangle((3, 3, 12, 12), outline=MID)
    d.rectangle((5, 5, 10, 10), fill=(23, 27, 31, 255))
    d.point((7, 7), fill=accent)
    d.point((8, 8), fill=accent)
    return im


base = "src/main/resources/assets/berlit/textures/blocks"
for name, accent, grid in (
    ("digital_avaritia_assembler", GOLD, True),
    ("digital_neutron_compressor", RED, False),
):
    if digital_faces.build(name, accent):
        continue
    side(accent).save(f"{base}/{name}_side.png")
    top(accent, grid).save(f"{base}/{name}_top.png")
    bottom(accent).save(f"{base}/{name}_bottom.png")
print("written digital crafting textures")
