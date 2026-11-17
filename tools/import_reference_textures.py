"""Copies hand-redrawn files from docs/reference back into the mod assets.

    python tools\\import_reference_textures.py

Edit the PNGs in docs/reference/materia, docs/reference/mecha or docs/reference/mecha_ae, run
this, then build. Files keep their original asset names, so the mapping below is the only thing
that needs to know where they belong.
"""

from __future__ import annotations

import os
import shutil

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
REFERENCE = os.path.join(ROOT, "docs", "reference")
BLOCKS = os.path.join(ROOT, "src", "main", "resources", "assets", "berlit", "textures", "blocks")
ITEMS = os.path.join(ROOT, "src", "main", "resources", "assets", "berlit", "textures", "items")

# reference folder (relative to docs/reference) -> target texture folder
GROUPS = {
    os.path.join("materia", "macerator"): BLOCKS,
    os.path.join("materia", "spawners"): BLOCKS,
    os.path.join("materia", "matter_generators"): BLOCKS,
    os.path.join("materia", "matter_items"): ITEMS,
    "mecha": BLOCKS,
    "mecha_ae": BLOCKS,
}

# earlier redraws used _up_botton; accept that spelling and store the corrected name
RENAME = {
    "matter_generator_{t}_up_botton.png".format(t=type_id): "matter_generator_{t}_up_bottom.png".format(t=type_id)
    for type_id in ("standard", "violet", "turquoise", "emerald", "azure", "amber", "crimson", "radiant")
}

# sheets are drawn by tools/extract_machine_references.py, they are not mod textures
SKIP = {"contact_sheet.png"}

# Digital machines share one set of faces: a file named "_front.png" (leading underscore) is copied
# to every entry of this list, while "digital_macerator_front.png" only replaces that machine.
DIGITAL_MACHINES = (
    "digital_macerator",
    "digital_compressor",
    "digital_metal_extruder",
    "digital_metal_roller",
    "digital_electric_furnace",
    "digital_avaritia_assembler",
    "digital_neutron_compressor",
)
DIGITAL_NEW = os.path.join("mecha_ae", "digital", "new")


def import_digital_new(copied):
    """docs/reference/mecha_ae/digital/new: shared faces and per-machine overrides."""
    source_dir = os.path.join(REFERENCE, DIGITAL_NEW)
    if not os.path.isdir(source_dir):
        return copied

    for file_name in sorted(os.listdir(source_dir)):
        if not file_name.lower().endswith(".png") or file_name in SKIP:
            continue
        source = os.path.join(source_dir, file_name)

        if file_name.startswith("_"):
            targets = [f"{machine}{file_name}" for machine in DIGITAL_MACHINES]
            note = f"shared -> {len(targets)} digital machines"
        else:
            targets = [file_name]
            note = "single machine"

        for target_name in targets:
            target = os.path.join(BLOCKS, target_name)
            shutil.copyfile(source, target)
            copied += 1
            print(f"{DIGITAL_NEW}/{file_name} -> {os.path.relpath(target, ROOT)}  ({note})")
    return copied


def main():
    copied = 0
    for group, target_dir in GROUPS.items():
        source_dir = os.path.join(REFERENCE, group)
        if not os.path.isdir(source_dir):
            continue
        for file_name in sorted(os.listdir(source_dir)):
            if not file_name.lower().endswith(".png") or file_name in SKIP:
                continue
            source = os.path.join(source_dir, file_name)
            target = os.path.join(target_dir, RENAME.get(file_name, file_name))
            if os.path.abspath(source) == os.path.abspath(target):
                continue
            shutil.copyfile(source, target)
            copied += 1
            print(f"{group}/{file_name} -> {os.path.relpath(target, ROOT)}")

    copied = import_digital_new(copied)
    print(f"copied {copied} files")


if __name__ == "__main__":
    main()
