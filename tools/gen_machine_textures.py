"""Generates processing machine block and stack upgrade textures."""

import os

from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
BLOCKS = os.path.join(ROOT, "src", "main", "resources", "assets", "berlit", "textures", "blocks")
ITEMS = os.path.join(ROOT, "src", "main", "resources", "assets", "berlit", "textures", "items")
os.makedirs(BLOCKS, exist_ok=True)
os.makedirs(ITEMS, exist_ok=True)


def machine(name, accent):
    side = Image.new("RGBA", (16, 16), (52, 52, 56, 255))
    d = ImageDraw.Draw(side)
    d.rectangle([0, 0, 15, 15], outline=(30, 30, 32, 255))
    d.rectangle([2, 2, 13, 13], fill=(70, 70, 75, 255), outline=accent)
    d.rectangle([4, 6, 11, 9], fill=accent)
    d.line([5, 4, 10, 4], fill=(120, 120, 125, 255))
    side.save(os.path.join(BLOCKS, name + "_side.png"))

    top = Image.new("RGBA", (16, 16), (52, 52, 56, 255))
    td = ImageDraw.Draw(top)
    td.rectangle([0, 0, 15, 15], outline=(30, 30, 32, 255))
    td.rectangle([3, 3, 12, 12], fill=(64, 64, 68, 255), outline=accent)
    td.ellipse([6, 6, 9, 9], fill=accent)
    top.save(os.path.join(BLOCKS, name + "_top.png"))

    bottom = Image.new("RGBA", (16, 16), (44, 44, 48, 255))
    bd = ImageDraw.Draw(bottom)
    bd.rectangle([0, 0, 15, 15], outline=(30, 30, 32, 255))
    bd.rectangle([3, 3, 12, 12], fill=(58, 58, 62, 255))
    bottom.save(os.path.join(BLOCKS, name + "_bottom.png"))


machine("machine_macerator", (200, 90, 60, 255))
machine("machine_compressor", (90, 140, 220, 255))
machine("machine_metal_former", (190, 170, 70, 255))
machine("machine_electric_furnace", (220, 120, 60, 255))

# Stack upgrade icon.
img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
d = ImageDraw.Draw(img)
d.rectangle([1, 5, 14, 12], fill=(120, 120, 130, 255), outline=(40, 40, 44, 255))
d.rectangle([3, 2, 12, 10], fill=(150, 150, 160, 255), outline=(40, 40, 44, 255))
d.rectangle([5, 4, 10, 8], fill=(70, 180, 90, 255))
img.save(os.path.join(ITEMS, "stack_upgrade.png"))

print("done")
