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
# The metal former was split into two blocks; the accent tells them apart.
machine("machine_metal_extruder", (190, 170, 70, 255))
machine("machine_metal_roller", (120, 165, 205, 255))
machine("machine_electric_furnace", (220, 120, 60, 255))
# UU matter replicator: violet accent, its own names (replicator_side/top/bottom).
def digital_machine(name, accent):
    """Dark circuit-board machine texture with a cyan digital frame and a type accent."""
    side = Image.new("RGBA", (16, 16), (24, 28, 38, 255))
    d = ImageDraw.Draw(side)
    d.rectangle([0, 0, 15, 15], outline=(8, 10, 14, 255))
    d.rectangle([2, 2, 13, 13], fill=(34, 40, 52, 255), outline=(80, 220, 230, 255))
    d.line([4, 5, 11, 5], fill=accent)
    d.line([4, 7, 11, 7], fill=(80, 220, 230, 255))
    d.line([4, 9, 11, 9], fill=accent)
    d.rectangle([5, 11, 10, 12], fill=(80, 220, 230, 255))
    d.rectangle([3, 3, 4, 4], fill=(80, 220, 230, 255))
    d.rectangle([11, 3, 12, 4], fill=(80, 220, 230, 255))
    side.save(os.path.join(BLOCKS, name + "_side.png"))

    top = Image.new("RGBA", (16, 16), (20, 24, 34, 255))
    td = ImageDraw.Draw(top)
    td.rectangle([0, 0, 15, 15], outline=(8, 10, 14, 255))
    td.rectangle([2, 2, 13, 13], fill=(30, 36, 48, 255), outline=(80, 220, 230, 255))
    for row in range(2):
        for col in range(2):
            td.rectangle([4 + col * 5, 4 + row * 5, 7 + col * 5, 7 + row * 5], fill=accent)
    td.rectangle([7, 7, 8, 8], fill=(80, 220, 230, 255))
    top.save(os.path.join(BLOCKS, name + "_top.png"))

    bottom = Image.new("RGBA", (16, 16), (16, 20, 28, 255))
    bd = ImageDraw.Draw(bottom)
    bd.rectangle([0, 0, 15, 15], outline=(8, 10, 14, 255))
    bd.rectangle([3, 3, 12, 12], fill=(26, 32, 42, 255), outline=(40, 120, 130, 255))
    for y in (5, 8, 11):
        bd.line([4, y, 11, y], fill=(80, 220, 230, 255))
    bottom.save(os.path.join(BLOCKS, name + "_bottom.png"))


machine("replicator", (170, 110, 230, 255))

# Digital machines: one circuit-board texture per Berlit processing machine.
digital_machine("digital_macerator", (220, 120, 60, 255))
digital_machine("digital_compressor", (90, 140, 220, 255))
digital_machine("digital_metal_extruder", (190, 170, 70, 255))
digital_machine("digital_metal_roller", (120, 165, 205, 255))
digital_machine("digital_electric_furnace", (220, 80, 60, 255))

# Stack upgrade icon.
img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
d = ImageDraw.Draw(img)
d.rectangle([1, 5, 14, 12], fill=(120, 120, 130, 255), outline=(40, 40, 44, 255))
d.rectangle([3, 2, 12, 10], fill=(150, 150, 160, 255), outline=(40, 40, 44, 255))
d.rectangle([5, 4, 10, 8], fill=(70, 180, 90, 255))
img.save(os.path.join(ITEMS, "stack_upgrade.png"))

# Hidden digital-pattern icon: small circuit chip.
img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
d = ImageDraw.Draw(img)
d.rectangle([1, 3, 14, 12], fill=(30, 38, 50, 255), outline=(40, 120, 130, 255))
d.rectangle([4, 5, 11, 10], fill=(70, 220, 230, 255), outline=(10, 20, 26, 255))
for x in (3, 6, 9, 12):
    d.line([x, 1, x, 3], fill=(120, 240, 245, 255))
    d.line([x, 12, x, 14], fill=(120, 240, 245, 255))
img.save(os.path.join(ITEMS, "digital_pattern.png"))

print("done")
