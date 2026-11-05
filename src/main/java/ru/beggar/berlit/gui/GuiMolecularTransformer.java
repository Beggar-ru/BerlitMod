package ru.beggar.berlit.gui;

import java.util.Arrays;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import ru.beggar.berlit.tileentity.TileEntityMolecularTransformer;

public class GuiMolecularTransformer extends BerlitGui {

    private final TileEntityMolecularTransformer tile;

    public GuiMolecularTransformer(InventoryPlayer playerInventory, TileEntityMolecularTransformer tile) {
        super(new ContainerMolecularTransformer(playerInventory, tile));
        this.tile = tile;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        fontRendererObj
            .drawString(StatCollector.translateToLocal("tile.berlit.molecular_transformer.name"), 8, 6, GuiTheme.TITLE);

        fontRendererObj.drawString(
            StatCollector.translateToLocalFormatted("berlit.gui.energy", tile.getLastInput()),
            12,
            78,
            GuiTheme.TEXT);

        // The bar shows the progress; the number is the precise value.
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
            .bindTexture(GuiKit.KIT);

        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;
        drawMachineFrame(x, y, true);
        drawProgressBar(x, y, tile.getProgressScaled(100));
    }
}
