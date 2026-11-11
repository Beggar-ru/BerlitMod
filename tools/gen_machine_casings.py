"""Regenerates the common machine faces (side/top/bottom) in the style of the supplied fronts.

0.29.6 brought detailed 32x32 `_front` textures but left the old flat 16x16 `_side`/`_top`/
`_bottom` in place, so every machine looked like a detailed face glued to a plain box. These
faces now reuse the front's own casing colour, so the block reads as one piece.
"""

from __future__ import annotations

import collections
import os

from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
BLOCKS = os.path.join(ROOT, "src", "main", "resources", "assets", "berlit", "textures", "blocks")

SIZE = 32

# machine -> accent colour, matching the front textures
MACHINES = {
    "machine_compressor": (90, 140, 220),
    "machine_metal_extruder": (190, 170, 70),
    "machine_metal_roller": (120, 165, 205),
    "machine_electric_furnace": (220, 120, 60),
}


def shade(colour, factor):
    return tuple(max(0, min(255, int(channel * factor))) for channel in colour)


def casing_colour(front):
    """Most common colour of the front: that is the block's body."""
    colours = collections.Counter(pixel[:3] for pixel in front.getdata() if pixel[3] > 0)
    return colours.most_common(1)[0][0]


def side_face(base, accent):
    image = Image.new("RGBA", (SIZE, SIZE), base + (255,))
    draw = ImageDraw.Draw(image)
    draw.rectangle([0, 0, SIZE - 1, SIZE - 1], outline=shade(base, 0.55) + (255,))
    draw.rectangle([3, 3, SIZE - 4, SIZE - 4], fill=shade(base, 0.88) + (255,), outline=shade(base, 0.7) + (255,))
    for x, y in ((5, 5), (SIZE - 6, 5), (5, SIZE - 6), (SIZE - 6, SIZE - 6)):
        draw.rectangle([x - 1, y - 1, x + 1, y + 1], fill=shade(accent, 0.75) + (255,))
    return image


def top_face(base, accent):
    image = Image.new("RGBA", (SIZE, SIZE), base + (255,))
    draw = ImageDraw.Draw(image)
    draw.rectangle([0, 0, SIZE - 1, SIZE - 1], outline=shade(base, 0.55) + (255,))
    draw.rectangle([4, 4, SIZE - 5, SIZE - 5], fill=shade(base, 0.85) + (255,), outline=shade(base, 0.7) + (255,))
    centre = SIZE // 2
    draw.line([(centre, 7), (centre, SIZE - 8)], fill=accent + (255,))
    draw.line([(7, centre), (SIZE - 8, centre)], fill=accent + (255,))
    draw.rectangle([centre - 2, centre - 2, centre + 2, centre + 2], fill=shade(accent, 1.15) + (255,))
    return image


def bottom_face(base):
    image = Image.new("RGBA", (SIZE, SIZE), shade(base, 0.8) + (255,))
    draw = ImageDraw.Draw(image)
    draw.rectangle([0, 0, SIZE - 1, SIZE - 1], outline=shade(base, 0.45) + (255,))
    draw.rectangle([4, 4, SIZE - 5, SIZE - 5], fill=shade(base, 0.7) + (255,), outline=shade(base, 0.55) + (255,))
    return image


def main():
    for name, accent in MACHINES.items():
        front_path = os.path.join(BLOCKS, f"{name}_front.png")
        if not os.path.isfile(front_path):
            print("skip", name, "- no front texture")
            continue
        with Image.open(front_path) as front_image:
            front = front_image.convert("RGBA")
        base = casing_colour(front)

        side_face(base, accent).save(os.path.join(BLOCKS, f"{name}_side.png"))
        top_face(base, accent).save(os.path.join(BLOCKS, f"{name}_top.png"))
        bottom_face(base).save(os.path.join(BLOCKS, f"{name}_bottom.png"))
        print("written", name, "faces from casing", "#%02X%02X%02X" % base)


if __name__ == "__main__":
    main()
