package ru.beggar.berlit.gui;

import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.Tessellator;
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
        this(container, PANEL_WIDTH, INVENTORY_TOP + INVENTORY_HEIGHT);
    }

    /** For screens whose panels are sized by their own GuiContainer (AE2, chests). */
    protected BerlitGui(Container container, int width, int height) {
        super(container);
        this.xSize = width;
        this.ySize = height;
    }

    /** Texture size of the screen; subclasses override it for 256x512 and 512x512 textures. */
    protected int textureWidth = 256;
    protected int textureHeight = 256;

    /** Draws a panel 1:1 from the texture at the coordinates the layout uses. */
    protected void drawPanel(int guiX, int guiY, int panelX, int panelY, int width, int height) {
        drawTextureRegion(guiX + panelX, guiY + panelY, panelX, panelY, width, height, textureWidth, textureHeight);
    }

    /**
     * Draws a region with UVs derived from the actual texture size.
     *
     * <p>
     * {@code drawTexturedModalRect} hardcodes 1/256, so on a 256x512 texture it samples the wrong rows
     * and on a 512x512 one it tiles the image four times.
     */
    protected void drawTextureRegion(int x, int y, int u, int v, int width, int height, int texWidth, int texHeight) {
        float uvWidth = 1.0F / texWidth;
        float uvHeight = 1.0F / texHeight;

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x, y + height, this.zLevel, u * uvWidth, (v + height) * uvHeight);
        tessellator.addVertexWithUV(x + width, y + height, this.zLevel, (u + width) * uvWidth, (v + height) * uvHeight);
        tessellator.addVertexWithUV(x + width, y, this.zLevel, (u + width) * uvWidth, v * uvHeight);
        tessellator.addVertexWithUV(x, y, this.zLevel, u * uvWidth, v * uvHeight);
        tessellator.draw();
    }

    /** Kit panel: machine gradient, 9-slice frame ring, 14px corner brackets. */
    protected void drawKitPanel(int x, int y, int width, int height) {
        drawGradientRect(x, y, x + width, y + height, GuiTheme.PANEL_TOP, GuiTheme.PANEL_BOTTOM);
        GuiKit.PANEL.draw(zLevel, x, y, width, height);
        drawBracket(x + 4, y + 4, 1, 1);
        drawBracket(x + width - 5, y + 4, -1, 1);
        drawBracket(x + 4, y + height - 5, 1, -1);
        drawBracket(x + width - 5, y + height - 5, -1, -1);
    }

    protected void drawBracket(int x, int y, int dx, int dy) {
        int arm = 14;
        drawRect(Math.min(x, x + dx * arm), y, Math.max(x, x + dx * arm) + 1, y + 1, GuiTheme.FRAME_LIGHT);
        drawRect(x, Math.min(y, y + dy * arm), x + 1, Math.max(y, y + dy * arm) + 1, GuiTheme.FRAME_LIGHT);
    }

    /** One 18x18 slot cell from the kit, at the slot's own coordinates. */
    protected void drawKitCell(int x, int y, int slotX, int slotY) {
        GuiKit.CELL.drawSprite(zLevel, x + slotX - 1, y + slotY - 1, MachineLayout.SLOT, MachineLayout.SLOT);
    }

    protected void drawKitArrow(int x, int y) {
        GuiKit.ARROW.drawSprite(zLevel, x, y, 12, 12);
    }

    /** Progress bar frame and trough; the moving fill is drawn by {@link #drawProgressBar}. */
    protected void drawProgressTrough(int x, int y) {
        int fx = x + MachineLayout.BAR_X;
        int fy = y + MachineLayout.BAR_Y;
        drawRect(fx, fy, fx + MachineLayout.BAR_WIDTH, fy + MachineLayout.BAR_HEIGHT, GuiTheme.BAR_EDGE);
        drawRect(
            fx + 1,
            fy + 1,
            fx + MachineLayout.BAR_WIDTH - 1,
            fy + MachineLayout.BAR_HEIGHT - 1,
            GuiTheme.BAR_FRAME);
        drawRect(fx + 1, fy + 1, fx + MachineLayout.BAR_WIDTH - 1, fy + 2, GuiTheme.BAR_FRAME);

        int tx = x + MachineLayout.TROUGH_X;
        int ty = y + MachineLayout.TROUGH_Y;
        drawRect(tx, ty, tx + MachineLayout.TROUGH_WIDTH, ty + MachineLayout.TROUGH_HEIGHT, GuiTheme.BAR_TROUGH);
        drawRect(tx, ty, tx + MachineLayout.TROUGH_WIDTH, ty + 1, GuiTheme.BAR_EDGE);
    }

    /** Small "i" badge; the tooltip itself comes from {@link #drawInfoTooltip}. */
    protected void drawInfoBadge(int x, int y) {
        int size = MachineLayout.INFO_SIZE;
        drawRect(x, y, x + size, y + size, GuiTheme.BAR_EDGE);
        drawRect(x + 1, y + 1, x + size - 1, y + size - 1, GuiTheme.PANEL_BOTTOM);
        drawRect(x + 1, y + 1, x + size - 1, y + 2, GuiTheme.FRAME_BORDER);
        drawRect(x + 1, y + size - 2, x + size - 1, y + size - 1, GuiTheme.BAR_EDGE);

        int cx = x + size / 2;
        drawRect(cx, y + 4, cx + 2, y + 6, GuiTheme.FRAME_LIGHT);
        drawRect(cx, y + 7, cx + 2, y + size - 3, GuiTheme.FRAME_LIGHT);
    }

    /** Whole machine screen: both panels, every cell, the arrow, the bar trough and the info badge. */
    protected void drawMachineFrame(int x, int y) {
        drawKitPanel(x, y, MachineLayout.WIDTH, MachineLayout.CONTENT_HEIGHT);
        drawKitPanel(x, y + MachineLayout.INVENTORY_TOP, MachineLayout.INVENTORY_WIDTH, MachineLayout.INVENTORY_HEIGHT);

        drawRect(x + 8, y + 15, x + MachineLayout.WIDTH - 9, y + 16, GuiTheme.FRAME_BORDER);
        drawRect(x + 8, y + 16, x + MachineLayout.WIDTH - 9, y + 17, GuiTheme.FRAME_RULE);

        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 3; col++) {
                drawKitCell(
                    x,
                    y,
                    MachineLayout.INPUT_X + col * MachineLayout.SLOT,
                    MachineLayout.INPUT_Y + row * MachineLayout.SLOT);
                drawKitCell(
                    x,
                    y,
                    MachineLayout.OUTPUT_X + col * MachineLayout.SLOT,
                    MachineLayout.OUTPUT_Y + row * MachineLayout.SLOT);
            }
        }
        for (int i = 0; i < 4; i++) {
            drawKitCell(x, y, MachineLayout.UPGRADE_X + i * MachineLayout.SLOT, MachineLayout.UPGRADE_Y);
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawKitCell(
                    x,
                    y,
                    MachineLayout.PLAYER_X + col * MachineLayout.SLOT,
                    MachineLayout.PLAYER_TOP + row * MachineLayout.SLOT);
            }
        }
        for (int col = 0; col < 9; col++) {
            drawKitCell(x, y, MachineLayout.PLAYER_X + col * MachineLayout.SLOT, MachineLayout.HOTBAR_Y);
        }

        drawKitArrow(x + MachineLayout.ARROW_X, y + MachineLayout.ARROW_Y);
        drawProgressTrough(x, y);
        drawInfoBadge(x + MachineLayout.INFO_X, y + MachineLayout.INFO_Y);
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
