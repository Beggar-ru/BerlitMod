package ru.beggar.berlit.gui;

import java.util.Arrays;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import ru.beggar.berlit.tileentity.TileEntitySpawnerBase;

public class GuiSpawner extends BerlitGui {

    private final TileEntitySpawnerBase tile;
    private final ResourceLocation texture;

    public GuiSpawner(InventoryPlayer playerInventory, TileEntitySpawnerBase tile) {
        super(new ContainerSpawner(playerInventory, tile));
        this.tile = tile;
        this.texture = new ResourceLocation(tile.getGuiTexture());
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        fontRendererObj.drawString(StatCollector.translateToLocal(tile.getTitleKey()), 8, 6, GuiTheme.TITLE);

        fontRendererObj.drawString(
            StatCollector.translateToLocalFormatted("berlit.gui.spawner.entities", tile.getTotalEntityCount()),
            12,
            78,
            GuiTheme.TEXT);

        // The cycle bar fills the trough, so the number sits at the right edge.
        drawRightAligned(tile.getCycleScaled(100) + "%", 186, 78, GuiTheme.ACCENT);

        drawInfoTooltip(
            mouseX,
            mouseY,
            Arrays.asList(
                StatCollector.translateToLocalFormatted("berlit.gui.spawner.entities", tile.getTotalEntityCount()),
                StatCollector.translateToLocalFormatted("berlit.gui.info.interval", tile.getInterval())));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager()
            .bindTexture(texture);

        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;
        drawPanels(x, y);
        drawProgressBar(x, y, tile.getCycleScaled(100));
    }
}
