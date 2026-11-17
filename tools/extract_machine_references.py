"""Collects the machine block textures into two reference folders for redrawing.

    python tools\\extract_machine_references.py

docs/reference/mecha     - ordinary machines
docs/reference/mecha_ae  - digital machines and AE blocks

Both folders stay out of git; only the copies in src/main/resources are shipped.
"""

from __future__ import annotations

import os
import shutil

from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
BLOCKS = os.path.join(ROOT, "src", "main", "resources", "assets", "berlit", "textures", "blocks")
REFERENCE = os.path.join(ROOT, "docs", "reference")

# prefix -> folder; the first matching prefix wins, so keep the specific ones first
GROUPS = [
    ("mecha_ae", ("digital_", "crafting_", "big_interface")),
    (
        "mecha",
        (
            "machine_",
            "replicator_",
            "molecular_transformer",
            "molecular_farm",
            "fast_",
            "cobble_",
            "super_",
            "test_",
            "nether_star",
            "pattern_encoder",
            "singularity_encoder",
            "avaritia_",
        ),
    ),
]

TITLES = {
    "mecha": "Обычные машины",
    "mecha_ae": "Цифровые машины и AE-блоки",
}


def classify(file_name):
    for folder, prefixes in GROUPS:
        if any(file_name.startswith(prefix) for prefix in prefixes):
            return folder
    return None


def load_font(size):
    """Arial has Cyrillic; the Pillow default renders the Russian title as boxes."""
    from PIL import ImageFont

    for candidate in (r"C:\Windows\Fonts\arial.ttf", r"C:\Windows\Fonts\segoeui.ttf"):
        if os.path.isfile(candidate):
            try:
                return ImageFont.truetype(candidate, size)
            except OSError:
                continue
    return ImageFont.load_default()


def contact_sheet(folder, files):
    scale = 4
    columns = 8
    cell = 32 * scale
    rows = (len(files) + columns - 1) // columns
    sheet = Image.new("RGBA", (columns * (cell + 8) + 8, rows * (cell + 18) + 30), (24, 26, 30, 255))
    draw = ImageDraw.Draw(sheet)
    font = load_font(13)

    draw.text((8, 6), f"{TITLES.get(folder, folder)} — {len(files)} текстур", fill=(230, 235, 240, 255), font=font)
    for index, path in enumerate(files):
        row, col = divmod(index, columns)
        x = 8 + col * (cell + 8)
        y = 26 + row * (cell + 18)
        with Image.open(path) as source:
            image = source.convert("RGBA")
        image = image.resize((cell, cell), Image.NEAREST)
        sheet.alpha_composite(image, (x, y))
    sheet.save(os.path.join(REFERENCE, folder, "contact_sheet.png"))


def main():
    buckets = {}
    for file_name in sorted(os.listdir(BLOCKS)):
        if not file_name.lower().endswith(".png"):
            continue
        folder = classify(file_name)
        if folder is None:
            continue
        buckets.setdefault(folder, []).append(os.path.join(BLOCKS, file_name))

    for folder, paths in buckets.items():
        target = os.path.join(REFERENCE, folder)
        os.makedirs(target, exist_ok=True)
        for path in paths:
            shutil.copyfile(path, os.path.join(target, os.path.basename(path)))
        contact_sheet(folder, sorted(paths))
        print(f"{folder}: {len(paths)} текстур")


if __name__ == "__main__":
    main()
