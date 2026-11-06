package ru.beggar.berlit.gui;

/**
 * Geometry of the replicator screen, shared by its container and GUI.
 *
 * <p>
 * The panel is the standard 200x140 Berlit machine panel. One scan slot, one whole-stack output
 * slot, the UU matter input/empty-cell pair and four upgrade slots fit around the usual progress
 * bar; the remembered item is drawn as a ghost between the scan slot and the output.
 */
public final class ReplicatorLayout {

    public static final int SCAN_X = 38;
    public static final int SCAN_Y = 22;

    public static final int OUTPUT_X = 110;
    public static final int OUTPUT_Y = 22;

    public static final int FLUID_X = 38;
    public static final int FLUID_Y = 44;

    public static final int EMPTY_X = 56;
    public static final int EMPTY_Y = 44;

    /** Ghost of the selected pattern, between the scan slot and the output. */
    public static final int GHOST_X = 76;
    public static final int GHOST_Y = 22;

    /** Previous/next pattern buttons flanking the ghost. */
    public static final int BUTTON_Y = 24;
    public static final int PREV_X = 58;
    public static final int NEXT_X = 95;
    public static final int BUTTON_WIDTH = 14;
    public static final int BUTTON_HEIGHT = 14;

    /** Vertical UU matter gauge on the far left. */
    public static final int TANK_X = 12;
    public static final int TANK_Y = 22;
    public static final int TANK_WIDTH = 14;
    public static final int TANK_HEIGHT = 40;

    private ReplicatorLayout() {}
}
