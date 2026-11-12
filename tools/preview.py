"""Preview and lint the Berlit GUI/texture assets.

Everything here works on the real files under src/main/resources, so the
preview always shows what the game will load, and the slot overlay always
uses the coordinates the Container classes really add.

Examples
--------
    python tools/preview.py sheet              # contact sheets -> docs/preview/
    python tools/preview.py gui                # every GUI mockup with slot overlay
    python tools/preview.py gui spawner_t2     # just one
    python tools/preview.py check              # UI lints, exit code 1 on errors

Requires Pillow only (same as the gen_*.py scripts).
"""

from __future__ import annotations

import argparse
import fnmatch
import json
import os
import re
import sys
from collections import Counter

from PIL import Image, ImageDraw, ImageFont

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "src", "main", "resources", "assets", "berlit")
TEX = os.path.join(ASSETS, "textures")
LANG_DIR = os.path.join(ASSETS, "lang")
JAVA_DIR = os.path.join(ROOT, "src", "main", "java")
OUT_DIR = os.path.join(ROOT, "docs", "preview")

# Layouts and the slot tile live in gui_layout.py / gui_theme.py so the
# generators and this checker cannot drift apart. The theme's tile is the exact
# 18x18 background pasted at (slot_x - 1, slot_y - 1); `check` compares the
# texture against it instead of guessing from colour uniformity.
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from gui_layout import NEI_PAGES, SCREENS, SLOT  # noqa: E402

# Vanilla ships these; they are not expected in the mod's own lang files.
VANILLA_KEYS = {"container.inventory"}
from gui_theme import TILE as SLOT_TILE  # noqa: E402


# Placeholder substituted into lang strings before measuring their width.
TEXT_SAMPLES = {"berlit.gui.energy": "8192", "progress": "100%", "cycle": "100%"}

# Rough width of the vanilla font; good enough to flag real overflow.
_NARROW = set("il.,:;'!|`")
_MEDIUM = set("ftI[](){}/\\-")


def mc_text_width(text):
    total = 0
    for ch in text:
        if ch == " ":
            total += 4
        elif ch in _NARROW:
            total += 2
        elif ch in _MEDIUM:
            total += 4
        elif ch in "MW@#":
            total += 8
        else:
            total += 6
    return total


# --------------------------------------------------------------------------
# Helpers
# --------------------------------------------------------------------------

def font(size):
    try:
        return ImageFont.load_default(size=size)
    except TypeError:  # Pillow < 10.1
        return ImageFont.load_default()


