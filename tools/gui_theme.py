"""Visual theme for the Berlit GUI: dark slate panels with cyan trim.

The screen is two panels drawn from one texture - the machine on top and the
player inventory below - so the theme works on one panel image at a time and
the generator places them at their vertical offsets.

One place defines every colour and primitive, and the preview/lint tool compares
the finished texture against the exact slot tile this module draws.
"""

from __future__ import annotations

from PIL import Image, ImageDraw

from gui_layout import BAR_FRAME, BAR_TROUGH, SLOT

# --- palette ---------------------------------------------------------------

BG_TOP = (54, 59, 72)
BG_BOTTOM = (38, 42, 52)

FRAME_OUTER = (12, 14, 18)
FRAME_CYAN = (44, 140, 138)
FRAME_LIGHT = (111, 233, 224)

PLATE = (33, 37, 46)
PLATE_DARK = (28, 32, 40)
EDGE_DARK = (20, 23, 29)
EDGE_LIGHT = (61, 68, 84)

SLOT_FILL = (35, 39, 47)
SLOT_EDGE_DARK = (18, 21, 26)
SLOT_EDGE_LIGHT = (60, 67, 82)

TROUGH = (18, 21, 27)
TROUGH_EDGE = (12, 14, 18)
BAR_FILL = (79, 214, 205)     # matches GuiTheme.ACCENT at full brightness
BAR_FILL_EDGE = (140, 246, 238)


def lerp(a, b, t):
    return tuple(round(a[i] + (b[i] - a[i]) * t) for i in range(3))


def slot_tile():
    """The canonical 18x18 slot background.

    Vanilla draws a slot background at (slot_x - 1, slot_y - 1), so pasting this
    tile at exactly that offset is what makes the texture line up with the code.
    """
    tile = Image.new("RGBA", (SLOT, SLOT), SLOT_FILL + (255,))
    draw = ImageDraw.Draw(tile)
    draw.line([(0, 0), (SLOT - 1, 0)], fill=SLOT_EDGE_DARK + (255,))
    draw.line([(0, 0), (0, SLOT - 1)], fill=SLOT_EDGE_DARK + (255,))
    draw.line([(0, SLOT - 1), (SLOT - 1, SLOT - 1)], fill=SLOT_EDGE_LIGHT + (255,))
    draw.line([(SLOT - 1, 0), (SLOT - 1, SLOT - 1)], fill=SLOT_EDGE_LIGHT + (255,))
    draw.rectangle([1, 1, SLOT - 2, SLOT - 2], fill=SLOT_FILL + (255,))
    return tile


TILE = slot_tile()


# --- primitives ------------------------------------------------------------

def new_panel(width, height):
    """Panel canvas with a vertical background gradient."""
    image = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)
    for y in range(height):
        draw.line([(0, y), (width - 1, y)], fill=lerp(BG_TOP, BG_BOTTOM, y / max(1, height - 1)) + (255,))
    return image


def draw_frame(image):
    width, height = image.size
    draw = ImageDraw.Draw(image)
    draw.rectangle([0, 0, width - 1, height - 1], outline=FRAME_OUTER + (255,))
    draw.rectangle([1, 1, width - 2, height - 2], outline=FRAME_CYAN + (255,))
    draw.rectangle([2, 2, width - 3, height - 3], outline=FRAME_CYAN + (255,))
    draw.rectangle([3, 3, width - 4, height - 4], outline=FRAME_OUTER + (255,))


def draw_inner_shadow(image):
    """Two rows/columns of darkening just inside the frame: the panel reads recessed."""
    width, height = image.size
    draw = ImageDraw.Draw(image)
    for offset, colour in ((4, (36, 40, 50)), (5, (44, 48, 60))):
        draw.line([(4, offset), (width - 5, offset)], fill=colour + (255,))
        draw.line([(offset, 4), (offset, height - 5)], fill=colour + (255,))
    for offset, colour in ((4, (70, 77, 92)), (5, (58, 64, 78))):
        draw.line([(4, height - 1 - offset), (width - 5, height - 1 - offset)], fill=colour + (255,))
        draw.line([(width - 1 - offset, 4), (width - 1 - offset, height - 5)], fill=colour + (255,))


def draw_corner_brackets(image):
    """Bright L-shaped accents in the four corners, like the reference art."""
    width, height = image.size
    draw = ImageDraw.Draw(image)
    inset = 4
    arm = 14
    for x, y, dx, dy in (
        (inset, inset, 1, 1),
        (width - 1 - inset, inset, -1, 1),
        (inset, height - 1 - inset, 1, -1),
        (width - 1 - inset, height - 1 - inset, -1, -1),
    ):
        draw.line([(x, y), (x + dx * arm, y)], fill=FRAME_LIGHT + (255,))
        draw.line([(x, y), (x, y + dy * arm)], fill=FRAME_LIGHT + (255,))
        draw.point([(x + dx, y), (x, y + dy), (x, y), (x + dx * 2, y + dy * 2)],
                   fill=FRAME_LIGHT + (255,))


