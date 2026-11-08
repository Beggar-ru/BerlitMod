"""Reports the macerator textures as colour zones plus the most frequent exact colours."""

from __future__ import annotations

import collections
import os

from PIL import Image, ImageDraw, ImageFont

ROOT = r"C:\main_mod"
BLOCKS = os.path.join(ROOT, "src", "main", "resources", "assets", "berlit", "textures", "blocks")
OUT_MD = os.path.join(ROOT, "docs", "MACERATOR_COLORS.md")
OUT_PNG = os.path.join(ROOT, "docs", "preview", "macerator_colors.png")

FACES = [
    ("machine_macerator_side", "Обычный · бок (лицевая)"),
    ("machine_macerator_top", "Обычный · верх (ротор)"),
    ("machine_macerator_bottom", "Обычный · низ"),
    ("digital_macerator_side", "Цифровой · бок (лицевая)"),
    ("digital_macerator_top", "Цифровой · верх (ротор)"),
    ("digital_macerator_bottom", "Цифровой · низ"),
]

ZONES = [
    ("тень, кант", 0.00, 0.30),
    ("тёмные панели", 0.30, 0.55),
    ("корпус", 0.55, 0.80),
    ("светлые детали", 0.80, 1.01),
]

RAMPS = [
    (
        "Обычный дробитель",
        [(0, (20, 23, 29), "тень, кант"), (140, (74, 84, 104), "корпус"), (255, (198, 208, 222), "заклёпки")],
    ),
    (
        "Цифровой дробитель",
        [
            (0, (150, 246, 238), "свечение панелей"),
            (70, (44, 140, 138), "бирюза механизма"),
            (150, (44, 52, 66), "корпус"),
            (255, (30, 34, 44), "тень, кант"),
        ],
    ),
]


def hex_of(pixel):
    return "#%02X%02X%02X" % tuple(int(round(c)) for c in pixel[:3])


def luminance(pixel):
    return (pixel[0] * 0.299 + pixel[1] * 0.587 + pixel[2] * 0.114) / 255.0


def analyse(path):
    image = Image.open(path).convert("RGBA")
    pixels = image.load()

    zones = collections.defaultdict(list)
    exact = collections.Counter()
    exact_points = collections.defaultdict(list)

    for y in range(16):
        for x in range(16):
            pixel = pixels[x, y]
            if pixel[3] == 0:
                continue
            level = luminance(pixel)
            for name, low, high in ZONES:
                if low <= level < high:
                    zones[name].append((x, y, pixel))
                    break
            colour = hex_of(pixel)
            exact[colour] += 1
            exact_points[colour].append((x, y))

    zone_rows = []
    for name, _low, _high in ZONES:
        points = zones.get(name, [])
        if not points:
            continue
        colour = tuple(sum(p[2][c] for p in points) / len(points) for c in range(3))
        xs = [p[0] for p in points]
        ys = [p[1] for p in points]
        zone_rows.append(
            {
                "name": name,
                "hex": hex_of(colour),
                "count": len(points),
                "share": 100.0 * len(points) / 256.0,
                "box": (min(xs), min(ys), max(xs), max(ys)),
            }
        )

    exact_rows = []
    for colour, count in exact.most_common(5):
        points = exact_points[colour]
        xs = [p[0] for p in points]
        ys = [p[1] for p in points]
        exact_rows.append(
            {
                "hex": colour,
                "count": count,
                "share": 100.0 * count / 256.0,
                "box": (min(xs), min(ys), max(xs), max(ys)),
            }
        )

    return image, zone_rows, exact_rows


def strip(images):
    scale = 10
    cell = 16 * scale
    pad = 14
    sheet = Image.new("RGBA", (len(images) * (cell + pad) + pad, cell + 34), (24, 26, 30, 255))
    draw = ImageDraw.Draw(sheet)
    try:
        font = ImageFont.load_default(size=13)
    except TypeError:
        font = ImageFont.load_default()
    for index, (name, image, _zones, _exact) in enumerate(images):
        x = pad + index * (cell + pad)
        draw.text((x, 6), str(index + 1), fill=(235, 235, 240), font=font)
        sheet.alpha_composite(image.resize((cell, cell), Image.NEAREST), (x, 24))
    sheet.save(OUT_PNG)


def main():
    lines = [
        "# Цвета и позиции текстур дробителей",
        "",
        "Снято с готовых PNG. Координаты — в системе 16×16, (0, 0) — левый верхний угол грани.",
        "Зоны сгруппированы по яркости пикселя: внутри зоны цвет усреднён, поэтому он и есть «цвет детали».",
        "",
        "## Рампы перекраски",
        "",
        "| Машина | Порог яркости | Цвет | Деталь |",
        "|---|---|---|---|",
    ]
    for machine, stops in RAMPS:
        for level, colour, role in stops:
            lines.append(f"| {machine} | {level} | `{"#%02X%02X%02X" % colour}` | {role} |")
    lines.append("")

    images = []
    for name, title in FACES:
        image, zones, exact = analyse(os.path.join(BLOCKS, name + ".png"))
        images.append((name, image, zones, exact))

        lines.append(f"## {title}")
        lines.append("")
        lines.append(f"`{name}.png`")
        lines.append("")
        lines.append("**Зоны**")
        lines.append("")
        lines.append("| Зона | Цвет | Доля | Границы (x1,y1)–(x2,y2) |")
        lines.append("|---|---|---|---|")
        for row in zones:
            x1, y1, x2, y2 = row["box"]
            lines.append(f"| {row['name']} | `{row['hex']}` | {row['share']:.1f}% | ({x1},{y1})–({x2},{y2}) |")
        lines.append("")
        lines.append("**Самые частые точные цвета**")
        lines.append("")
        lines.append("| Цвет | Доля | Пикселей | Границы (x1,y1)–(x2,y2) |")
        lines.append("|---|---|---|---|")
        for row in exact:
            x1, y1, x2, y2 = row["box"]
            lines.append(f"| `{row['hex']}` | {row['share']:.1f}% | {row['count']} | ({x1},{y1})–({x2},{y2}) |")
        lines.append("")

    strip(images)

    with open(OUT_MD, "w", encoding="utf-8", newline="\n") as handle:
        handle.write("\n".join(lines) + "\n")

    print("written", OUT_MD)
    print("written", OUT_PNG)


if __name__ == "__main__":
    main()
