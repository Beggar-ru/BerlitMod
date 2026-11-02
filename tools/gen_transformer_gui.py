"""Regenerates the Molecular Transformer GUI with 4 upgrade slots."""

import os

from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src", "main", "resources", "assets", "berlit", "textures", "gui", "molecular_transformer.png")

BG = (198, 198, 198, 255)
DARK = (85, 85, 85, 255)
SHADOW = (55, 55, 55, 255)
LIGHT = (255, 255, 255, 255)
SLOT_FILL = (139, 139, 139, 255)
SLOT_DARK = (55, 55, 55, 255)

img = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
d = ImageDraw.Draw(img)

d.rectangle([0, 0, 175, 165], fill=BG, outline=DARK)
d.line([1, 1, 174, 1], fill=LIGHT)
d.line([1, 1, 1, 164], fill=LIGHT)
d.line([174, 1, 174, 164], fill=SHADOW)
d.line([1, 164, 174, 164], fill=SHADOW)


def slot(slot_x, slot_y):
    x = slot_x - 1
    y = slot_y - 1
    d.rectangle([x, y, x + 17, y + 17], fill=SLOT_DARK)
    d.rectangle([x + 1, y + 1, x + 16, y + 16], fill=SLOT_FILL)


for i in range(6):
    slot(26 + (i % 3) * 18, 17 + (i // 3) * 18)
for i in range(6):
    slot(98 + (i % 3) * 18, 17 + (i // 3) * 18)
for i in range(4):
    slot(98 + i * 18, 59)

for row in range(3):
    for col in range(9):
        slot(8 + col * 18, 84 + row * 18)
for col in range(9):
    slot(8 + col * 18, 142)

# Text panel on the left, under the machine slots.
d.rectangle([6, 56, 94, 80], fill=(139, 139, 139, 255), outline=SLOT_DARK)

img.save(OUT)
print("written", OUT)
