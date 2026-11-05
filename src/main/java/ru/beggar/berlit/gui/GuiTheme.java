package ru.beggar.berlit.gui;

/**
 * Colours shared by the Berlit screens.
 *
 * <p>
 * Generated from {@code tools/palette.json} by {@code tools/gen_theme_java.py} - edit the palette, not
 * this file. Keep in sync with {@code tools/gui_theme.py}: the textures are drawn from the same values.
 */
public final class GuiTheme {

    /** Body text on the dark panel. */
    public static final int TEXT = 0xFFC8D2DE;

    /** Screen title, slightly brighter than the body. */
    public static final int TITLE = 0xFFE8F1F5;

    /** Cyan accent, matches the frame highlight in the texture. */
    public static final int ACCENT = 0xFF6FE9E0;

    /** Dimmed text for disabled widgets. */
    public static final int TEXT_DISABLED = 0xFF6B7382;

    /** Panel gradient, identical to the machine textures. */
    public static final int PANEL_TOP = 0xFF363B48;

    public static final int PANEL_BOTTOM = 0xFF262A34;

    /** Frame highlight, matches gui_theme.FRAME_LIGHT. */
    public static final int FRAME_LIGHT = 0xFF6FE9E0;

    /** Progress bar frame and trough. */
    public static final int BAR_FRAME = 0xFF2E3340;

    public static final int BAR_EDGE = 0xFF14171D;

    public static final int BAR_TROUGH = 0xFF12151B;

    /** Title rule under the header, dimmer than the frame. */
    public static final int FRAME_BORDER = 0xFF2C8C8A;

    public static final int FRAME_RULE = 0xFF386868;

    /** Button plate and its border. */
    public static final int BUTTON_FILL = 0xFF323A48;

    public static final int BUTTON_BORDER = 0xFF2C8C8A;

    private GuiTheme() {}
}
