package ru.beggar.berlit.gui;

/**
 * Geometry of the machine screens (transformer, macerator, compressor, extruder, roller, furnace),
 * shared by their Containers and Guis. The screen is a 200x140 panel with the progress bar and upgrade
 * well, plus the 176x98 inventory panel 8px below it.
 */
public final class MachineLayout {

    public static final int SLOT = 18;

    public static final int WIDTH = 200;
    public static final int CONTENT_HEIGHT = 140;

    public static final int INVENTORY_X = 12;
    public static final int INVENTORY_TOP = 148;
    public static final int INVENTORY_WIDTH = 176;
    public static final int INVENTORY_HEIGHT = 98;

    /** 3x2 input and 3x2 output groups. */
    public static final int INPUT_X = 38;
    public static final int INPUT_Y = 22;
    public static final int OUTPUT_X = 110;
    public static final int OUTPUT_Y = 22;

    /** Four upgrade slots under the bar. */
    public static final int UPGRADE_X = 64;
    public static final int UPGRADE_Y = 92;

    public static final int PLAYER_X = 20;
    public static final int PLAYER_TOP = 166;
    public static final int HOTBAR_Y = 222;

    /** Progress bar frame and the trough inside it. */
    public static final int BAR_X = 12;
    public static final int BAR_Y = 62;
    public static final int BAR_WIDTH = 176;
    public static final int BAR_HEIGHT = 12;
    public static final int TROUGH_X = 14;
    public static final int TROUGH_Y = 64;
    public static final int TROUGH_WIDTH = 172;
    public static final int TROUGH_HEIGHT = 8;

    /** Chevron between the input and output groups. */
    public static final int ARROW_X = 94;
    public static final int ARROW_Y = 34;

    /** Info badge in the bottom-right corner. */
    public static final int INFO_X = 176;
    public static final int INFO_Y = 116;
    public static final int INFO_SIZE = 14;

    private MachineLayout() {}
}
