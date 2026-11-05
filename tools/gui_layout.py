"""Screen layouts shared by the GUI generators, the preview tool and the Java code.

The slot coordinates are the ones the Container classes really add, so everything
downstream (texture generation, preview overlay, alignment check) uses one copy.
If a Container changes, edit it here.

Every screen is now built the same way: a content panel on top and a 176x98
player inventory panel 8px below it. Two render styles differ only in what the
content panel contains:

* ``machine`` - our machines: 200x140 with a progress bar, upgrade well, flow
  arrow and info badge.
* ``panel`` - AE2/Avaritia and chest screens: title, slot groups sunk into
  recesses, optional dividers.

The inventory panel of a panel screen is centred under its content panel:
``inv_x = (width - INV_W) // 2``.
"""

from __future__ import annotations

SLOT = 18            # vanilla slot pitch

MACHINE_W = 200      # machine panel width
MACHINE_H = 140      # machine panel height

INV_W = 176
INV_H = 98
INV_GAP = 8          # transparent gap between content and inventory panels
INV_DIVIDER_Y = 64   # hotbar separator inside the inventory panel

MACHINE_INV_X = (MACHINE_W - INV_W) // 2   # 12

GUI_W = MACHINE_W        # GuiContainer xSize for machine screens
GUI_H = MACHINE_H + INV_GAP + INV_H  # 246

# Progress bar drawn by BerlitGui: outer frame and the trough inside it.
BAR_FRAME = (12, 62, 188, 74)
BAR_TROUGH = (14, 64, 186, 72)

# Info badge in the bottom-right corner of the machine panel: (x, y, size).
INFO_ICON = (176, 116, 14)


