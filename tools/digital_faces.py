"""Builds the digital machine block faces from the hand-drawn set.

The casing (`digital_macerator_bottom`) is used for the top, the bottom and every side; only the
front differs. The macerator uses its hand-drawn front, the other machines get one generated in
the same language: dark casing, cyan traces, a coloured core that tells them apart.
"""

from __future__ import annotations

import os

from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
BLOCKS = os.path.join(ROOT, "src", "main", "resources", "assets", "berlit", "textures", "blocks")
HAND = os.path.join(ROOT, "docs", "reference", "ic2", "red", "hand")

CASING = os.path.join(HAND, "digital_macerator_bottom.png")
MACERATOR_FRONT = os.path.join(HAND, "digital_macerator_side.png")

TRACE = (79, 214, 205)
CORE_EDGE = (10, 12, 16)


def available():
    return os.path.isfile(CASING)


def front(casing, accent, size=32):
    """A front face in the hand-drawn style: cyan traces running into a coloured chip."""
    accent = tuple(accent[:3])
    image = casing.copy()
    draw = ImageDraw.Draw(image)
    centre = size // 2
    half = 6

    # traces left and right of the chip
    for offset in (-4, 0, 4):
        y = centre + offset
        draw.line([(2, y), (centre - half - 2, y)], fill=TRACE + (255,))
        draw.line([(centre + half + 2, y), (size - 3, y)], fill=TRACE + (255,))

    # chip body with the machine accent
    draw.rectangle(
        [centre - half, centre - half, centre + half, centre + half],
        fill=(26, 30, 38, 255),
        outline=accent + (255,),
    )
    draw.rectangle(
        [centre - half + 2, centre - half + 2, centre + half - 2, centre + half - 2],
        outline=tuple(int(c * 0.55) for c in accent) + (255,),
    )
    draw.rectangle([centre - 2, centre - 2, centre + 2, centre + 2], fill=accent + (255,))
    draw.point((centre - 1, centre - 1), fill=(255, 255, 255, 200))
    return image


def build(name, accent):
    """Writes the three faces for one digital machine. Returns False when the hand set is missing."""
    if not available():
        return False

    casing = Image.open(CASING).convert("RGBA")
    casing.save(os.path.join(BLOCKS, f"{name}_top.png"))
    casing.save(os.path.join(BLOCKS, f"{name}_bottom.png"))

    if name == "digital_macerator" and os.path.isfile(MACERATOR_FRONT):
        Image.open(MACERATOR_FRONT).convert("RGBA").save(os.path.join(BLOCKS, f"{name}_side.png"))
    else:
        front(casing, accent).save(os.path.join(BLOCKS, f"{name}_side.png"))
    return True
