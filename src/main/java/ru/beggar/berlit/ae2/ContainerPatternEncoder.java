package ru.beggar.berlit.ae2;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerPatternEncoder extends Container {

    private final TileEntityPatternEncoder tile;

    public ContainerPatternEncoder(InventoryPlayer playerInventory, TileEntityPatternEncoder tile) {
        this.tile = tile;

        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(tile, col + row * 9, 8 + col * 18, 24 + row * 18));
            }
        }

        addSlotToContainer(new Slot(tile, TileEntityPatternEncoder.OUTPUT, 190, 96) {

            @Override
            public boolean isItemValid(ItemStack stack) {
                return false;
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(playerInventory, col + row * 9 + 9, 30 + col * 18, 222 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(playerInventory, col, 30 + col * 18, 278));
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

            if (index == TileEntityPatternEncoder.OUTPUT) {
                if (!mergeItemStack(stack, 82, inventorySlots.size(), true)) return null;
            } else if (index < 81) {
                if (!mergeItemStack(stack, 82, inventorySlots.size(), true)) return null;
            } else {
                if (!mergeItemStack(stack, 0, 81, false)) return null;
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
