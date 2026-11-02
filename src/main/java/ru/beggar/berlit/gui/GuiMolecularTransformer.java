package ru.beggar.berlit.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import ru.beggar.berlit.tileentity.TileEntityMolecularTransformer;

public class GuiMolecularTransformer extends GuiContainer {

    private static final ResourceLocation TEXTURE = new ResourceLocation(
        "berlit:textures/gui/molecular_transformer.png");

    private final TileEntityMolecularTransformer tile;

    public GuiMolecularTransformer(InventoryPlayer playerInventory, TileEntityMolecularTransformer tile) {
        super(new ContainerMolecularTransformer(playerInventory, tile));
        this.tile = tile;
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String title = StatCollector.translateToLocal("tile.berlit.molecular_transformer.name");
        fontRendererObj.drawString(title, 8, 6, 4210752);

        fontRendererObj.drawString(
            StatCollector.translateToLocalFormatted("berlit.gui.energy", tile.getLastInput()),
            8,
            60,
            4210752);
        fontRendererObj.drawString(
            StatCollector.translateToLocalFormatted("berlit.gui.progress", tile.getProgressScaled(100)),
            8,
            72,
            4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager()
            .bindTexture(TEXTURE);

        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;
        drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
    }
}
