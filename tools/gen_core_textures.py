"""Generates the 32x32 spawner core item textures.

The shape follows Draconic Evolution's core silhouette (an eight-pointed metal
star around a ring), redrawn at 32x32 with the Berlit palette:

* tier I  (nuclear)         - dark steel with radioactive green;
* tier II (technological)   - dark steel with the Berlit GUI turquoise
                              (#2C8C8A / #6FE9E0);
* tier III (holy)           - ivory, pale gold and warm highlights.

Run from the project root: python tools/gen_core_textures.py
"""

import math
import os

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "src", "main", "resources", "assets", "berlit", "textures", "items")

SIZE = 32
CENTER = 16.0


def _star_points(inner_radius=14.0):
    """Vertices of an eight-pointed star: four long cardinal spikes and four
    diagonal arms reaching the corners of the canvas."""
    points = []
    for k in range(8):
        angle = math.radians(k * 45.0)
        radius = 17.5 if k % 2 == 0 else 24.5
        points.append((CENTER + radius * math.cos(angle), CENTER + radius * math.sin(angle)))
        angle = math.radians(k * 45.0 + 22.5)
        points.append((CENTER + inner_radius * math.cos(angle), CENTER + inner_radius * math.sin(angle)))
    return points


def _polygon_mask(points):
    """Point-in-polygon mask sampled at pixel centres, so the result is exactly
    symmetric on a 32x32 grid."""
    mask = [[False] * SIZE for _ in range(SIZE)]
    count = len(points)
    for y in range(SIZE):
        py = y + 0.5
        for x in range(SIZE):
            px = x + 0.5
            inside = False
            for i in range(count):
                x1, y1 = points[i]
                x2, y2 = points[(i + 1) % count]
                if (y1 > py) != (y2 > py):
                    cross_x = (x2 - x1) * (py - y1) / (y2 - y1) + x1
                    if px < cross_x:
                        inside = not inside
            mask[y][x] = inside
    return mask


def _erode(mask):
    """One-pixel erosion used to paint the dark outline around the silhouette."""
    result = [[False] * SIZE for _ in range(SIZE)]
    for y in range(SIZE):
        for x in range(SIZE):
            if not mask[y][x]:
                continue
            keep = True
            for dy, dx in ((-1, 0), (1, 0), (0, -1), (0, 1)):
                ny, nx = y + dy, x + dx
                if ny < 0 or ny >= SIZE or nx < 0 or nx >= SIZE or not mask[ny][nx]:
                    keep = False
                    break
            result[y][x] = keep
    return result


def _build(palette):
    mask = _polygon_mask(_star_points())
    inner = _erode(mask)
    image = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    pixels = image.load()

    for y in range(SIZE):
        for x in range(SIZE):
            if not mask[y][x]:
                continue

            u = x + 0.5 - CENTER
            v = y + 0.5 - CENTER
            radius = math.hypot(u, v)
            light = u + v  # negative means the upper-left side
            ax, ay = abs(u), abs(v)

            if not inner[y][x]:
                pixels[x, y] = palette["outline"]
                continue

            # --- metal star -------------------------------------------------
            shade = 3 if light < -11 else 2 if light < 1 else 1 if light < 11 else 0
            color = palette["metal"][shade]

            # Rim light on the top/left-facing edges keeps the points readable.
            top_edge = (y > 0 and not mask[y - 1][x]) or (x > 0 and not mask[y][x - 1])
            bottom_edge = (y < SIZE - 1 and not mask[y + 1][x]) or (x < SIZE - 1 and not mask[y][x + 1])
            if top_edge:
                color = palette["metal_hl"]
            elif bottom_edge:
                color = palette["metal_dk"]

            # --- recessed frame ring ---------------------------------------
            if radius >= 9.8:
                if radius < 12.0:
                    color = palette["frame"]
                    if light < -8:
                        color = palette["frame_hl"]
                    if radius < 10.6:
                        color = palette["frame_dk"]

            # --- dark eye ---------------------------------------------------
            if radius < 3.2:
                color = palette["hole"]
                if light < -1.2 and radius > 0.9:
                    color = palette["hole_hl"]
                if radius < 0.8:
                    color = palette["ember"]
            elif radius < 5.2:
                color = palette["rim"]
                if light < -2:
                    color = palette["rim_hl"]
            elif radius < 9.8:
                # --- energy ring --------------------------------------------
                color = palette["ring"][1]
                if light < -5:
                    color = palette["ring"][2]
                if light > 9:
                    color = palette["ring"][0]
                if radius < 6.0:
                    color = palette["ring_hl"] if light < 1.5 else palette["ring_dk"]
                if radius >= 9.0 and light > 0:
                    color = palette["ring_dk"]

            # --- glowing tips ----------------------------------------------
            if (ax < 1.8 and ay > 14.4) or (ay < 1.8 and ax > 14.4) or radius > 21.8:
                color = palette["tip"]
            if (ax < 1.0 and ay > 15.2) or (ay < 1.0 and ax > 15.2):
                color = palette["tip_hl"]

            pixels[x, y] = color

    return image


