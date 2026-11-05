package ru.beggar.berlit.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import ru.beggar.berlit.tileentity.TileEntitySuperChest;

public class GuiSuperChest extends GuiContainer {

    private static final ResourceLocation TEXTURE = new ResourceLocation("berlit:textures/gui/super_chest.png");

    public GuiSuperChest(InventoryPlayer playerInventory, TileEntitySuperChest tile) {
        super(new ContainerSuperChest(playerInventory, tile));
        this.xSize = 502;
        this.ySize = 278;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRendererObj.drawString(StatCollector.translateToLocal("tile.berlit.super_chest.name"), 8, 6, 0xE8F1F5);
        fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 172, 182, 0xC8D2DE);
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
