package ru.beggar.berlit.gui;

/**
 * Colours shared by the Berlit screens.
 *
 * <p>
 * Keep in sync with {@code tools/gui_theme.py}: the textures are drawn from the same palette, and the
 * text has to stay readable on the dark panel.
 */
public final class GuiTheme {

    /** Body text on the dark panel. */
    public static final int TEXT = 0xC8D2DE;

    /** Screen title, slightly brighter than the body. */
    public static final int TITLE = 0xE8F1F5;

    /** Cyan accent, matches the frame highlight in the texture. */
    public static final int ACCENT = 0x6FE9E0;

    /** Panel gradient, identical to the machine textures (gui_theme.BG_TOP / BG_BOTTOM). */
    public static final int PANEL_TOP = 0xFF363B48;
    public static final int PANEL_BOTTOM = 0xFF262A34;

    /** Frame highlight, matches gui_theme.FRAME_LIGHT. */
    public static final int FRAME_LIGHT = 0xFF6FE9E0;

    private GuiTheme() {}
}
