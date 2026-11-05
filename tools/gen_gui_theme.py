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
    """The shared 176x98 inventory panel, from whichever panel slot it occupies."""
    inv_x, inv_y, inv_w, inv_h = screen["panels"][1]
    image = theme.new_panel(inv_w, inv_h)
    theme.draw_inventory_divider(image, layout.INV_DIVIDER_Y)
    theme.paste_slots(image, slots_of(screen, "player"), offset_x=inv_x, offset_y=inv_y)
    theme.draw_frame(image)
    theme.draw_inner_shadow(image)
    theme.draw_corner_brackets(image)
    return image


def build_panel(screen, rect, first):
    """One panel of a ``panel`` screen; elements are clipped to its bounds."""
    panel_x, panel_y, panel_w, panel_h = rect
    image = theme.new_panel(panel_w, panel_h)

    if first:
        theme.draw_title_strip(image)

    for x0, y0, x1, y1 in screen.get("recesses", ()):
        if panel_x <= x0 and panel_y <= y0 and x1 < panel_x + panel_w and y1 < panel_y + panel_h:
            theme.draw_recess(image, (x0 - panel_x, y0 - panel_y, x1 - panel_x, y1 - panel_y))

    for y in screen.get("dividers", ()):
        if panel_y <= y < panel_y + panel_h:
            theme.draw_divider(image, y - panel_y)

    for arrow_x, arrow_y in screen.get("arrows", ()):
        if panel_x <= arrow_x < panel_x + panel_w and panel_y <= arrow_y < panel_y + panel_h:
            theme.draw_flow_arrow(image, arrow_x - panel_x, arrow_y - panel_y)

    inside = [slot for slot in all_slots(screen) if in_panel(slot, rect)]
    theme.paste_slots(image, inside, offset_x=panel_x, offset_y=panel_y)

    theme.draw_frame(image)
    theme.draw_inner_shadow(image)
    theme.draw_corner_brackets(image)
    return image


def in_panel(slot, rect):
    panel_x, panel_y, panel_w, panel_h = rect
    x, y = slot[0] - 1, slot[1] - 1
    return panel_x <= x and panel_y <= y and x + layout.SLOT <= panel_x + panel_w \
        and y + layout.SLOT <= panel_y + panel_h



def build_flat_panel(screen, rect, first):
    """Borderless variant: flat slate, cells with a light frame, no outer decoration."""
    panel_x, panel_y, panel_w, panel_h = rect
    image = theme.new_flat_panel(panel_w, panel_h)

    if first:
        theme.draw_title_rule(image)

    inside = [slot for slot in all_slots(screen) if in_panel(slot, rect)]
    theme.paste_cells(image, inside, offset_x=panel_x, offset_y=panel_y)
    return image


def build(screen):
    canvas = Image.new("RGBA", screen["canvas"], (0, 0, 0, 0))
    for index, rect in enumerate(screen["panels"]):
        if screen["style"] == "machine":
            panel = build_machine(screen) if index == 0 else build_inventory(screen)
        elif screen.get("variant") == "flat":
            panel = build_flat_panel(screen, rect, index == 0)
        else:
            panel = build_panel(screen, rect, index == 0)
        canvas.alpha_composite(panel, (rect[0], rect[1]))
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
