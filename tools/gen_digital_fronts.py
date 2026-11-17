"""Draws a product icon into the free area above the screen on the digital machine fronts.

    python tools\\gen_digital_fronts.py

The front is the same casing for every machine; only the little icon above the code screen differs.
Icons use the mod palette: light #BEC2CA for the product, #6FE9E0 highlight, #2C8C8A shade and
#14171D outline. After this runs, docs/reference/mecha_ae is refreshed so an importer run does not
put the plain fronts back.
"""

from __future__ import annotations

import os
import shutil

from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
BASE = os.path.join(ROOT, "tools", "assets", "digital_front_base.png")
BLOCKS = os.path.join(ROOT, "src", "main", "resources", "assets", "berlit", "textures", "blocks")
REFERENCE = os.path.join(ROOT, "docs", "reference", "mecha_ae")

BODY = (190, 194, 202, 255)  # #BEC2CA
LIGHT = (111, 233, 224, 255)  # #6FE9E0
SHADE = (44, 140, 138, 255)  # #2C8C8A
OUTLINE = (20, 23, 29, 255)  # #14171D

MACHINES = (
    "digital_macerator",
    "digital_compressor",
    "digital_metal_extruder",
    "digital_metal_roller",
    "digital_electric_furnace",
    "digital_avaritia_assembler",
    "digital_neutron_compressor",
)

# icon area: above the code screen, which starts at y=14
ICON_X, ICON_Y = 11, 3


def rect(draw, x, y, width, height, colour):
    draw.rectangle([x, y, x + width - 1, y + height - 1], fill=colour)


def ingot(draw, x, y):
    """Слиток: трапеция с бликом сверху и тенью снизу."""
    rect(draw, x + 1, y + 2, 8, 1, BODY)
    rect(draw, x, y + 3, 10, 3, BODY)
    rect(draw, x + 1, y + 2, 7, 1, LIGHT)
    rect(draw, x, y + 5, 10, 1, SHADE)
    draw.point((x, y + 3), fill=OUTLINE)
    draw.point((x + 9, y + 3), fill=OUTLINE)


def dust(draw, x, y):
    """Пыль: горка крошки с бликом."""
    rect(draw, x + 1, y + 5, 8, 2, BODY)
    rect(draw, x + 2, y + 4, 6, 1, BODY)
    rect(draw, x + 3, y + 3, 4, 1, LIGHT)
    rect(draw, x, y + 7, 10, 1, SHADE)
    draw.point((x + 2, y + 5), fill=SHADE)
    draw.point((x + 7, y + 5), fill=SHADE)


def compressed(draw, x, y):
    """Сжатый блок: куб со слоями."""
    rect(draw, x + 1, y + 2, 8, 5, BODY)
    rect(draw, x + 1, y + 2, 8, 1, LIGHT)
    rect(draw, x + 1, y + 6, 8, 1, SHADE)
    draw.line([x + 2, y + 4, x + 7, y + 4], fill=SHADE)
    draw.rectangle([x + 1, y + 2, x + 8, y + 6], outline=OUTLINE)


def rod(draw, x, y):
    """Пруток: заготовка, выходящая из фильеры."""
    rect(draw, x, y + 3, 3, 4, SHADE)
    rect(draw, x + 3, y + 4, 6, 2, BODY)
    rect(draw, x + 3, y + 4, 6, 1, LIGHT)
    draw.rectangle([x, y + 3, x + 2, y + 6], outline=OUTLINE)


def plate(draw, x, y):
    """Лист: широкая плита с бликом по верхней кромке."""
    rect(draw, x, y + 3, 10, 4, BODY)
    rect(draw, x, y + 3, 10, 1, LIGHT)
    rect(draw, x, y + 6, 10, 1, SHADE)
    draw.rectangle([x, y + 3, x + 9, y + 6], outline=OUTLINE)


def grid9(draw, x, y):
    """Сетка 9×9: три на три клетки, как у сборщика Avaritia."""
    for row in range(3):
        for col in range(3):
            rect(draw, x + col * 3, y + 2 + row * 3, 2, 2, LIGHT if (row + col) % 2 == 0 else BODY)


def singularity(draw, x, y):
    """Сингулярность: тёмное ядро в бирюзовом кольце с лучами."""
    rect(draw, x + 2, y + 2, 6, 6, SHADE)
    rect(draw, x + 3, y + 3, 4, 4, OUTLINE)
    draw.point((x + 4, y + 4), fill=SHADE)
    rect(draw, x + 4, y, 2, 2, LIGHT)
    rect(draw, x + 4, y + 8, 2, 2, LIGHT)
    rect(draw, x, y + 4, 2, 2, LIGHT)
    rect(draw, x + 8, y + 4, 2, 2, LIGHT)


ICONS = {
    "digital_macerator": dust,
    "digital_compressor": compressed,
    "digital_metal_extruder": rod,
    "digital_metal_roller": plate,
    "digital_electric_furnace": ingot,
    "digital_avaritia_assembler": grid9,
    "digital_neutron_compressor": singularity,
}


def build(machine):
    with Image.open(BASE) as source:
        image = source.convert("RGBA").copy()
    draw = ImageDraw.Draw(image)
    ICONS[machine](draw, ICON_X, ICON_Y)
    return image


def main():
    for machine in MACHINES:
        image = build(machine)
        for folder in (BLOCKS, REFERENCE):
            if not os.path.isdir(folder):
                continue
            image.save(os.path.join(folder, f"{machine}_front.png"))
        print("written", machine)

    sheet = Image.new("RGBA", (len(MACHINES) * (32 * 6 + 6) + 6, 32 * 6 + 12), (24, 26, 30, 255))
    for index, machine in enumerate(MACHINES):
        cut = build(machine).resize((32 * 6, 32 * 6), Image.NEAREST)
        sheet.alpha_composite(cut, (6 + index * (32 * 6 + 6), 6))
    sheet.save(os.path.join(ROOT, "docs", "preview", "digital_fronts_preview.png"))
    print("preview written")


if __name__ == "__main__":
    main()
