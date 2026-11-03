package ru.beggar.berlit.gui;

import java.util.Arrays;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import ru.beggar.berlit.tileentity.TileEntityProcessingMachine;

public class GuiProcessingMachine extends BerlitGui {

    private static final ResourceLocation TEXTURE = new ResourceLocation(
        "berlit:textures/gui/molecular_transformer.png");

    private final TileEntityProcessingMachine tile;

    public GuiProcessingMachine(InventoryPlayer playerInventory, TileEntityProcessingMachine tile) {
        super(new ContainerProcessingMachine(playerInventory, tile));
        this.tile = tile;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        fontRendererObj.drawString(
            StatCollector.translateToLocal(
                tile.getMachineType()
                    .getTitleKey()),
            8,
            6,
            GuiTheme.TITLE);

        fontRendererObj.drawString(
            StatCollector.translateToLocalFormatted("berlit.gui.energy", tile.getLastInput()),
            12,
            78,
            GuiTheme.TEXT);

        drawRightAligned(tile.getProgressScaled(100) + "%", 186, 78, GuiTheme.ACCENT);

        drawInfoTooltip(
            mouseX,
            mouseY,
            Arrays.asList(
                StatCollector
                    .translateToLocalFormatted("berlit.gui.info.energy", tile.getEnergy(), tile.getMaxEnergy()),
                StatCollector.translateToLocalFormatted("berlit.gui.info.input", tile.getLastInput()),
                StatCollector.translateToLocalFormatted("berlit.gui.info.recipe", tile.getRecipeEnergy())));
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager()
            .bindTexture(TEXTURE);

        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;
        drawPanels(x, y);
        drawProgressBar(x, y, tile.getProgressScaled(100));
    }
}
