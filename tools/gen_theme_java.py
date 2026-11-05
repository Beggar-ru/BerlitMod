"""Regenerates GuiTheme.java from tools/palette.json.

    python tools/gen_theme_java.py

The palette is the single source of truth: the texture generators read it too, so
the Java text colours and the drawn panels cannot drift apart.
"""

from __future__ import annotations

import json
import os
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
PALETTE = os.path.join(ROOT, "tools", "palette.json")
OUT = os.path.join(ROOT, "src", "main", "java", "ru", "beggar", "berlit", "gui", "GuiTheme.java")

TEMPLATE = """package ru.beggar.berlit.gui;

/**
 * Colours shared by the Berlit screens.
 *
 * <p>
 * Generated from {{@code tools/palette.json}} by {{@code tools/gen_theme_java.py}} - edit the palette, not
 * this file. Keep in sync with {{@code tools/gui_theme.py}}: the textures are drawn from the same values.
 */
public final class GuiTheme {{

    /** Body text on the dark panel. */
    public static final int TEXT = {text};

    /** Screen title, slightly brighter than the body. */
    public static final int TITLE = {title};

    /** Cyan accent, matches the frame highlight in the texture. */
    public static final int ACCENT = {accent};

    /** Dimmed text for disabled widgets. */
    public static final int TEXT_DISABLED = {disabled};

    /** Panel gradient, identical to the machine textures. */
    public static final int PANEL_TOP = {panel_top};

    public static final int PANEL_BOTTOM = {panel_bottom};

    /** Frame highlight, matches gui_theme.FRAME_LIGHT. */
    public static final int FRAME_LIGHT = {frame_light};

    /** Progress bar frame and trough. */
    public static final int BAR_FRAME = {bar_frame};

    public static final int BAR_EDGE = {bar_edge};

    public static final int BAR_TROUGH = {bar_trough};

    /** Title rule under the header, dimmer than the frame. */
    public static final int FRAME_BORDER = {frame_border};

    public static final int FRAME_RULE = {frame_rule};

    /** Button plate and its border. */
    public static final int BUTTON_FILL = {button_fill};

    public static final int BUTTON_BORDER = {button_border};

    private GuiTheme() {{}}
}}
"""


def argb(hex_colour):
    value = hex_colour.lstrip("#")
    return "0xFF" + value.upper()


def main():
    with open(PALETTE, encoding="utf-8") as handle:
        palette = json.load(handle)

    source = TEMPLATE.format(
        text=argb(palette["text"]["base"]),
        title=argb(palette["text"]["title"]),
        accent=argb(palette["text"]["accent"]),
        disabled=argb(palette["text"]["disabled"]),
        panel_top=argb(palette["panel"]["top"]),
        panel_bottom=argb(palette["panel"]["bottom"]),
        frame_light=argb(palette["frame"]["light"]),
        button_fill=argb(palette["button"]["fill"]),
        button_border=argb(palette["button"]["border"]),
        frame_border=argb(palette["frame"]["cyan"]),
        frame_rule=argb(palette["frame"]["rule"]),
        bar_frame=argb(palette["progress"]["frame"]),
        bar_edge=argb(palette["progress"]["frame_edge"]),
        bar_trough=argb(palette["progress"]["trough"]),
    )

    with open(OUT, "w", encoding="utf-8", newline="\n") as handle:
        handle.write(source)
    print("written", OUT)
    return 0


if __name__ == "__main__":
    sys.exit(main())