def draw_title_strip(image):
    """Dark band for the screen title, with a glowing cyan underline."""
    width = image.size[0]
    overlay = Image.new("RGBA", (width - 8, 11), (26, 29, 36, 150))
    image.alpha_composite(overlay, (4, 4))
    draw = ImageDraw.Draw(image)
    draw.line([(4, 15), (width - 5, 15)], fill=FRAME_CYAN + (255,))
    # ImageDraw writes raw RGBA instead of blending, so translucent accents have
    # to be pre-blended to an opaque colour or they punch alpha holes.
    draw.line([(4, 16), (width - 5, 16)], fill=(56, 104, 104, 255))
    for x in (4, 5, width - 6, width - 5):
        draw.point((x, 15), fill=FRAME_LIGHT + (255,))


def draw_progress_bar(image):
    """Empty bar frame + trough. The fill is drawn by BerlitGui at runtime."""
    x0, y0, x1, y1 = BAR_FRAME
    draw = ImageDraw.Draw(image)
    draw.rectangle([x0, y0, x1, y1], fill=(46, 51, 64, 255), outline=EDGE_DARK + (255,))
    draw.line([(x0 + 1, y0 + 1), (x1 - 1, y0 + 1)], fill=(74, 82, 100, 255))
    draw.line([(x0 + 1, y1 - 1), (x1 - 1, y1 - 1)], fill=(24, 27, 34, 255))

    tx0, ty0, tx1, ty1 = BAR_TROUGH
    draw.rectangle([tx0, ty0, tx1, ty1], fill=TROUGH + (255,))
    # Faint ticks so an empty bar still reads as a track rather than a hole.
    for x in range(tx0 + 8, tx1 - 2, 8):
        draw.line([(x, ty0 + 1), (x, ty1 - 1)], fill=(32, 37, 46, 255))
    draw.line([(tx0, ty0), (tx1, ty0)], fill=(10, 12, 16, 255))
    draw.line([(tx0, ty0), (tx0, ty1)], fill=(10, 12, 16, 255))
    draw.line([(tx0, ty1), (tx1, ty1)], fill=(46, 52, 64, 255))


def draw_recess(image, box):
    """Sunk panel: used for slot groups so they read as a separate block."""
    x0, y0, x1, y1 = box
    draw = ImageDraw.Draw(image)
    draw.rectangle([x0, y0, x1, y1], fill=PLATE_DARK + (255,), outline=EDGE_DARK + (255,))
    draw.line([(x0 + 1, y0 + 1), (x1 - 1, y0 + 1)], fill=(44, 49, 61, 255))
    draw.line([(x0 + 1, y0 + 1), (x0 + 1, y1 - 1)], fill=(44, 49, 61, 255))
    draw.line([(x0 + 1, y1 - 1), (x1 - 1, y1 - 1)], fill=(24, 27, 34, 255))
    draw.line([(x1 - 1, y0 + 1), (x1 - 1, y1 - 1)], fill=(24, 27, 34, 255))


def draw_divider(image, y, x0=8, x1=None):
    """Thin separator between a machine area and the player inventory."""
    if x1 is None:
        x1 = image.size[0] - 9
    draw = ImageDraw.Draw(image)
    draw.line([(x0, y), (x1, y)], fill=EDGE_DARK + (255,))
    draw.line([(x0, y + 1), (x1, y + 1)], fill=(48, 54, 66, 255))


def draw_inventory_divider(image, y):
    """Separator between the main inventory rows and the hotbar."""
    width = image.size[0]
    draw = ImageDraw.Draw(image)
    draw.line([(8, y), (width - 9, y)], fill=EDGE_DARK + (255,))
    draw.line([(8, y + 1), (width - 9, y + 1)], fill=(48, 54, 66, 255))


def draw_flow_arrow(image, cx, cy):
    """Two cyan chevrons showing the input -> output direction."""
    draw = ImageDraw.Draw(image)
    for offset in (0, 6):
        draw.line([(cx + offset - 5, cy - 5), (cx + offset, cy), (cx + offset - 5, cy + 5)],
                  fill=FRAME_CYAN + (255,))
        draw.line([(cx + offset - 4, cy - 4), (cx + offset, cy), (cx + offset - 4, cy + 4)],
                  fill=FRAME_LIGHT + (255,))


def draw_info_icon(image, box):
    """Small 'i' badge; the Gui draws its tooltip on hover."""
    x, y, size = box
    draw = ImageDraw.Draw(image)
    draw.rectangle([x, y, x + size - 1, y + size - 1], fill=(30, 34, 43, 255), outline=FRAME_CYAN + (255,))
    draw.line([(x + 1, y + 1), (x + size - 2, y + 1)], fill=(56, 104, 104, 255))

    cx = x + size // 2
    draw.line([(cx, y + 4), (cx, y + 5)], fill=FRAME_LIGHT + (255,))          # dot of the i
    draw.line([(cx, y + 7), (cx, y + size - 4)], fill=FRAME_LIGHT + (255,))   # stem
    draw.line([(cx - 1, y + size - 4), (cx + 1, y + size - 4)], fill=FRAME_LIGHT + (255,))


def paste_slots(image, slots, offset_x=0, offset_y=0):
    for slot_x, slot_y in slots:
        image.alpha_composite(TILE, (slot_x - 1 - offset_x, slot_y - 1 - offset_y))