PALETTES = {
    "spawner_core_t1": {
        "outline": (5, 9, 6, 255),
        "metal": [
            (26, 37, 28, 255),
            (36, 50, 38, 255),
            (48, 66, 50, 255),
            (64, 86, 66, 255),
        ],
        "metal_hl": (84, 110, 86, 255),
        "metal_dk": (16, 23, 17, 255),
        "frame": (15, 22, 16, 255),
        "frame_hl": (45, 64, 46, 255),
        "frame_dk": (9, 14, 10, 255),
        "ring": [(36, 98, 42, 255), (58, 148, 64, 255), (86, 198, 92, 255)],
        "ring_hl": (170, 255, 158, 255),
        "ring_dk": (24, 64, 28, 255),
        "hole": (9, 17, 10, 255),
        "hole_hl": (36, 62, 38, 255),
        "ember": (200, 255, 185, 255),
        "rim": (12, 24, 14, 255),
        "rim_hl": (70, 116, 72, 255),
        "tip": (115, 230, 120, 255),
        "tip_hl": (210, 255, 200, 255),
    },
    "spawner_core_t2": {
        "outline": (5, 7, 10, 255),
        "metal": [
            (28, 36, 47, 255),
            (38, 48, 62, 255),
            (51, 65, 84, 255),
            (68, 86, 110, 255),
        ],
        "metal_hl": (96, 117, 142, 255),
        "metal_dk": (17, 21, 29, 255),
        "frame": (12, 17, 22, 255),
        "frame_hl": (50, 66, 82, 255),
        "frame_dk": (8, 11, 15, 255),
        # Berlit GUI turquoise: frame #2C8C8A, light #6FE9E0.
        "ring": [(28, 92, 90, 255), (44, 140, 138, 255), (111, 233, 224, 255)],
        "ring_hl": (185, 255, 249, 255),
        "ring_dk": (24, 66, 66, 255),
        "hole": (8, 12, 16, 255),
        "hole_hl": (34, 58, 61, 255),
        "ember": (196, 255, 250, 255),
        "rim": (10, 16, 20, 255),
        "rim_hl": (64, 106, 108, 255),
        "tip": (125, 238, 228, 255),
        "tip_hl": (225, 255, 253, 255),
    },
    "spawner_core_t3": {
        "outline": (59, 44, 22, 255),
        "metal": [
            (176, 150, 92, 255),
            (202, 178, 118, 255),
            (228, 208, 156, 255),
            (250, 239, 208, 255),
        ],
        "metal_hl": (255, 254, 242, 255),
        "metal_dk": (120, 96, 50, 255),
        "frame": (148, 120, 64, 255),
        "frame_hl": (220, 196, 140, 255),
        "frame_dk": (112, 88, 44, 255),
        "ring": [(226, 184, 96, 255), (248, 212, 136, 255), (255, 238, 184, 255)],
        "ring_hl": (255, 255, 250, 255),
        "ring_dk": (188, 144, 62, 255),
        "hole": (255, 250, 232, 255),
        "hole_hl": (255, 255, 255, 255),
        "ember": (255, 255, 255, 255),
        "rim": (188, 150, 74, 255),
        "rim_hl": (255, 250, 228, 255),
        "tip": (255, 247, 208, 255),
        "tip_hl": (255, 255, 255, 255),
    },
}


def main():
    os.makedirs(OUT, exist_ok=True)
    for name, palette in PALETTES.items():
        _build(palette).save(os.path.join(OUT, name + ".png"))
    print("generated 3 core textures at 32x32")


if __name__ == "__main__":
    main()
