"""Renders tools/palette.json as a swatch sheet for reference."""

import json
import os

from PIL import Image, ImageDraw, ImageFont

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
PALETTE = os.path.join(ROOT, "tools", "palette.json")
OUT = os.path.join(ROOT, "docs", "preview", "palette.png")


def font(size):
    try:
        return ImageFont.load_default(size=size)
    except TypeError:
        return ImageFont.load_default()


def rgb(value):
    value = value.lstrip("#")
    return tuple(int(value[i:i + 2], 16) for i in (0, 2, 4))


def main():
    with open(PALETTE, encoding="utf-8") as handle:
        palette = json.load(handle)

    groups = [(name, values) for name, values in palette.items() if isinstance(values, dict)]
    swatch_w = 150
    swatch_h = 54
    columns = 4
    rows = sum((len(values) + columns - 1) // columns for _name, values in groups)
    height = 30 + rows * (swatch_h + 26) + len(groups) * 24

    canvas = Image.new("RGB", (columns * swatch_w + 20, height), (24, 26, 30))
    draw = ImageDraw.Draw(canvas)
    title = font(16)
    small = font(11)
    draw.text((10, 8), "Berlit UI palette - tools/palette.json", fill=(235, 235, 240), font=title)

    y = 34
    for name, values in groups:
        draw.text((10, y), name.upper(), fill=(120, 200, 195), font=small)
        y += 20
        for index, (key, colour) in enumerate(values.items()):
            row, col = divmod(index, columns)
            x = 10 + col * swatch_w
            sy = y + row * (swatch_h + 26)
            draw.rectangle([x, sy, x + swatch_w - 10, sy + swatch_h - 14], fill=rgb(colour))
            draw.text((x + 2, sy + swatch_h - 12), f"{key} {colour}", fill=(200, 205, 215), font=small)
        rows_used = (len(values) + columns - 1) // columns
        y += rows_used * (swatch_h + 26)

    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    canvas.save(OUT)
    print("written", OUT, canvas.size)


if __name__ == "__main__":
    main()
