"""Generates the Avaritia pattern item texture."""

import os

from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src", "main", "resources", "assets", "berlit", "textures", "items")
os.makedirs(OUT, exist_ok=True)

img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
d = ImageDraw.Draw(img)
d.rectangle([1, 2, 14, 13], fill=(226, 226, 220, 255), outline=(40, 40, 44, 255))
for row in range(3):
    for col in range(3):
        d.rectangle([3 + col * 4, 4 + row * 4, 5 + col * 4, 6 + row * 4], fill=(70, 140, 220, 255))
img.save(os.path.join(OUT, "avaritia_pattern.png"))
print("done")
