"""Recolours the IC2 scrap icon into the eight solid matter kinds.

The shape, outline and shading come from `itemScrap.png` in the IC2 jar: only the colour is
mapped, through a luminance ramp, so every kind keeps the same silhouette.
"""

from __future__ import annotations

import io
import os
import zipfile

from PIL import Image

ROOT = r"C:\main_mod"
IC2 = os.path.join(ROOT, "libs", "industrialcraft-2-2.2.828-experimental.jar")
SCRAP = "assets/ic2/textures/items/itemScrap.png"
OUT = os.path.join(ROOT, "src", "main", "resources", "assets", "berlit", "textures", "items")

MATTER = [
    ("standard", (232, 236, 242)),
    ("violet", (168, 124, 224)),
    ("turquoise", (79, 214, 205)),
    ("emerald", (91, 217, 122)),
    ("azure", (91, 143, 224)),
    ("amber", (232, 180, 79)),
    ("crimson", (224, 96, 91)),
    ("radiant", (255, 242, 176)),
]


def luminance(pixel):
    return 0.299 * pixel[0] + 0.587 * pixel[1] + 0.114 * pixel[2]


def mix(first, second, t):
    return tuple(round(first[i] + (second[i] - first[i]) * t) for i in range(3))


def ramp(colour, level):
    """Map a 0..255 lightness onto a dark -> base -> light ramp of the target colour."""
    dark = tuple(channel * 0.42 for channel in colour)
    light = tuple(min(255, channel * 1.32) for channel in colour)
    if level < 128:
        return mix(dark, colour, level / 128.0)
    return mix(colour, light, (level - 128) / 127.0)


def recolour(source, colour):
    image = source.copy()
    pixels = image.load()
    for y in range(image.size[1]):
        for x in range(image.size[0]):
            r, g, b, a = pixels[x, y]
            if a == 0:
                continue
            level = int(max(0, min(255, luminance((r, g, b)) * 1.55)))
            pixels[x, y] = ramp(colour, level) + (a,)
    return image


def main():
    if not os.path.isfile(IC2):
        raise SystemExit(f"IC2 jar not found: {IC2}")

    with zipfile.ZipFile(IC2) as jar:
        base = Image.open(io.BytesIO(jar.read(SCRAP))).convert("RGBA")
    if base.size != (16, 16):
        raise SystemExit(f"unexpected scrap size {base.size}")

    for name, colour in MATTER:
        recolour(base, colour).save(os.path.join(OUT, f"matter_{name}.png"))

    print("written 8 matter icons from IC2 scrap")


if __name__ == "__main__":
    main()