def grid(origin_x, origin_y, cols, rows):
    """Slot (x, y) the way a Container computes them: 26 + (i % 3) * 18."""
    return [
        (origin_x + SLOT * (i % cols), origin_y + SLOT * (i // cols))
        for i in range(cols * rows)
    ]


def player_inventory(x0=20, top=166, hotbar=222):
    """3 x 9 main inventory plus the hotbar."""
    slots = [(x0 + SLOT * col, top + SLOT * row) for row in range(3) for col in range(9)]
    slots += [(x0 + SLOT * col, hotbar) for col in range(9)]
    return slots


def machine_upgrades(count=4):
    """Upgrade slots under the progress bar, centred on the machine panel."""
    x0 = (MACHINE_W - SLOT * 4) // 2
    return [(x0 + SLOT * (i % 4), 92 + SLOT * (i // 4)) for i in range(count)]


def inventory_slots(width):
    """Player inventory for a panel of the given width, plus the label position."""
    inv_x = (width - INV_W) // 2
    inv_top = None  # filled in by panel_screen
    return inv_x, inv_top


def panel_screen(texture, size, groups, text, recesses=(), dividers=(), arrows=(), canvas=None):
    """Content panel ``size`` with the shared inventory panel below it.

    ``groups`` and ``text`` use GUI coordinates, i.e. the content panel starts at
    (0, 0) and the inventory panel at ``(inv_x, height + INV_GAP)``.
    """
    width, height = size
    inv_x = (width - INV_W) // 2
    inv_top = height + INV_GAP
    total_h = inv_top + INV_H
    return {
        "style": "panel",
        "texture": texture,
        "canvas": canvas or (max(256, width), max(256, total_h)),
        "content": (width, height),
        "inv": (inv_x, inv_top),
        "panels": [(0, 0, width, height), (inv_x, inv_top, INV_W, INV_H)],
        "groups": groups,
        "text": text,
        "recesses": list(recesses),
        "dividers": list(dividers),
        "arrows": list(arrows),
    }


def inv_player(width, height):
    """Player inventory slots and the label position for a panel screen."""
    inv_x = (width - INV_W) // 2
    inv_top = height + INV_GAP
    slots = player_inventory(x0=inv_x + 8, top=inv_top + 18, hotbar=inv_top + 74)
    label = ("inventory", inv_x + 8, inv_top + 6, 140, "container.inventory")
    return slots, label


def machine_screen(texture, groups, text):
    return {
        "style": "machine",
        "texture": texture,
        "canvas": (256, 256),
        "panels": [(0, 0, MACHINE_W, MACHINE_H), (MACHINE_INV_X, MACHINE_H + INV_GAP, INV_W, INV_H)],
        "groups": groups,
        "text": text,
    }


INVENTORY_LABEL = ("inventory", MACHINE_INV_X + 8, 154, 160, "container.inventory")

SCREENS = {
    # --- our machines: wide panel + separate inventory ---------------------
    "molecular_transformer": machine_screen(
        "gui/molecular_transformer.png",
        [
            ("input", grid(38, 22, 3, 2)),
            ("output", grid(110, 22, 3, 2)),
            ("upgrade", machine_upgrades(4)),
            ("player", player_inventory()),
        ],
        [
            ("title", 8, 6, 180, "tile.berlit.molecular_transformer.name"),
            ("energy", 12, 78, 100, "berlit.gui.energy"),
            ("progress", 130, 78, 58, None),
            INVENTORY_LABEL,
        ],
    ),
    "spawner": machine_screen(
        "gui/spawner.png",
        [
            ("entity", [(38, 22)]),
            ("upgrade", machine_upgrades(7)),
            ("player", player_inventory()),
        ],
        [
            ("title", 8, 6, 180, "tile.berlit.spawner.name"),
            ("entities", 12, 78, 100, "berlit.gui.spawner.entities"),
            ("cycle", 130, 78, 58, None),
            INVENTORY_LABEL,
        ],
    ),
    "spawner_t2": machine_screen(
        "gui/spawner_t2.png",
        [
            ("entity", grid(20, 22, 5, 2)),
            ("upgrade", machine_upgrades(7)),
            ("player", player_inventory()),
        ],
        [
            ("title", 8, 6, 180, "tile.berlit.spawner_t2.name"),
            ("entities", 12, 78, 100, "berlit.gui.spawner.entities"),
            ("cycle", 130, 78, 58, None),
            INVENTORY_LABEL,
        ],
    ),
    # --- AE2 / Avaritia screens -------------------------------------------
    "avaritia_assembler": panel_screen(
        "gui/avaritia_assembler.png",
        (176, 90),
        [
            ("craft", grid(44, 20, 3, 3)),
            ("player", inv_player(176, 90)[0]),
        ],
        [
            ("title", 8, 6, 160, "tile.berlit.avaritia_assembler.name"),
            inv_player(176, 90)[1],
        ],
        recesses=[(42, 18, 98, 74)],
    ),
    "pattern_encoder": panel_screen(
        "gui/pattern_encoder.png",
        (220, 196),
        [
            ("craft", grid(8, 24, 9, 9)),
            ("output", [(190, 96)]),
            ("player", inv_player(220, 196)[0]),
        ],
        [
            ("title", 8, 6, 200, "tile.berlit.pattern_encoder.name"),
            inv_player(220, 196)[1],
        ],
        recesses=[(6, 22, 170, 184), (188, 94, 208, 114)],
        arrows=[(178, 104)],
        canvas=(256, 512),
    ),
    "singularity_encoder": panel_screen(
        "gui/singularity_encoder.png",
        (176, 76),
        [
            ("input", [(44, 20)]),
            ("output", [(116, 20)]),
            ("player", inv_player(176, 76)[0]),
        ],
        [
            ("title", 8, 6, 160, "tile.berlit.singularity_encoder.name"),
            inv_player(176, 76)[1],
        ],
        recesses=[(42, 18, 134, 38)],
        arrows=[(86, 28)],
    ),
    "neutron_compressor": panel_screen(
        "gui/neutron_compressor.png",
        (176, 90),
        [
            ("input", [(44, 20)]),
            ("pattern", [(80, 20)]),
            ("output", [(116, 20)]),
            ("player", inv_player(176, 90)[0]),
        ],
        [
            ("title", 8, 6, 160, "tile.berlit.neutron_compressor.name"),
            inv_player(176, 90)[1],
        ],
        recesses=[(42, 18, 134, 38)],
        arrows=[(69, 28), (105, 28)],
    ),
    "super_chest": panel_screen(
        "gui/super_chest.png",
        (502, 190),
        [
            ("chest", grid(8, 18, 27, 9)),
            ("player", inv_player(502, 190)[0]),
        ],
        [
            ("title", 8, 6, 200, "tile.berlit.super_chest.name"),
            inv_player(502, 190)[1],
        ],
        recesses=[(6, 16, 496, 178)],
        canvas=(512, 512),
    ),
}

# NEI recipe pages are separate 176x166 backgrounds; the positions here mirror the
# PositionedStack coordinates in compat/nei/*NEI.java.
NEI_PAGES = {
    "machines": {
        "texture": "gui/nei_spawner.png",
        "page": (176, 166),
        "items": [(8, 20), (50, 20), (110, 20)],
        "text": [(8, 6, 140)],
    },
}
