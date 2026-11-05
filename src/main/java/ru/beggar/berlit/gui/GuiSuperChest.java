package ru.beggar.berlit.gui;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import ru.beggar.berlit.tileentity.TileEntitySuperChest;

public class GuiSuperChest extends BerlitGui {

    private static final ResourceLocation TEXTURE = new ResourceLocation("berlit:textures/gui/super_chest.png");

    private static final int WIDTH = 502;
    private static final int CONTENT_HEIGHT = 190;
    private static final int INVENTORY_X = (WIDTH - 176) / 2;
    private static final int INVENTORY_TOP = CONTENT_HEIGHT + 8;

    public GuiSuperChest(InventoryPlayer playerInventory, TileEntitySuperChest tile) {
        super(new ContainerSuperChest(playerInventory, tile), WIDTH, INVENTORY_TOP + 98);
        this.textureWidth = 512;
        this.textureHeight = 512;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRendererObj
            .drawString(StatCollector.translateToLocal("tile.berlit.super_chest.name"), 8, 6, GuiTheme.TITLE);
        fontRendererObj.drawString(
            StatCollector.translateToLocal("container.inventory"),
            INVENTORY_X + 8,
            INVENTORY_TOP + 6,
            GuiTheme.TEXT);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager()
            .bindTexture(TEXTURE);

        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;
        drawPanel(x, y, 0, 0, WIDTH, CONTENT_HEIGHT);
        drawPanel(x, y, INVENTORY_X, INVENTORY_TOP, 176, 98);
    }
}
