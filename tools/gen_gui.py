"""Generates the Molecular Transformer GUI texture (256x256, GUI area 176x166)."""

import os
import sys

from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src", "main", "resources", "assets", "berlit", "textures", "gui", "molecular_transformer.png")
os.makedirs(os.path.dirname(OUT), exist_ok=True)

W, H = 176, 166
img = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
d = ImageDraw.Draw(img)

BG = (198, 198, 198, 255)
DARK = (85, 85, 85, 255)
SHADOW = (55, 55, 55, 255)
LIGHT = (255, 255, 255, 255)
SLOT_FILL = (139, 139, 139, 255)
SLOT_DARK = (55, 55, 55, 255)

# Panel.
d.rectangle([0, 0, W - 1, H - 1], fill=BG, outline=DARK)
d.line([1, 1, W - 2, 1], fill=LIGHT)
d.line([1, 1, 1, H - 2], fill=LIGHT)
d.line([W - 2, 1, W - 2, H - 2], fill=SHADOW)
d.line([1, H - 2, W - 2, H - 2], fill=SHADOW)


def slot(x, y):
    d.rectangle([x, y, x + 17, y + 17], fill=SLOT_DARK)
    d.rectangle([x + 1, y + 1, x + 16, y + 16], fill=SLOT_FILL)
    d.line([x + 1, y + 1, x + 16, y + 1], fill=SLOT_DARK)
    d.line([x + 1, y + 1, x + 1, y + 16], fill=SLOT_DARK)


# Machine slots: 6 input (left) + 6 output (right).
for i in range(6):
    slot(26 + (i % 3) * 18, 17 + (i // 3) * 18)
for i in range(6):
    slot(98 + (i % 3) * 18, 17 + (i // 3) * 18)

# Player inventory + hotbar.
for row in range(3):
    for col in range(9):
        slot(8 + col * 18, 84 + row * 18)
for col in range(9):
    slot(8 + col * 18, 142)

# Energy bar frame (left side).
d.rectangle([8, 17, 19, 74], fill=SLOT_DARK)
d.rectangle([9, 18, 18, 73], fill=(30, 30, 30, 255))

# Energy bar fill overlay at (176, 0), 12x58.
for i in range(58):
    t = i / 57.0
    color = (int(40 + 120 * t), int(150 + 90 * t), int(40 + 60 * t), 255)
    d.line([176, i, 187, i], fill=color)

img.save(OUT)
print("written", OUT, img.size)
