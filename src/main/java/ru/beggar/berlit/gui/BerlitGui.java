package ru.beggar.berlit.gui;

import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.util.StatCollector;

/**
 * Shared frame for the Berlit screens.
 *
 * <p>
 * A screen is two separate panels cut from one 256x256 texture: the machine on top (200x140) and the
 * player inventory centred below it (176x98 at x=12), with an 8px transparent gap. The sizes live here
 * so the textures in {@code tools/gui_layout.py} and the Java code cannot disagree.
 */
public abstract class BerlitGui extends GuiContainer {

    public static final int PANEL_WIDTH = 200;
    public static final int MACHINE_HEIGHT = 140;

    public static final int INVENTORY_X = 12;
    public static final int INVENTORY_TOP = 148;
    public static final int INVENTORY_WIDTH = 176;
    public static final int INVENTORY_HEIGHT = 98;

    /** Progress bar trough inside the machine panel, in panel coordinates. */
    private static final int BAR_X = 14;
    private static final int BAR_Y = 64;
    private static final int BAR_WIDTH = 172;
    private static final int BAR_HEIGHT = 8;

    private static final int BAR_COLOR = 0xFF4FD6CD;
    private static final int BAR_HIGHLIGHT = 0xFF8CF6EE;

    /** Info badge in the bottom-right corner; the texture draws the badge, this handles the tooltip. */
    private static final int INFO_X = 176;
    private static final int INFO_Y = 116;
    private static final int INFO_SIZE = 14;

    private static final int INVENTORY_LABEL_X = 20;
    private static final int INVENTORY_LABEL_Y = 154;

    protected BerlitGui(Container container) {
        super(container);
        this.xSize = PANEL_WIDTH;
        this.ySize = INVENTORY_TOP + INVENTORY_HEIGHT;
    }

    /** Draws both panels; the gap between them stays transparent. */
    protected void drawPanels(int x, int y) {
        drawTexturedModalRect(x, y, 0, 0, PANEL_WIDTH, MACHINE_HEIGHT);
        drawTexturedModalRect(
            x + INVENTORY_X,
            y + INVENTORY_TOP,
            INVENTORY_X,
            INVENTORY_TOP,
            INVENTORY_WIDTH,
            INVENTORY_HEIGHT);
    }

    /**
     * Fills the trough drawn in the texture. The bar, its frame and its recess come from the texture, so
     * only the moving part is drawn here.
     */
    protected void drawProgressBar(int x, int y, int percent) {
        int clamped = Math.max(0, Math.min(100, percent));
        int filled = BAR_WIDTH * clamped / 100;
        if (filled <= 0) {
            return;
        }
        drawRect(x + BAR_X, y + BAR_Y, x + BAR_X + filled, y + BAR_Y + BAR_HEIGHT, BAR_COLOR);
        drawRect(x + BAR_X, y + BAR_Y, x + BAR_X + filled, y + BAR_Y + 1, BAR_HIGHLIGHT);
    }

    /** Shows a tooltip when the mouse hovers the info badge in the machine panel corner. */
    protected void drawInfoTooltip(int mouseX, int mouseY, List<String> lines) {
        if (mouseX >= INFO_X && mouseX < INFO_X + INFO_SIZE && mouseY >= INFO_Y && mouseY < INFO_Y + INFO_SIZE) {
            drawHoveringText(lines, mouseX, mouseY, fontRendererObj);
        }
    }

    /** Right-aligns a string at {@code right} so percentages line up at the panel edge. */
    protected void drawRightAligned(String text, int right, int y, int color) {
        fontRendererObj.drawString(text, right - fontRendererObj.getStringWidth(text), y, color);
    }

    /**
     * Labels the lower panel. Subclasses call {@code super} first so every screen gets the inventory
     * caption without repeating it.
     */
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRendererObj.drawString(
            StatCollector.translateToLocal("container.inventory"),
            INVENTORY_LABEL_X,
            INVENTORY_LABEL_Y,
            GuiTheme.TEXT);
    }
}
