package ru.beggar.berlit.gui;

import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import ru.beggar.berlit.item.ItemEntityCard;
import ru.beggar.berlit.spawner.EntityDefinition;
import ru.beggar.berlit.spawner.EntityRegistry;
import ru.beggar.berlit.spawner.UpgradeRegistry;
import ru.beggar.berlit.tileentity.TileEntitySpawner;

public class GuiSpawner extends GuiContainer {

    private static final ResourceLocation TEXTURE = new ResourceLocation("berlit:textures/gui/spawner.png");

    private final TileEntitySpawner tile;

    public GuiSpawner(InventoryPlayer playerInventory, TileEntitySpawner tile) {
        super(new ContainerSpawner(playerInventory, tile));
        this.tile = tile;
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRendererObj.drawString(StatCollector.translateToLocal("tile.berlit.spawner.name"), 8, 6, 4210752);

        String entityName = "-";
        List<?> slots = inventorySlots.inventorySlots;
        if (!slots.isEmpty()) {
            ItemStack stack = ((net.minecraft.inventory.Slot) slots.get(0)).getStack();
            EntityDefinition definition = EntityRegistry.get(ItemEntityCard.getEntityId(stack));
            if (definition != null) {
                entityName = definition.getDisplayName();
            }
        }

        fontRendererObj.drawString(
            StatCollector.translateToLocalFormatted("berlit.gui.spawner.entity", entityName),
            8,
            60,
            4210752);
        fontRendererObj.drawString(
            StatCollector.translateToLocalFormatted("berlit.gui.spawner.cycle", tile.getCycleScaled(100)),
            8,
            72,
            4210752);

        int upgrades = 0;
        for (int i = TileEntitySpawner.SLOT_UPGRADE_START; i < TileEntitySpawner.SLOT_COUNT; i++) {
            if (UpgradeRegistry.get(tile.getStackInSlot(i)) != null) upgrades++;
        }
        fontRendererObj.drawString(
            StatCollector.translateToLocalFormatted("berlit.gui.spawner.upgrades", upgrades),
            98,
            60,
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
