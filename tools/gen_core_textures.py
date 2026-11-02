"""Generates spawner core item textures."""

import os

from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src", "main", "resources", "assets", "berlit", "textures", "items")
os.makedirs(OUT, exist_ok=True)


def core(path, glow):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.ellipse([1, 1, 14, 14], fill=(45, 45, 50, 255), outline=(20, 20, 22, 255))
    d.ellipse([4, 4, 11, 11], fill=glow, outline=(20, 20, 22, 255))
    d.ellipse([6, 6, 9, 9], fill=(255, 255, 255, 180))
    img.save(path)


core(os.path.join(OUT, "spawner_core_t1.png"), (90, 200, 120, 255))
core(os.path.join(OUT, "spawner_core_t2.png"), (90, 160, 255, 255))
core(os.path.join(OUT, "spawner_core_t3.png"), (200, 110, 255, 255))
print("done")
