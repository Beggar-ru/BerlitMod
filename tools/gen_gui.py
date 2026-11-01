"""Generates the Molecular Transformer GUI texture (256x256, GUI area 176x166)."""

import os

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

d.rectangle([0, 0, W - 1, H - 1], fill=BG, outline=DARK)
d.line([1, 1, W - 2, 1], fill=LIGHT)
d.line([1, 1, 1, H - 2], fill=LIGHT)
d.line([W - 2, 1, W - 2, H - 2], fill=SHADOW)
d.line([1, H - 2, W - 2, H - 2], fill=SHADOW)


def slot(slot_x, slot_y):
    """Draws the 18x18 slot background for a Slot at (slot_x, slot_y) (vanilla draws it at -1 offset)."""
    x = slot_x - 1
    y = slot_y - 1
    d.rectangle([x, y, x + 17, y + 17], fill=SLOT_DARK)
    d.rectangle([x + 1, y + 1, x + 16, y + 16], fill=SLOT_FILL)


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

# Recessed panel for the status text under the machine slots.
d.rectangle([24, 57, 170, 80], fill=(139, 139, 139, 255), outline=SLOT_DARK)

img.save(OUT)
print("written", OUT, img.size)
