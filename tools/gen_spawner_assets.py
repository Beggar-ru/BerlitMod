"""Generates spawner block/item/GUI textures."""

import os

from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "src", "main", "resources", "assets", "berlit", "textures")
os.makedirs(os.path.join(ASSETS, "blocks"), exist_ok=True)
os.makedirs(os.path.join(ASSETS, "items"), exist_ok=True)
os.makedirs(os.path.join(ASSETS, "gui"), exist_ok=True)

BG = (198, 198, 198, 255)
DARK = (85, 85, 85, 255)
SHADOW = (55, 55, 55, 255)
LIGHT = (255, 255, 255, 255)
SLOT_FILL = (139, 139, 139, 255)
SLOT_DARK = (55, 55, 55, 255)


def panel(draw, width, height):
    draw.rectangle([0, 0, width - 1, height - 1], fill=BG, outline=DARK)
    draw.line([1, 1, width - 2, 1], fill=LIGHT)
    draw.line([1, 1, 1, height - 2], fill=LIGHT)
    draw.line([width - 2, 1, width - 2, height - 2], fill=SHADOW)
    draw.line([1, height - 2, width - 2, height - 2], fill=SHADOW)


def slot(draw, x, y, size=18):
    x -= 1
    y -= 1
    draw.rectangle([x, y, x + size - 1, y + size - 1], fill=SLOT_DARK)
    draw.rectangle([x + 1, y + 1, x + size - 2, y + size - 2], fill=SLOT_FILL)


# ---- Block textures (16x16) ----
def block_texture(path, bars):
    img = Image.new("RGBA", (16, 16), (52, 52, 56, 255))
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, 15, 15], outline=(30, 30, 32, 255))
    if bars:
        for x in (2, 5, 8, 11, 14):
            d.line([x, 2, x, 13], fill=(96, 96, 100, 255))
        d.line([2, 7, 13, 7], fill=(78, 78, 82, 255))
    else:
        d.rectangle([3, 3, 12, 12], fill=(72, 72, 76, 255), outline=(96, 96, 100, 255))
        d.rectangle([6, 6, 9, 9], fill=(40, 120, 60, 255))
    img.save(path)


block_texture(os.path.join(ASSETS, "blocks", "spawner_side.png"), True)
block_texture(os.path.join(ASSETS, "blocks", "spawner_top.png"), False)
block_texture(os.path.join(ASSETS, "blocks", "spawner_bottom.png"), False)

# ---- Entity card item (16x16) ----
card = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
cd = ImageDraw.Draw(card)
cd.rectangle([2, 1, 13, 14], fill=(238, 238, 232, 255), outline=(60, 60, 60, 255))
cd.rectangle([4, 3, 11, 6], fill=(70, 70, 70, 255))
cd.ellipse([6, 8, 9, 11], fill=(40, 170, 80, 255))
card.save(os.path.join(ASSETS, "items", "entity_card.png"))

# ---- Spawner GUI (256x256, 176x166) ----
gui = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
gd = ImageDraw.Draw(gui)
panel(gd, 176, 166)

slot(gd, 26, 17)  # entity
for i in range(7):
    slot(gd, 62 + (i % 4) * 18, 17 + (i // 4) * 18)

for row in range(3):
    for col in range(9):
        slot(gd, 8 + col * 18, 84 + row * 18)
for col in range(9):
    slot(gd, 8 + col * 18, 142)

# Status panel under the machine slots.
gd.rectangle([6, 56, 170, 80], fill=(139, 139, 139, 255), outline=SLOT_DARK)
gui.save(os.path.join(ASSETS, "gui", "spawner.png"))

# ---- NEI background (same panel, no slots) ----
nei = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
nd = ImageDraw.Draw(nei)
panel(nd, 176, 166)
nei.save(os.path.join(ASSETS, "gui", "nei_spawner.png"))

print("done")
