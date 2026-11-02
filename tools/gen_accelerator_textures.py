"""Generates accelerator item textures."""

import os

from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src", "main", "resources", "assets", "berlit", "textures", "items")
os.makedirs(OUT, exist_ok=True)


def accelerator(path, body, glow):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rectangle([2, 2, 13, 13], fill=body, outline=(30, 30, 30, 255))
    d.rectangle([4, 4, 11, 11], fill=(40, 40, 40, 255))
    d.ellipse([5, 5, 10, 10], fill=glow)
    d.line([3, 1, 3, 3], fill=(200, 200, 200, 255))
    d.line([12, 12, 12, 14], fill=(200, 200, 200, 255))
    img.save(path)


accelerator(os.path.join(OUT, "accelerator_t2.png"), (70, 90, 120, 255), (80, 180, 255, 255))
accelerator(os.path.join(OUT, "accelerator_t3.png"), (90, 70, 110, 255), (200, 120, 255, 255))
print("done")
