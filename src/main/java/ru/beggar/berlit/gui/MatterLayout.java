package ru.beggar.berlit.gui;

/** Geometry of the solid matter generator screen (176x90 panel + inventory), shared by Container and Gui. */
public final class MatterLayout {

    public static final int SLOT = 18;

    public static final int WIDTH = 190;
    public static final int CONTENT_HEIGHT = 90;
    /** Inventory panel is the usual 176px block, centred under the wider content panel. */
    public static final int INVENTORY_X = 7;
    public static final int INVENTORY_WIDTH = 176;
    public static final int INVENTORY_TOP = 98;
    public static final int INVENTORY_HEIGHT = 98;

    public static final int INPUT_X = 52;
    public static final int OUTPUT_X = 122;
    public static final int SLOT_Y = 40;

    /** Chevron between the two slots, centred on the slot tiles. */
    public static final int ARROW_X = 83;
    public static final int ARROW_Y = 42;

    public static final int BAR_X = 19;
    public static final int BAR_Y = 62;
    public static final int BAR_WIDTH = 152;
    public static final int BAR_HEIGHT = 12;
    public static final int TROUGH_X = 21;
    public static final int TROUGH_Y = 64;
    public static final int TROUGH_WIDTH = 148;
    public static final int TROUGH_HEIGHT = 8;

    public static final int PLAYER_X = 15;
    public static final int PLAYER_TOP = 116;
    public static final int HOTBAR_Y = 172;

    private MatterLayout() {}
}
