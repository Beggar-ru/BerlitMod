"""Draws the Berlit GUI textures in the dark slate + cyan theme.

    python tools/gen_gui_theme.py

Each screen is two panels - the machine, wider, on top, and the player inventory
centred below - composed into one 256x256 texture. Slots land on the coordinates
from tools/gui_layout.py, i.e. exactly where the Container classes put them, and
tools/preview.py check verifies that afterwards.
"""

from __future__ import annotations

import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import gui_layout as layout  # noqa: E402
import gui_theme as theme  # noqa: E402
from PIL import Image  # noqa: E402

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TEXTURES = os.path.join(ROOT, "src", "main", "resources", "assets", "berlit", "textures")


def slots_of(screen, *groups):
    return [slot for group, slots in screen["groups"] if group in groups for slot in slots]


def well_box(slots, pad=4):
    """Recess around the upgrade slots, sized to the rows actually used."""
    xs = [x for x, _ in slots]
    ys = [y for _, y in slots]
    return (min(xs) - pad, min(ys) - pad, max(xs) + layout.SLOT - 2 + pad, max(ys) + layout.SLOT - 2 + pad)


def build_machine(screen):
    image = theme.new_panel(layout.MACHINE_W, layout.MACHINE_H)
    theme.draw_title_strip(image)
    theme.draw_progress_bar(image)

    upgrades = slots_of(screen, "upgrade")
    if upgrades:
        theme.draw_upgrade_well(image, well_box(upgrades))

    if slots_of(screen, "input"):
        theme.draw_flow_arrow(image, layout.MACHINE_W // 2, 40)

    theme.paste_slots(image, slots_of(screen, "input", "output", "upgrade", "entity"))
    theme.draw_info_icon(image, layout.INFO_ICON)
    theme.draw_frame(image)
    theme.draw_inner_shadow(image)
    theme.draw_corner_brackets(image)
    return image


def build_inventory(screen):
    image = theme.new_panel(layout.INV_W, layout.INV_H)
    theme.draw_inventory_divider(image, layout.INVENTORY_DIVIDER_Y - layout.INV_TOP)
    theme.paste_slots(
        image,
        slots_of(screen, "player"),
        offset_x=layout.INV_X,
        offset_y=layout.INV_TOP)
    theme.draw_frame(image)
    theme.draw_inner_shadow(image)
    theme.draw_corner_brackets(image)
    return image


def build(screen):
    canvas = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
    canvas.alpha_composite(build_machine(screen), (0, 0))
    canvas.alpha_composite(build_inventory(screen), (layout.INV_X, layout.INV_TOP))
    return canvas


def main():
    for name, screen in layout.SCREENS.items():
        out = os.path.join(TEXTURES, *screen["texture"].split("/"))
        os.makedirs(os.path.dirname(out), exist_ok=True)
        build(screen).save(out)
        print(f"written {screen['texture']}  ({name})")
    return 0


if __name__ == "__main__":
    sys.exit(main())