def checkerboard(width, height, cell=8, dark=(28, 28, 32), light=(38, 38, 44)):
    """Neutral backdrop so transparent pixels are visible."""
    img = Image.new("RGBA", (width, height), dark + (255,))
    draw = ImageDraw.Draw(img)
    for y in range(0, height, cell):
        for x in range(0, width, cell):
            if (x // cell + y // cell) % 2:
                draw.rectangle([x, y, x + cell - 1, y + cell - 1], fill=light + (255,))
    return img


def texture_path(relative):
    return os.path.join(TEX, relative)


def slot_rect(slot_x, slot_y):
    """Bounds of the 18x18 slot background: vanilla draws it at (x-1, y-1)."""
    return (slot_x - 1, slot_y - 1, slot_x + SLOT - 2, slot_y + SLOT - 2)


def slot_inner(slot_x, slot_y):
    """The 16x16 fill inside the slot border, as Pillow crop bounds (right/lower exclusive)."""
    return (slot_x, slot_y, slot_x + 16, slot_y + 16)


def read_lang(name):
    path = os.path.join(LANG_DIR, name)
    values = {}
    if not os.path.isfile(path):
        return values
    with open(path, encoding="utf-8") as handle:
        for line in handle:
            line = line.strip()
            if not line or line.startswith("#") or "=" not in line:
                continue
            key, value = line.split("=", 1)
            values[key.strip()] = value.strip()
    return values


def read_java_sources():
    sources = {}
    for base, _, files in os.walk(JAVA_DIR):
        for name in files:
            if name.endswith(".java"):
                path = os.path.join(base, name)
                with open(path, encoding="utf-8") as handle:
                    sources[os.path.relpath(path, JAVA_DIR).replace("\\", "/")] = handle.read()
    return sources


# --------------------------------------------------------------------------
# Preview
# --------------------------------------------------------------------------


def draw_disk(canvas, origin_x, origin_y, scale):
    """10x10 floppy glyph, drawn pixel-wise so it stays crisp at any zoom."""
    body = (200, 205, 214)
    edge = (138, 146, 162)
    shutter = (58, 64, 80)
    label = (110, 118, 136)

    def put(offset_x, offset_y, colour):
        canvas.alpha_composite(
            Image.new("RGBA", (scale, scale), colour + (255,)),
            ((origin_x + offset_x) * scale, (origin_y + offset_y) * scale))

    for ix in range(10):
        for iy in range(10):
            put(ix, iy, edge if ix in (0, 9) or iy in (0, 9) else body)

    for ix in range(2, 7):
        for iy in range(1, 4):
            put(ix, iy, shutter)

    for ix in range(2, 8):
        for iy in range(6, 9):
            put(ix, iy, label)


def draw_cube(canvas, x, y, size, scale):
    """Same cube the NineSliceButton draws: lighter top half, darker bottom, 1px edge."""
    from PIL import ImageDraw as _ImageDraw

    draw = _ImageDraw.Draw(canvas)
    face = (200, 205, 214, 255)
    shade = (138, 146, 162, 255)
    edge = (90, 98, 114, 255)

    def rect(x0, y0, x1, y1, colour):
        draw.rectangle([x0 * scale, y0 * scale, x1 * scale - 1, y1 * scale - 1], fill=colour)

    rect(x, y, x + size, y + size, face)
    rect(x, y, x + size, y + size // 2, shade)
    rect(x, y, x + size, y + 1, edge)
    rect(x, y + size - 1, x + size, y + size, edge)
    rect(x, y, x + 1, y + size, edge)
    rect(x + size - 1, y, x + size, y + size, edge)


def draw_button(canvas, x, y, width, height, label, scale):
    """Same look as ThemedButton: dark plate, cyan border, glyph or text."""
    from PIL import ImageDraw as _ImageDraw

    draw = _ImageDraw.Draw(canvas)
    plate = (50, 58, 72, 255)
    border = (44, 140, 138, 255)
    draw.rectangle([x * scale, y * scale, (x + width) * scale - 1, (y + height) * scale - 1], fill=plate)
    draw.rectangle([x * scale, y * scale, (x + width) * scale - 1, y * scale + scale - 1], fill=border)
    draw.rectangle(
        [x * scale, (y + height) * scale - scale, (x + width) * scale - 1, (y + height) * scale - 1],
        fill=border)
    draw.rectangle([x * scale, y * scale, x * scale + scale - 1, (y + height) * scale - 1], fill=border)
    draw.rectangle(
        [(x + width) * scale - scale, y * scale, (x + width) * scale - 1, (y + height) * scale - 1],
        fill=border)

    if label == "disk":
        draw_disk(canvas, x + (width - 10) // 2, y + (height - 10) // 2, scale)
    elif label == "one":
        draw_cube(canvas, x + (width - 8) // 2, y + (height - 8) // 2, 8, scale)
    elif label == "stack":
        base_x = x + (width - 8) // 2
        base_y = y + (height - 8) // 2
        draw_cube(canvas, base_x, base_y + 4, 6, scale)
        draw_cube(canvas, base_x + 1, base_y + 2, 6, scale)
        draw_cube(canvas, base_x + 2, base_y, 6, scale)
    else:
        glyph = font(max(8, scale * 4))
        width_px = draw.textlength(label, font=glyph)
        draw.text(
            ((x + width / 2) * scale - width_px / 2, (y + height / 2) * scale - glyph.size / 2),
            label,
            fill=(200, 210, 222, 255),
            font=glyph)


def nine_slice(image, target_w, target_h, inset):
    """Classic 9-slice: corners 1:1, edges stretched in one axis, middle in both."""
    source = image.convert("RGBA")
    width, height = source.size
    out = Image.new("RGBA", (target_w, target_h), (0, 0, 0, 0))

    xs_src = (0, inset, width - inset, width)
    ys_src = (0, inset, height - inset, height)
    xs_dst = (0, inset, target_w - inset, target_w)
    ys_dst = (0, inset, target_h - inset, target_h)

    for row in range(3):
        for col in range(3):
            sx0, sx1 = xs_src[col], xs_src[col + 1]
            sy0, sy1 = ys_src[row], ys_src[row + 1]
            dx0, dx1 = xs_dst[col], xs_dst[col + 1]
            dy0, dy1 = ys_dst[row], ys_dst[row + 1]
            if sx1 <= sx0 or sy1 <= sy0 or dx1 <= dx0 or dy1 <= dy0:
                continue
            out.paste(source.crop((sx0, sy0, sx1, sy1)).resize((dx1 - dx0, dy1 - dy0), Image.NEAREST), (dx0, dy0))
    return out


def draw_rect(canvas, x, y, width, height, colour, scale):
    """Filled rectangle in GUI coordinates."""
    from PIL import ImageDraw as _ImageDraw

    draw = _ImageDraw.Draw(canvas)
    draw.rectangle(
        [x * scale, y * scale, (x + width) * scale - 1, (y + height) * scale - 1],
        fill=colour + (255,))


def render_widget_screen(screen, scale):
    """Screens drawn at runtime: compose the kit the way the Gui does."""
    canvas_w, canvas_h = screen["canvas"]
    canvas = checkerboard(canvas_w * scale, canvas_h * scale)
    kit = Image.open(texture_path(screen["kit"]["texture"])).convert("RGBA")

    panel_x, panel_y, panel_size, panel_inset = screen["kit"]["panel"]
    panel_tile = kit.crop((panel_x, panel_y, panel_x + panel_size, panel_y + panel_size))
    # scale the tile first: the inset must be in the same pixel space as the target
    panel_tile = panel_tile.resize((panel_size * scale, panel_size * scale), Image.NEAREST)

    for x, y, width, height in screen["panels"]:
        panel = Image.new("RGBA", (width * scale, height * scale), (0, 0, 0, 0))
        panel_draw = ImageDraw.Draw(panel)
        top = (54, 59, 72)
        bottom = (38, 42, 52)
        for row in range(height * scale):
            mix = row / max(1, height * scale - 1)
            colour = tuple(round(top[i] + (bottom[i] - top[i]) * mix) for i in range(3))
            panel_draw.line([(0, row), (width * scale, row)], fill=colour + (255,))

        panel.alpha_composite(
            nine_slice(panel_tile, width * scale, height * scale, panel_inset * scale))
        canvas.alpha_composite(panel, (x * scale, y * scale))

        # bright L brackets, 14px arms at inset 4 like the machine panels
        bracket = ImageDraw.Draw(canvas)
        arm = 14 * scale
        thickness = scale
        for cx, cy, dx, dy in (
            (4, 4, 1, 1),
            (width - 5, 4, -1, 1),
            (4, height - 5, 1, -1),
            (width - 5, height - 5, -1, -1),
        ):
            px = (x + cx) * scale
            py = (y + cy) * scale
            bracket.rectangle(
                [min(px, px + dx * arm), py, max(px, px + dx * arm) + thickness - 1, py + thickness - 1],
                fill=(111, 233, 224, 255))
            bracket.rectangle(
                [px, min(py, py + dy * arm), px + thickness - 1, max(py, py + dy * arm) + thickness - 1],
                fill=(111, 233, 224, 255))

    cell_x, cell_y, cell_size, _cell_inset = screen["kit"]["cell"]
    cell = kit.crop((cell_x, cell_y, cell_x + cell_size, cell_y + cell_size))
    cell = cell.resize((cell_size * scale, cell_size * scale), Image.NEAREST)
    for _group, slots in screen["groups"]:
        for slot_x, slot_y in slots:
            canvas.alpha_composite(cell, ((slot_x - 1) * scale, (slot_y - 1) * scale))

    arrow_x, arrow_y, arrow_size, _arrow_inset = screen["kit"]["arrow"]
    arrow = kit.crop((arrow_x, arrow_y, arrow_x + arrow_size, arrow_y + arrow_size))
    arrow = arrow.resize((arrow_size * scale, arrow_size * scale), Image.NEAREST)
    for cx, cy in screen.get("arrows", ()):
        canvas.alpha_composite(arrow, ((cx - arrow_size // 2) * scale, (cy - arrow_size // 2) * scale))

    bar = screen.get("bar")
    if bar:
        bx, by, bw, bh = bar
        draw_rect(canvas, bx, by, bw, bh, (20, 23, 29), scale)
        draw_rect(canvas, bx + 1, by + 1, bw - 2, bh - 2, (46, 51, 64), scale)
        draw_rect(canvas, bx + 2, by + 2, bw - 4, bh - 4, (18, 21, 27), scale)

    info = screen.get("info")
    if info:
        ix, iy, isize = info
        draw_rect(canvas, ix, iy, isize, isize, (20, 23, 29), scale)
        draw_rect(canvas, ix + 1, iy + 1, isize - 2, isize - 2, (38, 42, 52), scale)
        draw_rect(canvas, ix + 6, iy + 4, 2, 2, (111, 233, 224), scale)
        draw_rect(canvas, ix + 6, iy + 7, 2, isize - 10, (111, 233, 224), scale)

    from PIL import ImageDraw as _ImageDraw
    draw = _ImageDraw.Draw(canvas)
    draw.line([(8 * scale, 15 * scale), ((screen["panels"][0][2] - 9) * scale, 15 * scale)],
              fill=(44, 140, 138, 255))
    draw.line([(8 * scale, 16 * scale), ((screen["panels"][0][2] - 9) * scale, 16 * scale)],
              fill=(56, 104, 104, 255))

    for button_x, button_y, button_w, button_h, label in screen.get("buttons", ()):
        draw_button(canvas, button_x, button_y, button_w, button_h, label, scale)

    return canvas


def render_screen(name, screen, scale=4, overlay=True):
    if screen.get("style") == "widget":
        return render_widget_screen(screen, scale)

    path = texture_path(screen["texture"])
    if not os.path.isfile(path):
        raise SystemExit(f"missing texture: {path}")

    source = Image.open(path).convert("RGBA")
    canvas_w, canvas_h = screen["canvas"]
    canvas = checkerboard(canvas_w * scale, canvas_h * scale)
    # Panels are cut from the texture and placed where the GuiContainer draws them.
    for panel_x, panel_y, panel_w, panel_h in screen["panels"]:
        panel = source.crop((panel_x, panel_y, panel_x + panel_w, panel_y + panel_h))
        canvas.alpha_composite(
            panel.resize((panel_w * scale, panel_h * scale), Image.NEAREST),
            (panel_x * scale, panel_y * scale))

    draw = ImageDraw.Draw(canvas)
    label_font = font(max(9, scale * 2))

    for button_x, button_y, button_w, button_h, button_label in screen.get("buttons", ()):
        draw_button(canvas, button_x, button_y, button_w, button_h, button_label, scale)

    if overlay:
        palette = {
            "input": (80, 200, 255),
            "output": (255, 140, 60),
            "upgrade": (180, 120, 255),
            "entity": (120, 230, 120),
            "player": (255, 80, 160),
        }
        abbreviations = {
            "input": "i", "output": "o", "upgrade": "u", "entity": "e", "player": "p",
        }
        for group, slots in screen["groups"]:
            colour = palette.get(group, (255, 255, 0))
            for index, (sx, sy) in enumerate(slots):
                x0, y0, x1, y1 = slot_rect(sx, sy)
                draw.rectangle(
                    [x0 * scale, y0 * scale, (x1 + 1) * scale - 1, (y1 + 1) * scale - 1],
                    outline=colour + (255,),
                    width=max(1, scale // 2),
                )
                if group != "player":
                    text = f"{abbreviations.get(group, group[0])}{index}"
                    width_px = draw.textlength(text, font=label_font)
                    draw.text(
                        ((x0 + SLOT / 2) * scale - width_px / 2,
                         (y0 + SLOT / 2) * scale - label_font.size / 2 - 1),
                        text,
                        fill=(15, 15, 20, 235),
                        font=label_font,
                    )

        for label, tx, ty, width, _key in screen["text"]:
            draw.rectangle(
                [tx * scale, (ty - 1) * scale, (tx + width) * scale, (ty + 9) * scale],
                outline=(0, 255, 200, 160),
                width=1,
            )
            draw.text(
                ((tx + 1) * scale, ty * scale),
                f"{label} line",
                fill=(0, 230, 180, 190),
                font=label_font,
            )

    return canvas


def draw_coordinate_grid(sheet, offset_y, width, height, scale, step):
    """Ruler over the mockup: light lines every `step` GUI pixels, strong ones every 5 steps."""
    draw = ImageDraw.Draw(sheet)
    ruler = font(11)

    for x in range(0, width + 1, step):
        px = x * scale
        strong = x % (step * 5) == 0
        draw.line(
            [(px, offset_y), (px, offset_y + height * scale)],
            fill=(120, 200, 195, 150 if strong else 55),
        )
        if strong:
            draw.text((px + 2, offset_y + 2), str(x), fill=(210, 235, 232, 230), font=ruler)

    for y in range(0, height + 1, step):
        py = offset_y + y * scale
        strong = y % (step * 5) == 0
        draw.line(
            [(0, py), (width * scale, py)],
            fill=(120, 200, 195, 150 if strong else 55),
        )
        if strong:
            draw.text((2, py + 1), str(y), fill=(210, 235, 232, 230), font=ruler)


def describe_screen(name, screen):
    """Human-readable list of everything the screen is made of, with its coordinates."""
    lines = [f"# {name}", ""]

    for index, panel in enumerate(screen.get("panels", [])):
        x, y, w, h = panel
        lines.append(f"панель {index + 1}: x={x} y={y} w={w} h={h}")

    for group, slots in screen.get("groups", []):
        if slots:
            first = slots[0]
            lines.append(f"слоты '{group}': {len(slots)} шт., первый x={first[0]} y={first[1]}")
            if len(slots) > 1:
                lines.append("    все: " + ", ".join(f"({x},{y})" for x, y in slots))

    for entry in screen.get("text", []):
        name_key = entry[0]
        x, y, w = entry[1], entry[2], entry[3]
        lines.append(f"текст '{name_key}': x={x} y={y} ширина={w}")

    for button in screen.get("buttons", []):
        x, y, w, h, label = button[:5]
        lines.append(f"кнопка '{label}': x={x} y={y} w={w} h={h}")

    for x, y in screen.get("arrows", []):
        lines.append(f"стрелка: центр x={x} y={y}")

    bar = screen.get("bar")
    if bar:
        lines.append(f"полоса прогресса: x={bar[0]} y={bar[1]} w={bar[2]} h={bar[3]}")

    info = screen.get("info")
    if info:
        lines.append(f"значок информации: x={info[0]} y={info[1]} размер={info[2]}")

    return "\n".join(lines) + "\n"


def save_sheet(name, screen, scale=4, overlay=True, grid=0):
    canvas = render_screen(name, screen, scale=scale, overlay=overlay)
    width, height = canvas.size
    header = font(16)
    bar = 26
    sheet = checkerboard(width, height + bar, cell=16)
    sheet.alpha_composite(canvas, (0, bar))
    if grid:
        draw_coordinate_grid(sheet, bar, screen["canvas"][0], screen["canvas"][1], scale, grid)
    ImageDraw.Draw(sheet).text(
        (6, 5),
        f"{name}  <-  {screen.get('texture', 'drawn from the kit')}  ({scale}x)",
        fill=(230, 230, 235, 255),
        font=header,
    )
    os.makedirs(OUT_DIR, exist_ok=True)
    suffix = "" if overlay else "_clean"
    if grid:
        suffix += "_grid"
    out = os.path.join(OUT_DIR, f"gui_{name}_{scale}x{suffix}.png")
    sheet.save(out)
    return out


def wrap_label(text, max_width, draw, label_font, max_lines=2):
    """Split a long texture name at underscores so it fits a sheet cell."""
    words = text.replace(".png", "").split("_")
    lines = []
    current = ""
    for word in words:
        candidate = f"{current}_{word}" if current else word
        if draw.textlength(candidate, font=label_font) <= max_width or not current:
            current = candidate
        else:
            lines.append(current)
            current = word
    if current:
        lines.append(current)
    if len(lines) > max_lines:
        lines = lines[:max_lines]
        lines[-1] += "..."
    return lines


def build_texture_sheet(scale=8, columns=8):
    """Contact sheet of every 16x16 block/item texture."""
    entries = []
    for group in ("blocks", "items"):
        directory = os.path.join(TEX, group)
        if not os.path.isdir(directory):
            continue
        for file_name in sorted(os.listdir(directory)):
            if file_name.lower().endswith(".png"):
                entries.append((f"{group}/{file_name}", os.path.join(directory, file_name)))

    cell = 16 * scale
    pad = 6
    cell_w = cell + pad * 2
    cell_h = cell + 30 + pad
    rows = (len(entries) + columns - 1) // columns

    sheet = checkerboard(columns * cell_w, rows * cell_h + 24, cell=12)
    draw = ImageDraw.Draw(sheet)
    draw.text((6, 5), f"Berlit textures, {scale}x", fill=(235, 235, 240, 255), font=font(14))

    small = font(11)
    for index, (name, path) in enumerate(entries):
        row, col = divmod(index, columns)
        x = col * cell_w + pad
        y = row * cell_h + 24 + pad
        image = Image.open(path).convert("RGBA")
        if image.size != (16, 16):
            image = image.resize((16, 16), Image.NEAREST)
        sheet.alpha_composite(image.resize((cell, cell), Image.NEAREST), (x, y))
        for line_index, line in enumerate(wrap_label(name, cell_w - 8, draw, small)):
            draw.text(
                (x, y + cell + 2 + line_index * 12),
                line,
                fill=(200, 200, 210, 255),
                font=small,
            )

    os.makedirs(OUT_DIR, exist_ok=True)
    out = os.path.join(OUT_DIR, f"textures_{scale}x.png")
    sheet.save(out)
    return out


# --------------------------------------------------------------------------
# Lint
# --------------------------------------------------------------------------

class Report:

    def __init__(self):
        self.errors = []
        self.warnings = []

    def error(self, where, message):
        self.errors.append(f"ERROR  {where}: {message}")

    def warn(self, where, message):
        self.warnings.append(f"WARN   {where}: {message}")

    def print(self):
        for line in self.errors + self.warnings:
            print(line)
        print()
        print(f"{len(self.errors)} error(s), {len(self.warnings)} warning(s)")
        return 1 if self.errors else 0


def panel_background(image, slots, panels):
    """Most common colour of the panel *outside* the slots - the screen background.

    Slot fill can cover more pixels than the background, so sampling the whole
    panel would report the slot colour as the background.
    """
    slot_pixels = set()
    for sx, sy in slots:
        for y in range(sy - 1, sy + SLOT - 1):
            for x in range(sx - 1, sx + SLOT - 1):
                slot_pixels.add((x, y))

    counter = Counter()
    for panel_x, panel_y, panel_w, panel_h in panels:
        for y in range(panel_y, panel_y + panel_h):
            for x in range(panel_x, panel_x + panel_w):
                if (x, y) not in slot_pixels:
                    counter[image.getpixel((x, y))] += 1
    return counter.most_common(1)[0][0] if counter else (0, 0, 0, 0)


def modal_colour(image):
    """(colour, share) of the dominant colour, or (None, 0.0) if it is noise."""
    colours = image.getcolors(maxcolors=1 << 12)
    if not colours:
        return None, 0.0
    colours.sort(reverse=True)
    count, colour = colours[0]
    return colour, count / (image.size[0] * image.size[1])


def check_text_lines(where, screen, occupied, report, langs):
    for label, tx, ty, width, key in screen["text"]:
        band = (tx, ty, tx + width, ty + 8)
        for tag, x0, y0, x1, y1 in occupied:
            if band[0] <= x1 and x0 <= band[2] and band[1] <= y1 and y0 <= band[3]:
                report.error(where, f"text line '{label}' overlaps slot {tag}")

        # key is None for lines the Gui builds without a lang string (e.g. "42%").
        if key is None:
            candidates = [(label, TEXT_SAMPLES.get(label, ""))]
        else:
            candidates = []
            for lang_name, values in langs.items():
                template = values.get(key)
                if template is None:
                    continue
                sample = TEXT_SAMPLES.get(key, "")
                try:
                    candidates.append((lang_name, template % sample if "%" in template else template))
                except (TypeError, ValueError):
                    candidates.append((lang_name, template))

        for source, rendered in candidates:
            measured = mc_text_width(rendered)
            if measured > width:
                report.warn(
                    where,
                    f"{source} '{key or label}' is ~{measured}px wide, text line '{label}' has {width}px: "
                    f"\"{rendered}\"",
                )




def check_screen(name, screen, report, langs):
    where = name
    if screen.get("style") == "widget":
        occupied = []
        for group, slots in screen["groups"]:
            for index, (sx, sy) in enumerate(slots):
                x0, y0, x1, y1 = slot_rect(sx, sy)
                occupied.append((f"{group}{index} @({sx},{sy})", x0, y0, x1, y1))
                for button in screen.get("buttons", ()):
                    bx, by, bw, bh = button[0], button[1], button[2], button[3]
                    if x0 <= bx + bw and bx <= x1 and y0 <= by + bh and by <= y1:
                        report.error(where, f"button {button[4]} at ({bx},{by}) overlaps {group}{index}")
        check_text_lines(where, screen, occupied, report, langs)
        return
    path = texture_path(screen["texture"])
    if not os.path.isfile(path):
        report.error(where, f"texture missing: textures/{screen['texture']}")
        return

    image = Image.open(path)
    canvas_w, canvas_h = screen["canvas"]
    if image.size[0] < canvas_w or image.size[1] < canvas_h:
        report.error(where, f"texture is {image.size[0]}x{image.size[1]}, the screen needs {canvas_w}x{canvas_h}")
        return
    if image.mode != "RGBA":
        report.error(where, f"mode is {image.mode}; GUI textures need RGBA so the panel can be drawn over the world")
    rgba = image.convert("RGBA")

    # A GUI texture is sampled with GL_NEAREST; odd sizes are legal but 256x256 is the
    # vanilla convention and keeps the whole image power-of-two on old GPUs.

    for value in rgba.size:
        if value & (value - 1):
            report.warn(where, f"size {value} is not a power of two")

    for panel_x, panel_y, panel_w, panel_h in screen["panels"]:
        region = rgba.crop((panel_x, panel_y, panel_x + panel_w, panel_y + panel_h))
        holes = sum(region.getchannel("A").histogram()[:255])
        if holes:
            report.error(
                where,
                f"{holes} transparent pixel(s) in the panel at ({panel_x},{panel_y}) "
                    + f"{panel_w}x{panel_h}")

    all_slots = [slot for _group, group in screen["groups"] for slot in group]
    background = panel_background(rgba, all_slots, screen["panels"])
    occupied = []
    for group, slots in screen["groups"]:
        for index, (sx, sy) in enumerate(slots):
            tag = f"{group}{index} @({sx},{sy})"
            x0, y0, x1, y1 = slot_rect(sx, sy)
            if x0 < 0 or y0 < 0 or x1 >= canvas_w or y1 >= canvas_h:
                report.error(where, f"slot {tag} falls outside the screen")
                continue
            modal, share = modal_colour(rgba.crop(slot_inner(sx, sy)))

            # Compare the whole 18x18 slot area with the tile the generator pastes.
            # Any mismatch means the texture and the Container coordinates disagree.
            expected = SLOT_TILE
            region = rgba.crop((x0, y0, x0 + SLOT, y0 + SLOT))
            mismatched = sum(
                1
                for y in range(SLOT)
                for x in range(SLOT)
                if region.getpixel((x, y)) != expected.getpixel((x, y))
            )
            if mismatched:
                worst = 100 * mismatched / (SLOT * SLOT)
                level = report.error if worst > 10 else report.warn
                level(
                    where,
                    f"slot {tag} does not match the theme tile ({worst:.0f}% of pixels differ); "
                    "the texture was not regenerated after the slot layout changed",
                )
            elif modal is not None and modal[:3] == background[:3]:
                report.error(where, f"slot {tag} has no slot background drawn at this position")
            occupied.append((tag, x0, y0, x1, y1))

    for i, (tag_a, ax0, ay0, ax1, ay1) in enumerate(occupied):
        for tag_b, bx0, by0, bx1, by1 in occupied[i + 1:]:
            if ax0 <= bx1 and bx0 <= ax1 and ay0 <= by1 and by0 <= ay1:
                report.error(where, f"slots overlap: {tag_a} and {tag_b}")

    check_text_lines(where, screen, occupied, report, langs)


def check_lang(report):
    en = read_lang("en_US.lang")
    ru = read_lang("ru_RU.lang")
    if not en or not ru:
        report.error("lang", "en_US.lang / ru_RU.lang is missing")
        return

    for key in sorted(set(en) - set(ru)):
        report.warn("ru_RU.lang", f"missing translation for '{key}' (present in en_US)")
    for key in sorted(set(ru) - set(en)):
        report.warn("en_US.lang", f"missing translation for '{key}' (present in ru_RU)")


TRANSLATE_CALL = re.compile(r"translateTo(?:Local|LocalFormatted)\(\s*\"([^\"]+)\"(\s*\+)?")
TITLE_KEY = re.compile(r"getTitleKey\(\)\s*\{[^{}]*?return\s+\"([^\"]+)\"", re.S)
SUFFIX_KEY = re.compile(
    r"\"((?:tile|item|container|nei|berlit|itemGroup)\.[A-Za-z0-9_.]+\.(?:name|tooltip|empty|unknown|drops))\""
)


def java_lang_keys(sources):
    """Lang keys the code really asks for: translateToLocal() arguments and title keys.

    Registry names such as "berlit.machine_macerator" look like keys but are not,
    so they must not be collected.
    """
    keys = {}
    for text in sources.values():
        for match in TRANSLATE_CALL.finditer(text):
            # "berlit.gui.mode." + mode -> a prefix, not a key
            keys[match.group(1)] = bool(match.group(2))
        for match in TITLE_KEY.finditer(text):
            keys.setdefault(match.group(1), False)
        for match in SUFFIX_KEY.finditer(text):
            keys.setdefault(match.group(1), False)
    return keys


def check_java_references(report, langs):
    sources = read_java_sources()
    texture_pattern = re.compile(r"berlit:textures/([A-Za-z0-9_/]+\.png)")

    for source_name, text in sorted(sources.items()):
        for relative in sorted(set(texture_pattern.findall(text))):
            if not os.path.isfile(texture_path(relative)):
                report.error(source_name, f"references a missing texture: textures/{relative}")

    for key, is_prefix in sorted(java_lang_keys(sources).items()):
        if key in VANILLA_KEYS:
            continue
        for lang_name, values in langs.items():
            if is_prefix:
                if not any(existing.startswith(key) for existing in values):
                    report.error(f"lang/{lang_name}", f"code builds keys matching '{key}*', none exist")
            elif key not in values:
                report.error(f"lang/{lang_name}", f"missing key '{key}' used by the code")


def check_uv_scaling(report):
    """A screen whose texture is larger than 256 must set textureWidth/Height on its Gui.

    drawTexturedModalRect divides UVs by 256, so a 256x512 or 512x512 GUI texture gets
    tiled instead of sampled once unless the Gui uses BerlitGui.drawPanel with the real size.
    """
    sources = read_java_sources()
    for name, screen in SCREENS.items():
        canvas_w, canvas_h = screen["canvas"]
        if canvas_w <= 256 and canvas_h <= 256:
            continue
        declared = any(
            f"textureWidth = {canvas_w}" in text and f"textureHeight = {canvas_h}" in text
            for text in sources.values())
        if not declared:
            report.error(
                name,
                f"canvas is {canvas_w}x{canvas_h}; set textureWidth = {canvas_w} and "
                    + f"textureHeight = {canvas_h} in the Gui, otherwise the panel tiles")


def check_nei(report):
    """NEI recipe pages: 176x166 slate backgrounds with a slot cell under every item."""
    for name, page in NEI_PAGES.items():
        where = f"nei:{name}"
        path = texture_path(page["texture"])
        if not os.path.isfile(path):
            report.error(where, f"texture missing: textures/{page['texture']}")
            continue

        with Image.open(path) as image:
            if image.mode != "RGBA":
                report.error(where, f"mode is {image.mode}; NEI page textures need RGBA")
            rgba = image.convert("RGBA")

        page_w, page_h = page["page"]
        page_x, page_y = page.get("origin", (5, 11))
        if rgba.size[0] < page_x + page_w or rgba.size[1] < page_y + page_h:
            report.error(
                where,
                f"texture is {rgba.size[0]}x{rgba.size[1]}, page at {page_x},{page_y} needs {page_w}x{page_h}")
            continue

        holes = sum(
            rgba.crop((page_x, page_y, page_x + page_w, page_y + page_h))
                .getchannel("A")
                .histogram()[:255])
        if holes:
            report.error(where, f"{holes} transparent pixel(s) inside the {page_w}x{page_h} page")

        items = []
        for item_x, item_y in page["slots"]:
            if item_x < 0 or item_y < 0 or item_x + 16 > page_w or item_y + 16 > page_h:
                report.error(where, f"item at ({item_x},{item_y}) falls outside the page")
                continue
            items.append((item_x, item_y))

        # Every stack needs a cell under it: the page is regenerated from the same
        # coordinates, so a mismatch means the texture is stale.
        for item_x, item_y in items:
            cell = rgba.crop(
                (page_x + item_x, page_y + item_y, page_x + item_x + 16, page_y + item_y + 16))
            if len(cell.getcolors(2) or []) != 1:
                report.error(
                    where,
                    f"no slot cell at ({item_x},{item_y}); run python tools/gen_nei_pages.py")

        for index, (ax, ay) in enumerate(items):
            for bx, by in items[index + 1:]:
                if ax < bx + 18 and bx < ax + 18 and ay < by + 18 and by < ay + 18:
                    report.error(where, f"items at ({ax},{ay}) and ({bx},{by}) overlap")

        for text_x, text_y, text_w in page["text"]:
            for item_x, item_y in items:
                if text_x <= item_x + 16 and item_x <= text_x + text_w and text_y <= item_y + 16 \
                        and item_y <= text_y + 8:
                    report.error(where, f"text at ({text_x},{text_y}) overlaps the item at ({item_x},{item_y})")

def load_exceptions():
    """Globs of textures that deliberately break the block/item rules (tools/lint_exceptions.json)."""
    path = os.path.join(os.path.dirname(os.path.abspath(__file__)), "lint_exceptions.json")
    if not os.path.isfile(path):
        return {"hd": {}, "opaque_items": {}}
    with open(path, encoding="utf-8") as handle:
        data = json.load(handle)
    return {key: {k: v for k, v in data.get(key, {}).items() if not k.startswith("_")} for key in ("hd", "opaque_items")}


def excused(relative, patterns):
    return any(fnmatch.fnmatch(relative, pattern) for pattern in patterns)


def check_item_textures(report):
    exceptions = load_exceptions()
    for group in ("blocks", "items"):
        directory = os.path.join(TEX, group)
        if not os.path.isdir(directory):
            continue
        for file_name in sorted(os.listdir(directory)):
            if not file_name.lower().endswith(".png"):
                continue
            relative = f"{group}/{file_name}"
            with Image.open(os.path.join(directory, file_name)) as image:
                if image.size != (16, 16) and not excused(relative, exceptions["hd"]):
                    report.warn(relative, f"size is {image.size[0]}x{image.size[1]}, block/item textures are 16x16")
                rgba = image.convert("RGBA")
                low, high = rgba.getchannel("A").getextrema()
                if high == 0:
                    report.error(relative, "texture is fully transparent (invisible in game)")
                if group == "items" and low == 255 and not excused(relative, exceptions["opaque_items"]):
                    report.warn(relative, "item texture is fully opaque; items normally have a transparent background")


# Screen name -> the Java class that owns its geometry. Anything listed here must agree with
# gui_layout.py, otherwise the mockup and the game drift apart and nobody notices until in-game.
LAYOUT_CLASSES = {
    "matter_generator": "MatterLayout.java",
    "avaritia_assembler": "AssemblerLayout.java",
    "neutron_compressor": "CompressorLayout.java",
    "molecular_transformer": "MachineLayout.java",
    "spawner": "MachineLayout.java",
    "spawner_t2": "MachineLayout.java",
}

JAVA_GUI_DIR = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "src", "main", "java",
                            "ru", "beggar", "berlit", "gui")
JAVA_AE2_DIR = os.path.join(
    os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
    "src",
    "main",
    "java",
    "ru",
    "beggar",
    "berlit",
    "ae2")
JAVA_MACHINE_DIR = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "src", "main", "java",
                                "ru", "beggar", "berlit", "machine")


def read_java_constants(file_name):
    """Literals from a *Layout class: name -> int."""
    for directory in (JAVA_GUI_DIR, JAVA_MACHINE_DIR, JAVA_AE2_DIR):
        path = os.path.join(directory, file_name)
        if os.path.isfile(path):
            break
    else:
        return {}

    constants = {}
    pattern = re.compile(r"public static final int ([A-Z_]+) = (-?\d+);")
    with open(path, encoding="utf-8") as handle:
        for line in handle:
            match = pattern.search(line)
            if match:
                constants[match.group(1)] = int(match.group(2))
    return constants


def check_layout_java(report):
    """Compare the preview geometry with the Java constants it is supposed to mirror."""
    for name, java_file in LAYOUT_CLASSES.items():
        screen = SCREENS.get(name)
        if screen is None:
            continue
        constants = read_java_constants(java_file)
        if not constants:
            report.warn(name, f"{java_file} not found or has no int constants to compare")
            continue

        canvas_w, canvas_h = screen["canvas"]
        panels = screen["panels"]
        player = next((slots for group, slots in screen.get("groups", []) if group == "player"), [])

        def expect(key, preview_value):
            if key not in constants:
                report.warn(name, f"{java_file}: {key} is missing")
                return
            if constants[key] != preview_value:
                report.error(
                    name,
                    f"{java_file}: {key}={constants[key]} but the preview uses {preview_value}")

        expect("WIDTH", canvas_w)

        if len(panels) > 1:
            expect("INVENTORY_TOP", panels[1][1])
            if "INVENTORY_X" in constants:
                expect("INVENTORY_X", panels[1][0])
            if "INVENTORY_WIDTH" in constants:
                expect("INVENTORY_WIDTH", panels[1][2])

        if player:
            if "PLAYER_X" in constants:
                expect("PLAYER_X", player[0][0])
            if "PLAYER_TOP" in constants:
                expect("PLAYER_TOP", player[0][1])
            if "HOTBAR_Y" in constants and len(player) >= 36:
                expect("HOTBAR_Y", player[27][1])


def cmd_check(args):
    report = Report()
    langs = {"en_US.lang": read_lang("en_US.lang"), "ru_RU.lang": read_lang("ru_RU.lang")}
    for name, screen in SCREENS.items():
        check_screen(name, screen, report, langs)
    check_lang(report)
    check_java_references(report, langs)
    check_item_textures(report)
    check_nei(report)
    check_uv_scaling(report)
    check_layout_java(report)
    return report.print()


def cmd_sheet(args):
    print("written", build_texture_sheet(scale=args.scale))
    for name, screen in SCREENS.items():
        print("written", save_sheet(name, screen, scale=args.gui_scale))
    return 0



def cmd_screens(args):
    """Every screen of the mod with the data needed to pick one for --grid."""
    print(f"{len(SCREENS)} screens (use: preview.py gui --grid <name>)")
    print()
    print(f"{'name':28s} {'style':9s} {'canvas':11s} slots")
    print("-" * 62)
    for name in sorted(SCREENS):
        screen = SCREENS[name]
        width, height = screen["canvas"]
        slots = sum(len(slots) for _group, slots in screen.get("groups", []))
        style = screen.get("style", "?")
        print(f"{name:28s} {style:9s} {width}x{height:<7} {slots}")
    return 0


def cmd_gui(args):
    names = args.names or list(SCREENS)
    for name in names:
        if name not in SCREENS:
            known = "\n  ".join(sorted(SCREENS))
            raise SystemExit(f"unknown screen '{name}'. Known screens:\n  {known}\n"
                             "Pick one and run: preview.py gui --grid <name>")
        print(
            "written",
            save_sheet(name, SCREENS[name], scale=args.gui_scale, overlay=not args.clean, grid=args.grid_step if args.grid else 0))
        if args.grid:
            table = describe_screen(name, SCREENS[name])
            print()
            print(table)
            # the console mangles Cyrillic on Windows, so the table also lands next to the image
            target = os.path.join(OUT_DIR, f"gui_{name}_{args.gui_scale}x_grid.txt")
            with open(target, "w", encoding="utf-8", newline="\n") as handle:
                handle.write(table)
            print("written", target)
    return 0


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    sub = parser.add_subparsers(dest="command", required=True)

    sheet = sub.add_parser("sheet", help="build contact sheets into docs/preview/")
    sheet.add_argument("--scale", type=int, default=8, help="block/item zoom (default 8)")
    sheet.add_argument("--gui-scale", type=int, default=4, help="GUI zoom (default 4)")
    sheet.set_defaults(func=cmd_sheet)

    gui = sub.add_parser("gui", help="render GUI mockups with the slot overlay")
    gui.add_argument("names", nargs="*", help="screen names (default: all)")
    gui.add_argument("--gui-scale", type=int, default=4)
    gui.add_argument("--clean", action="store_true", help="no slot overlay, just the artwork")
    gui.add_argument("--grid", action="store_true", help="coordinate ruler plus an element table")
    gui.add_argument("--grid-step", type=int, default=10, help="ruler step in GUI pixels (default 10)")
    gui.set_defaults(func=cmd_gui)

    screens = sub.add_parser("screens", aliases=["list", "ls"], help="list every screen name")
    screens.set_defaults(func=cmd_screens)

    check = sub.add_parser("check", help="lint textures, slots and translations")
    check.set_defaults(func=cmd_check)

    args = parser.parse_args(argv)
    return args.func(args)


if __name__ == "__main__":
    sys.exit(main())
