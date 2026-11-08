"""Generates processing machine block and stack upgrade textures."""

import io
import os
import zipfile

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


machine("machine_compressor", (90, 140, 220, 255))
# --- macerator: IC2 shape, Berlit colours ----------------------------------
IC2_JAR = os.path.join(ROOT, "libs", "industrialcraft-2-2.2.828-experimental.jar")
IC2_MACERATOR = "assets/ic2/textures/blocks/machine/blockMacerator.png"
# The IC2 sheet holds 32x32 faces: index 1 has the rotor, index 0 is the plain casing.
IC2_ROTOR = 32
IC2_CASING = 0


def ic2_face(offset):
    """One 32x32 face from the IC2 macerator sheet."""
    with zipfile.ZipFile(IC2_JAR) as jar:
        sheet = Image.open(io.BytesIO(jar.read(IC2_MACERATOR))).convert("RGBA")
    return sheet.crop((offset, 0, offset + 32, 32))


def ramp_pixel(pixel, stops):
    """Map the pixel lightness onto a colour ramp, keeping alpha and therefore the silhouette."""
    level = pixel[0] * 0.299 + pixel[1] * 0.587 + pixel[2] * 0.114
    level = max(0.0, min(255.0, level))
    for index in range(len(stops) - 1):
        low_level, low_colour = stops[index]
        high_level, high_colour = stops[index + 1]
        if level <= high_level or index == len(stops) - 2:
            span = max(1.0, high_level - low_level)
            mix = max(0.0, min(1.0, (level - low_level) / span))
            colour = tuple(round(low_colour[c] + (high_colour[c] - low_colour[c]) * mix) for c in range(3))
            return colour + (pixel[3],)
    return pixel


def recolour(image, stops):
    result = image.copy()
    pixels = result.load()
    for y in range(result.size[1]):
        for x in range(result.size[0]):
            pixels[x, y] = ramp_pixel(pixels[x, y], stops)
    return result


def macerator(name, stops):
    """IC2 macerator faces, recoloured and downsampled to the 16x16 the mod uses."""
    rotor = recolour(ic2_face(IC2_ROTOR), stops).resize((16, 16), Image.NEAREST)
    casing = recolour(ic2_face(IC2_CASING), stops).resize((16, 16), Image.NEAREST)
    rotor.save(os.path.join(BLOCKS, name + "_side.png"))
    casing.save(os.path.join(BLOCKS, name + "_top.png"))
    casing.save(os.path.join(BLOCKS, name + "_bottom.png"))


# The metal former was split into two blocks; the accent tells them apart.
# Macerator keeps the IC2 silhouette: steel body, slightly cooler than vanilla IC2.
macerator("machine_macerator", ((0, (20, 23, 29)), (140, (74, 84, 104)), (255, (198, 208, 222))))
# Digital version uses the interface palette: dark slate casing, glowing cyan rotor.
macerator("digital_macerator", ((0, (111, 233, 224)), (120, (44, 140, 138)), (255, (38, 42, 52))))

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
