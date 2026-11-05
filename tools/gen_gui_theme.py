"""Draws the Berlit GUI textures in the dark slate + cyan theme.

    python tools/gen_gui_theme.py

Screens come from tools/gui_layout.py, so slots land exactly where the Container
classes put them. Machine screens are two panels (machine + inventory) with a
transparent gap; the AE2/Avaritia and chest screens are a single panel of the
size their GuiContainer uses. tools/preview.py check verifies the result.
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


def all_slots(screen):
    return [slot for _group, slots in screen["groups"] for slot in slots]


def well_box(slots, pad=4):
    """Recess around a group of slots, sized to the rows actually used."""
    xs = [x for x, _ in slots]
    ys = [y for _, y in slots]
    return (min(xs) - pad, min(ys) - pad, max(xs) + layout.SLOT - 2 + pad, max(ys) + layout.SLOT - 2 + pad)


def build_machine(screen):
    image = theme.new_panel(layout.MACHINE_W, layout.MACHINE_H)
    theme.draw_title_strip(image)
    theme.draw_progress_bar(image)

    upgrades = slots_of(screen, "upgrade")
    if upgrades:
        theme.draw_recess(image, well_box(upgrades))

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


def build_panel(screen, width, height):
    """Single-panel screens: same palette, no progress bar or inventory split."""
    image = theme.new_panel(width, height)
    theme.draw_title_strip(image)

    for x0, y0, x1, y1 in screen.get("recesses", ()):
        theme.draw_recess(image, (x0, y0, x1, y1))

    for y in screen.get("dividers", ()):
        theme.draw_divider(image, y)

    theme.paste_slots(image, all_slots(screen))
    theme.draw_frame(image)
    theme.draw_inner_shadow(image)
    theme.draw_corner_brackets(image)
    return image


def build(screen):
    canvas = Image.new("RGBA", screen["canvas"], (0, 0, 0, 0))
    for index, (panel_x, panel_y, panel_w, panel_h) in enumerate(screen["panels"]):
        if screen["style"] == "machine":
            panel = build_machine(screen) if index == 0 else build_inventory(screen)
        else:
            panel = build_panel(screen, panel_w, panel_h)
        canvas.alpha_composite(panel, (panel_x, panel_y))
    return canvas


def main():
    for name, screen in layout.SCREENS.items():
        out = os.path.join(TEXTURES, *screen["texture"].split("/"))
        os.makedirs(os.path.dirname(out), exist_ok=True)
        build(screen).save(out)
        print(f"written {screen['texture']}  ({name}) {screen['canvas']}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
