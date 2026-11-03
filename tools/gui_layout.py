"""Screen layouts shared by the GUI generators, the preview tool and the Java code.

The slot coordinates are the ones the Container classes really add, so everything
downstream (texture generation, preview overlay, alignment check) uses one copy.
If a Container or a TileEntity*Spawner changes, edit it here.

Geometry
--------
The machine panel is wider than the inventory panel, which sits centred under it,
separated by an 8px transparent gap:

    rows   0..139   machine panel   200x140, at x=0
    rows 140..147   transparent gap
    rows 148..245   inventory panel 176x98, at x=12

Both panels are cut from one 256x256 texture, so the gap stays transparent and
the world shows through. Inside a panel the usable area starts 6px in from the
frame.
"""

from __future__ import annotations

SLOT = 18            # vanilla slot pitch

MACHINE_W = 200      # machine panel width
MACHINE_H = 140      # machine panel height

INV_X = 12           # inventory panel is centred under the machine panel
INV_W = 176
INV_TOP = 148        # 8px gap after the machine panel
INV_H = 98

GUI_W = MACHINE_W        # GuiContainer xSize
GUI_H = INV_TOP + INV_H  # GuiContainer ySize = 246

# (x, y, width, height) of each panel inside the texture; the gap stays transparent.
PANELS = [
    (0, 0, MACHINE_W, MACHINE_H),
    (INV_X, INV_TOP, INV_W, INV_H),
]

# Progress bar drawn by BerlitGui: outer frame and the trough inside it.
BAR_FRAME = (12, 62, 188, 74)
BAR_TROUGH = (14, 64, 186, 72)

# Info badge in the bottom-right corner of the machine panel: (x, y, size).
INFO_ICON = (176, 116, 14)

INVENTORY_DIVIDER_Y = 212


def grid(origin_x, origin_y, cols, rows):
    """Slot (x, y) the way a Container computes them: 26 + (i % 3) * 18."""
    return [
        (origin_x + SLOT * (i % cols), origin_y + SLOT * (i // cols))
        for i in range(cols * rows)
    ]


def player_inventory():
    """3 x 9 main inventory plus the hotbar, inside the lower panel."""
    x0 = INV_X + 8
    slots = [(x0 + SLOT * col, 166 + SLOT * row) for row in range(3) for col in range(9)]
    slots += [(x0 + SLOT * col, 222) for col in range(9)]
    return slots


def machine_upgrades(count=4):
    """Upgrade slots under the progress bar, centred on the machine panel."""
    x0 = (MACHINE_W - SLOT * 4) // 2
    return [(x0 + SLOT * (i % 4), 92 + SLOT * (i // 4)) for i in range(count)]


def screen(texture, groups, text):
    return {"texture": texture, "groups": groups, "text": text}


INVENTORY_LABEL = ("inventory", INV_X + 8, 154, 160, "container.inventory")

SCREENS = {
    "molecular_transformer": screen(
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
    "spawner": screen(
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
    "spawner_t2": screen(
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
