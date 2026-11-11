"""Makes the supplied machine faces crisp.

The hand-drawn set was exported with soft gradients and glows, so at 32x32 the block atlas shows
hundreds of near-identical shades and the faces read as blurry next to pixel art. This snaps each
face to a small palette without dithering: the shapes and colours stay, the soft blends go.

Run order: after the art is in place. The other generators skip files that already exist, so this
is the last step on the machine textures.
"""

from __future__ import annotations

import os

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
BLOCKS = os.path.join(ROOT, "src", "main", "resources", "assets", "berlit", "textures", "blocks")

MACHINES = (
    "machine_macerator",
    "machine_compressor",
    "machine_metal_extruder",
    "machine_metal_roller",
    "machine_electric_furnace",
)
FACES = ("front", "side", "top", "bottom")
COLOURS = 12


def sharpen(image, colours=COLOURS):
    """Quantise to `colours` without dithering, keeping the alpha channel."""
    quantised = image.convert("RGB").quantize(colors=colours, method=Image.Quantize.FASTOCTREE,
                                              dither=Image.Dither.NONE).convert("RGBA")
    quantised.putalpha(image.getchannel("A"))
    return quantised


def main():
    done = 0
    for name in MACHINES:
        for face in FACES:
            path = os.path.join(BLOCKS, f"{name}_{face}.png")
            if not os.path.isfile(path):
                continue
            with Image.open(path) as source:
                image = source.convert("RGBA")
            before = len(image.getcolors(8192) or [])
            result = sharpen(image)
            after = len(result.getcolors(8192) or [])
            if after < before:
                result.save(path)
                done += 1
                print(f"{name}_{face}: {before} -> {after} colours")
    print(f"sharpened {done} faces")


if __name__ == "__main__":
    main()
