package ru.beggar.berlit.ae2;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

public class GuiAvaritiaAssembler extends GuiContainer {

    private static final ResourceLocation TEXTURE = new ResourceLocation("berlit:textures/gui/avaritia_assembler.png");

    public GuiAvaritiaAssembler(InventoryPlayer playerInventory, TileEntityAvaritiaAssembler tile) {
        super(new ContainerAvaritiaAssembler(playerInventory, tile));
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRendererObj
            .drawString(StatCollector.translateToLocal("tile.berlit.avaritia_assembler.name"), 8, 6, 0xE8F1F5);
        fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 8, 72, 0xC8D2DE);
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
