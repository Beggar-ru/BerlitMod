package ru.beggar.berlit.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import ru.beggar.berlit.item.ItemEntityCard;
import ru.beggar.berlit.spawner.UpgradeRegistry;
import ru.beggar.berlit.tileentity.TileEntitySpawnerBase;

public class ContainerSpawner extends Container {

    private static final int SLOT_SIZE = 18;

    private final TileEntitySpawnerBase tile;

    public ContainerSpawner(InventoryPlayer playerInventory, TileEntitySpawnerBase tile) {
        this.tile = tile;

        for (int i = 0; i < tile.getEntitySlotCount(); i++) {
            addSlotToContainer(new Slot(tile, i, tile.getEntitySlotX(i), tile.getEntitySlotY(i)) {

                @Override
                public int getSlotStackLimit() {
                    return tile.getEntitySlotLimit();
                }
            });
        }

        for (int i = 0; i < TileEntitySpawnerBase.UPGRADE_SLOTS; i++) {
            final int index = tile.getUpgradeSlotStart() + i;
            addSlotToContainer(new Slot(tile, index, tile.getUpgradeSlotX(i), tile.getUpgradeSlotY(i)) {

                @Override
                public boolean isItemValid(ItemStack stack) {
                    return tile.canAcceptUpgrade(index, stack);
                }

                /** One core per slot: the rest of the held stack stays in the cursor. */
                @Override
                public int getSlotStackLimit() {
                    return 1;
                }
            });
        }

        bindPlayerInventory(playerInventory);
    }

    private void bindPlayerInventory(InventoryPlayer inventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(inventory, col + row * 9 + 9, 20 + col * SLOT_SIZE, 166 + row * SLOT_SIZE));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(inventory, col, 20 + col * SLOT_SIZE, 222));
        }
    }

    /**
     * Shift-click path for cores: one core per slot and none twice, so each core kind is
     * placed into the first upgrade slot that still accepts it. Anything that does not fit
     * stays in the player's inventory.
     */
    private boolean mergeUpgrades(ItemStack stack) {
        boolean moved = false;

        for (int i = tile.getUpgradeSlotStart(); i < tile.getSlotCount() && stack.stackSize > 0; i++) {
            if (tile.getStackInSlot(i) != null) continue;
            if (!tile.canAcceptUpgrade(i, stack)) continue;

            ItemStack single = stack.copy();
            single.stackSize = 1;
            tile.setInventorySlotContents(i, single);
            stack.stackSize--;
            moved = true;
        }

        return moved;
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

            int entityCount = tile.getEntitySlotCount();
            int machineSlots = tile.getSlotCount();

            if (index < machineSlots) {
                if (!mergeItemStack(stack, machineSlots, inventorySlots.size(), true)) {
                    return null;
                }
            } else if (stack.getItem() instanceof ItemEntityCard) {
                if (!mergeItemStack(stack, 0, entityCount, false)) {
                    return null;
                }
            } else if (UpgradeRegistry.get(stack) != null) {
                if (!mergeUpgrades(stack)) {
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
