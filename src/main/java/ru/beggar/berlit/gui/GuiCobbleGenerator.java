package ru.beggar.berlit.gui;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import ru.beggar.berlit.tileentity.TileEntityCobbleGenerator;

/**
 * Screen for the cobblestone generator. Uses the shared Berlit frame (dark panel, progress bar, info badge) so it sits
 * alongside the other machines.
 */
public class GuiCobbleGenerator extends BerlitGui {

    private final TileEntityCobbleGenerator tile;

    public GuiCobbleGenerator(InventoryPlayer playerInventory, TileEntityCobbleGenerator tile) {
        super(new ContainerCobbleGenerator(playerInventory, tile));
        this.tile = tile;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        fontRendererObj.drawString(StatCollector.translateToLocal(tile.getInventoryName()), 8, 6, GuiTheme.TITLE);

        fontRendererObj.drawString(
            StatCollector.translateToLocalFormatted("berlit.gui.energy", tile.getLastInput()),
            12,
            78,
            GuiTheme.TEXT);

        drawRightAligned(tile.getProgressScaled(100) + "%", 186, 78, GuiTheme.ACCENT);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager()
            .bindTexture(GuiKit.KIT);

        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;
        drawMachineFrame(x, y, false);
        drawProgressBar(x, y, tile.getProgressScaled(100));
    }
}
