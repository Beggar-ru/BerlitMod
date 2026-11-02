"""Generates tier-2 spawner block and GUI textures."""

import os

from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "src", "main", "resources", "assets", "berlit", "textures")

BG = (198, 198, 198, 255)
DARK = (85, 85, 85, 255)
SHADOW = (55, 55, 55, 255)
LIGHT = (255, 255, 255, 255)
SLOT_FILL = (139, 139, 139, 255)
SLOT_DARK = (55, 55, 55, 255)


def block_texture(path, bars):
    img = Image.new("RGBA", (16, 16), (48, 46, 40, 255))
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, 15, 15], outline=(28, 26, 22, 255))
    if bars:
        for x in (2, 5, 8, 11, 14):
            d.line([x, 2, x, 13], fill=(170, 140, 60, 255))
        d.line([2, 7, 13, 7], fill=(120, 100, 45, 255))
    else:
        d.rectangle([3, 3, 12, 12], fill=(64, 60, 48, 255), outline=(190, 160, 70, 255))
        d.rectangle([6, 6, 9, 9], fill=(60, 160, 90, 255))
    img.save(path)


block_texture(os.path.join(ASSETS, "blocks", "spawner_t2_side.png"), True)
block_texture(os.path.join(ASSETS, "blocks", "spawner_t2_top.png"), False)
block_texture(os.path.join(ASSETS, "blocks", "spawner_t2_bottom.png"), False)

# GUI 176x166: 10 entity slots (5x2) + 7 upgrade slots (4+3).
gui = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
d = ImageDraw.Draw(gui)

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


for i in range(10):
    slot(8 + (i % 5) * 18, 17 + (i // 5) * 18)
for i in range(7):
    slot(98 + (i % 4) * 18, 17 + (i // 4) * 18)

for row in range(3):
    for col in range(9):
        slot(8 + col * 18, 84 + row * 18)
for col in range(9):
    slot(8 + col * 18, 142)

d.rectangle([6, 56, 170, 80], fill=(139, 139, 139, 255), outline=SLOT_DARK)
gui.save(os.path.join(ASSETS, "gui", "spawner_t2.png"))

print("done")
