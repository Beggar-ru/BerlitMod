package ru.beggar.berlit.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import ru.beggar.berlit.item.ItemEntityCard;
import ru.beggar.berlit.spawner.UpgradeRegistry;
import ru.beggar.berlit.tileentity.TileEntitySpawner;

public class ContainerSpawner extends Container {

    private static final int SLOT_SIZE = 18;
    private static final int ENTITY_X = 26;
    private static final int ENTITY_Y = 17;
    private static final int UPGRADE_X = 62;
    private static final int UPGRADE_Y = 17;

    private final TileEntitySpawner tile;

    public ContainerSpawner(InventoryPlayer playerInventory, TileEntitySpawner tile) {
        this.tile = tile;

        addSlotToContainer(new Slot(tile, TileEntitySpawner.SLOT_ENTITY, ENTITY_X, ENTITY_Y));

        for (int i = 0; i < TileEntitySpawner.SLOT_UPGRADE_COUNT; i++) {
            addSlotToContainer(
                new Slot(
                    tile,
                    TileEntitySpawner.SLOT_UPGRADE_START + i,
                    UPGRADE_X + (i % 4) * SLOT_SIZE,
                    UPGRADE_Y + (i / 4) * SLOT_SIZE) {

                    @Override
                    public boolean isItemValid(ItemStack stack) {
                        return UpgradeRegistry.get(stack) != null;
                    }
                });
        }

        bindPlayerInventory(playerInventory);
    }

    private void bindPlayerInventory(InventoryPlayer inventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(inventory, col + row * 9 + 9, 8 + col * SLOT_SIZE, 84 + row * SLOT_SIZE));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(inventory, col, 8 + col * SLOT_SIZE, 142));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return tile.isUseableByPlayer(player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack result = null;
        Slot slot = (Slot) inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            result = stack.copy();

            if (index < TileEntitySpawner.SLOT_COUNT) {
                if (!mergeItemStack(stack, TileEntitySpawner.SLOT_COUNT, inventorySlots.size(), true)) {
                    return null;
                }
            } else if (stack.getItem() instanceof ItemEntityCard) {
                if (!mergeItemStack(stack, TileEntitySpawner.SLOT_ENTITY, TileEntitySpawner.SLOT_ENTITY + 1, false)) {
                    return null;
                }
            } else if (UpgradeRegistry.get(stack) != null) {
                if (!mergeItemStack(stack, TileEntitySpawner.SLOT_UPGRADE_START, TileEntitySpawner.SLOT_COUNT, false)) {
                    return null;
                }
            } else {
                return null;
            }

            if (stack.stackSize == 0) {
                slot.putStack(null);
            } else {
                slot.onSlotChanged();
            }
        }

        return result;
    }
}
