package ru.beggar.berlit.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import ru.beggar.berlit.spawner.UpgradeRegistry;
import ru.beggar.berlit.tileentity.TileEntitySpawnerBase;

public class GuiSpawner extends GuiContainer {

    private final TileEntitySpawnerBase tile;
    private final ResourceLocation texture;

    public GuiSpawner(InventoryPlayer playerInventory, TileEntitySpawnerBase tile) {
        super(new ContainerSpawner(playerInventory, tile));
        this.tile = tile;
        this.texture = new ResourceLocation(tile.getGuiTexture());
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRendererObj.drawString(StatCollector.translateToLocal(tile.getTitleKey()), 8, 6, 4210752);

        fontRendererObj.drawString(
            StatCollector.translateToLocalFormatted("berlit.gui.spawner.entities", tile.getTotalEntityCount()),
            8,
            60,
            4210752);
        fontRendererObj.drawString(
            StatCollector.translateToLocalFormatted("berlit.gui.spawner.cycle", tile.getCycleScaled(100)),
            8,
            72,
            4210752);

        int upgrades = 0;
        for (int i = tile.getUpgradeSlotStart(); i < tile.getSlotCount(); i++) {
            if (UpgradeRegistry.get(tile.getStackInSlot(i)) != null) upgrades++;
        }
        fontRendererObj.drawString(
            StatCollector.translateToLocalFormatted("berlit.gui.spawner.upgrades", upgrades),
            108,
            60,
            4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager()
            .bindTexture(texture);

        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;
        drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
    }
}
