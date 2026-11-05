package ru.beggar.berlit.machine;

/**
 * Geometry of the neutron compressor screen, shared by the Container (slot positions) and the Gui
 * (everything drawn at runtime from the 9-slice kit).
 */
public final class CompressorLayout {

    public static final int SLOT = 18;

    public static final int WIDTH = 176;
    public static final int CONTENT_HEIGHT = 90;
    public static final int INVENTORY_TOP = 98;
    public static final int INVENTORY_HEIGHT = 98;

    public static final int INPUT_X = 44;
    public static final int PATTERN_X = 80;
    public static final int OUTPUT_X = 116;
    public static final int SLOT_Y = 20;

    public static final int PLAYER_X = 8;
    public static final int PLAYER_TOP = 116;
    public static final int HOTBAR_Y = 172;

    public static final int BUTTON_X = 140;
    public static final int BUTTON_CRAFT_Y = 18;
    public static final int BUTTON_CRAFT64_Y = 40;
    public static final int BUTTON_WIDTH = 22;
    public static final int BUTTON_HEIGHT = 20;

    private CompressorLayout() {}
}
