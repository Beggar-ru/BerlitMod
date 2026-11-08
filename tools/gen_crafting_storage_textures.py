"""Builds the nine crafting-storage textures from AE2's own storage block.

The AE2 crafting storage is a grey casing with a coloured core; each tier keeps that shape and
only the core colour changes, so the blocks read as one family next to regular AE2 CPU parts.
"""

from __future__ import annotations

import io
import os
import zipfile

from PIL import Image

ROOT = r"C:\main_mod"
AE2 = os.path.join(ROOT, "libs", "appliedenergistics2-rv3-beta-6.jar")
BASE = "assets/appliedenergistics2/textures/blocks/BlockCraftingStorage.png"
OUT = os.path.join(ROOT, "src", "main", "resources", "assets", "berlit", "textures", "blocks")

TIERS = [
    ("crafting_storage_256k", (138, 148, 166)),
    ("crafting_storage_1m", (111, 168, 220)),
    ("crafting_storage_4m", (79, 214, 205)),
    ("crafting_storage_16m", (91, 217, 122)),
    ("crafting_storage_32m", (232, 194, 79)),
    ("crafting_storage_64m", (232, 145, 79)),
    ("crafting_storage_128m", (224, 96, 91)),
    ("crafting_storage_256m", (168, 124, 224)),
    ("crafting_storage_512m", (255, 242, 176)),
]

CASING = (176, 176, 176)


def is_core(pixel):
    r, g, b, a = pixel
    return a > 0 and r > 120 and r > g + 25 and g > b


def mix(first, second, t):
    return tuple(round(first[i] + (second[i] - first[i]) * t) for i in range(3))


def ramp(colour, level):
    dark = tuple(channel * 0.55 for channel in colour)
    light = tuple(min(255, channel * 1.25) for channel in colour)
    if level < 128:
        return mix(dark, colour, level / 128.0)
    return mix(colour, light, (level - 128) / 127.0)


def recolour_core(source, colour):
    """Keep the casing, repaint the core through a luminance ramp."""
    image = source.copy()
    pixels = image.load()
    for y in range(image.size[1]):
        for x in range(image.size[0]):
            pixel = pixels[x, y]
            if not is_core(pixel):
                continue
            level = int(max(0, min(255, (pixel[0] * 0.6 + pixel[1] * 0.3 + pixel[2] * 0.1) * 1.15)))
            pixels[x, y] = ramp(colour, level) + (pixel[3],)
    return image


def blank_core(source):
    """Bottom face: same casing, core filled with the casing colour instead of an accent."""
    image = source.copy()
    pixels = image.load()
    for y in range(image.size[1]):
        for x in range(image.size[0]):
            pixel = pixels[x, y]
            if not is_core(pixel):
                continue
            level = int(max(0, min(255, pixel[0] * 0.6 + pixel[1] * 0.3 + pixel[2] * 0.1)))
            pixels[x, y] = ramp(CASING, level) + (pixel[3],)
    return image


def main():
    if not os.path.isfile(AE2):
        raise SystemExit(f"AE2 jar not found: {AE2}")

    with zipfile.ZipFile(AE2) as jar:
        base = Image.open(io.BytesIO(jar.read(BASE))).convert("RGBA")

    for name, colour in TIERS:
        side = recolour_core(base, colour)
        side.save(os.path.join(OUT, f"{name}_side.png"))
        side.save(os.path.join(OUT, f"{name}_top.png"))
        blank_core(base).save(os.path.join(OUT, f"{name}_bottom.png"))

    print(f"written {len(TIERS) * 3} crafting storage textures")


if __name__ == "__main__":
    main()
