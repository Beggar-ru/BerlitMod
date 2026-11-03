"""Generates the wide super chest GUI texture (502x266 inside a 512x512 canvas)."""

import os

from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src", "main", "resources", "assets", "berlit", "textures", "gui", "super_chest.png")
os.makedirs(os.path.dirname(OUT), exist_ok=True)

BG = (42, 47, 55, 255)
DARK = (28, 32, 38, 255)
LIGHT = (88, 98, 110, 255)
SLOT_FILL = (58, 65, 74, 255)
SLOT_DARK = (30, 34, 40, 255)

W, H = 502, 266
img = Image.new("RGBA", (512, 512), (0, 0, 0, 0))
d = ImageDraw.Draw(img)

d.rectangle([0, 0, W - 1, H - 1], fill=BG, outline=DARK)
d.line([1, 1, W - 2, 1], fill=LIGHT)
d.line([1, 1, 1, H - 2], fill=LIGHT)
d.line([W - 2, 1, W - 2, H - 2], fill=DARK)
d.line([1, H - 2, W - 2, H - 2], fill=DARK)


def slot(x, y):
    d.rectangle([x - 1, y - 1, x + 16, y + 16], fill=SLOT_DARK)
    d.rectangle([x, y, x + 15, y + 15], fill=SLOT_FILL)


for row in range(9):
    for col in range(27):
        slot(8 + col * 18, 18 + row * 18)

for row in range(3):
    for col in range(9):
        slot(8 + col * 18, 190 + row * 18)
for col in range(9):
    slot(8 + col * 18, 248)

img.save(OUT)
print("written", OUT)
