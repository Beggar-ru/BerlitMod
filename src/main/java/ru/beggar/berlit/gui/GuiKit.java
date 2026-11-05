package ru.beggar.berlit.gui;

import net.minecraft.util.ResourceLocation;

/**
 * The 9-slice kit: one 64x64 texture that every widget-based screen draws from.
 *
 * <p>
 * Layout of {@code textures/gui/kit.png}:
 *
 * <pre>
 *   (0, 0)   24x24 panel   , 8px insets
 *   (24, 0)  16x16 button  , 4px insets
 *   (0, 24)  18x18 cell    , drawn 1:1 per slot
 *   (24, 24) 24x24 tooltip , 8px insets
 *   (0, 48)  12x12 arrow   , drawn 1:1 between slots
 * </pre>
 */
public final class GuiKit {

    public static final ResourceLocation KIT = new ResourceLocation("berlit:textures/gui/kit.png");

    public static final NineSlice PANEL = new NineSlice(KIT, 64, 64, 0, 0, 24, 8);
    public static final NineSlice BUTTON = new NineSlice(KIT, 64, 64, 24, 0, 16, 4);
    public static final NineSlice CELL = new NineSlice(KIT, 64, 64, 0, 24, 18, 1);
    public static final NineSlice TOOLTIP = new NineSlice(KIT, 64, 64, 24, 24, 24, 8);
    public static final NineSlice ARROW = new NineSlice(KIT, 64, 64, 0, 48, 12, 1);

    private GuiKit() {}
}
