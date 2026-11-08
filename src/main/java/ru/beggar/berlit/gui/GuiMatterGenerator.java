package ru.beggar.berlit.gui;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import ru.beggar.berlit.matter.MatterType;
import ru.beggar.berlit.tileentity.TileEntityMatterGenerator;

/** Solid matter generator: one input, one output, a progress bar - drawn from the 9-slice kit. */
public class GuiMatterGenerator extends BerlitGui {

    private final TileEntityMatterGenerator tile;

    public GuiMatterGenerator(InventoryPlayer playerInventory, TileEntityMatterGenerator tile) {
        super(
            new ContainerMatterGenerator(playerInventory, tile),
            MatterLayout.WIDTH,
            MatterLayout.INVENTORY_TOP + MatterLayout.INVENTORY_HEIGHT);
        this.tile = tile;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        MatterType type = tile.getMatterType();
        fontRendererObj.drawString(StatCollector.translateToLocal(type.getBlockKey()), 8, 6, GuiTheme.TITLE);
        fontRendererObj.drawString(
            StatCollector.translateToLocal("container.inventory"),
            8,
            MatterLayout.INVENTORY_TOP + 6,
            GuiTheme.TEXT);

        String energy = tile.getEnergy() + " / " + type.getEnergyCapacity() + " EU";
        drawRightAligned(energy, MatterLayout.WIDTH - 10, MatterLayout.BAR_Y - 12, GuiTheme.TEXT);

        String eu = type.getEnergyPerOperation() + " EU";
        drawRightAligned(
            eu,
            MatterLayout.WIDTH - 10,
            MatterLayout.BAR_Y + MatterLayout.BAR_HEIGHT + 2,
            GuiTheme.TEXT_DISABLED);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager()
            .bindTexture(GuiKit.KIT);

        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;

        drawKitPanel(x, y, MatterLayout.WIDTH, MatterLayout.CONTENT_HEIGHT);
        drawKitPanel(
            x + MatterLayout.INVENTORY_X,
            y + MatterLayout.INVENTORY_TOP,
            MatterLayout.INVENTORY_WIDTH,
            MatterLayout.INVENTORY_HEIGHT);

        drawRect(x + 8, y + 15, x + MatterLayout.WIDTH - 9, y + 16, GuiTheme.FRAME_BORDER);
        drawRect(x + 8, y + 16, x + MatterLayout.WIDTH - 9, y + 17, GuiTheme.FRAME_RULE);

        drawKitCell(x, y, MatterLayout.INPUT_X, MatterLayout.SLOT_Y);
        drawKitCell(x, y, MatterLayout.OUTPUT_X, MatterLayout.SLOT_Y);
        drawKitArrow(x + MatterLayout.ARROW_X, y + MatterLayout.ARROW_Y);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawKitCell(
                    x,
                    y,
                    MatterLayout.PLAYER_X + col * MatterLayout.SLOT,
                    MatterLayout.PLAYER_TOP + row * MatterLayout.SLOT);
            }
        }
        for (int col = 0; col < 9; col++) {
            drawKitCell(x, y, MatterLayout.PLAYER_X + col * MatterLayout.SLOT, MatterLayout.HOTBAR_Y);
        }

        int fx = x + MatterLayout.BAR_X;
        int fy = y + MatterLayout.BAR_Y;
        drawRect(fx, fy, fx + MatterLayout.BAR_WIDTH, fy + MatterLayout.BAR_HEIGHT, GuiTheme.BAR_EDGE);
        drawRect(fx + 1, fy + 1, fx + MatterLayout.BAR_WIDTH - 1, fy + MatterLayout.BAR_HEIGHT - 1, GuiTheme.BAR_FRAME);

        int tx = x + MatterLayout.TROUGH_X;
        int ty = y + MatterLayout.TROUGH_Y;
        drawRect(tx, ty, tx + MatterLayout.TROUGH_WIDTH, ty + MatterLayout.TROUGH_HEIGHT, GuiTheme.BAR_TROUGH);

        int percent = tile.getProgressPercent();
        if (percent > 0) {
            int filled = Math.max(1, MatterLayout.TROUGH_WIDTH * percent / 100);
            drawRect(tx, ty, tx + filled, ty + MatterLayout.TROUGH_HEIGHT, GuiTheme.BAR_FILL);
            drawRect(tx, ty, tx + filled, ty + 1, GuiTheme.BAR_FILL_EDGE);
        }
    }
}
