"""Draws the NEI recipe pages in the dark slate + cyan theme.

    python tools/gen_nei_pages.py

Pages come from ``tools/gui_layout.NEI_PAGES``. One 18x18 cell is pasted under every
item position the handler uses, so the background always lines up with the
PositionedStack coordinates in ``compat/nei/*NEI.java``; move a stack and the page has
to be regenerated. ``tools/preview.py check`` verifies the result.
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


def build(page):
    width, height = page["page"]
    image = theme.new_panel(width, height)
    theme.draw_title_strip(image)

    for cx, cy in page.get("arrows", ()):
        theme.draw_flow_arrow(image, cx, cy)
    theme.paste_slots(image, page["slots"])

    theme.draw_frame(image)
    theme.draw_inner_shadow(image)
    theme.draw_corner_brackets(image)

    # NEI samples the page with vanilla's fixed 1/256 UV scale, so the 176x166 page has to
    # sit at the top-left corner of a 256x256 canvas - a 176x166 file would tile.
    canvas = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
    canvas.alpha_composite(image, (0, 0))
    return canvas


def main():
    for name, page in layout.NEI_PAGES.items():
        image = build(page)
        path = os.path.join(TEXTURES, *page["texture"].split("/"))
        os.makedirs(os.path.dirname(path), exist_ok=True)
        image.save(path)
        print(f"{name:24} -> {page['texture']}  {image.size[0]}x{image.size[1]}")


if __name__ == "__main__":
    main()
