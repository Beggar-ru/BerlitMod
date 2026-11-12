"""Copies hand-redrawn files from docs/reference/materia back into the mod assets.

    python tools\\import_reference_textures.py

Edit the PNGs in docs/reference/materia, run this, then build. Files keep their original asset
names, so the mapping below is the only thing that needs to know where they belong.
"""

from __future__ import annotations

import os
import shutil

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
REFERENCE = os.path.join(ROOT, "docs", "reference", "materia")
BLOCKS = os.path.join(ROOT, "src", "main", "resources", "assets", "berlit", "textures", "blocks")
ITEMS = os.path.join(ROOT, "src", "main", "resources", "assets", "berlit", "textures", "items")

# reference sub-folder -> target texture folder
GROUPS = {
    "macerator": BLOCKS,
    "spawners": BLOCKS,
    "matter_generators": BLOCKS,
    "matter_items": ITEMS,
}

# earlier redraws used _up_botton; accept that spelling and store the corrected name
RENAME = {
    "matter_generator_{t}_up_botton.png".format(t=type_id): "matter_generator_{t}_up_bottom.png".format(t=type_id)
    for type_id in ("standard", "violet", "turquoise", "emerald", "azure", "amber", "crimson", "radiant")
}


def main():
    copied = 0
    for group, target_dir in GROUPS.items():
        source_dir = os.path.join(REFERENCE, group)
        if not os.path.isdir(source_dir):
            continue
        for file_name in sorted(os.listdir(source_dir)):
            if not file_name.lower().endswith(".png"):
                continue
            source = os.path.join(source_dir, file_name)
            target = os.path.join(target_dir, RENAME.get(file_name, file_name))
            if os.path.abspath(source) == os.path.abspath(target):
                continue
            shutil.copyfile(source, target)
            copied += 1
            print(f"{group}/{file_name} -> {os.path.relpath(target, ROOT)}")
    print(f"copied {copied} files")


if __name__ == "__main__":
    main()
