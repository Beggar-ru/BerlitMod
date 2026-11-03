"""Generates the super chest block textures."""

import os

from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src", "main", "resources", "assets", "berlit", "textures", "blocks")
os.makedirs(OUT, exist_ok=True)

img = Image.new("RGBA", (16, 16), (74, 58, 40, 255))
d = ImageDraw.Draw(img)
d.rectangle([0, 0, 15, 15], outline=(40, 30, 20, 255))
d.rectangle([2, 2, 13, 13], fill=(96, 74, 48, 255), outline=(60, 44, 28, 255))
d.rectangle([7, 7, 8, 9], fill=(220, 190, 90, 255))
img.save(os.path.join(OUT, "super_chest_side.png"))

top = Image.new("RGBA", (16, 16), (86, 68, 46, 255))
td = ImageDraw.Draw(top)
td.rectangle([0, 0, 15, 15], outline=(40, 30, 20, 255))
td.rectangle([3, 3, 12, 12], fill=(104, 82, 54, 255), outline=(60, 44, 28, 255))
top.save(os.path.join(OUT, "super_chest_top.png"))

print("done")
