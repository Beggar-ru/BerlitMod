package ru.beggar.berlit.ae2;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerSingularityEncoder extends Container {

    private final TileEntitySingularityEncoder tile;

    public ContainerSingularityEncoder(InventoryPlayer playerInventory, TileEntitySingularityEncoder tile) {
        this.tile = tile;

        addSlotToContainer(new Slot(tile, TileEntitySingularityEncoder.INPUT, 44, 20));
        addSlotToContainer(new Slot(tile, TileEntitySingularityEncoder.OUTPUT, 116, 20) {

            @Override
            public boolean isItemValid(ItemStack stack) {
                return false;
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 102 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(playerInventory, col, 8 + col * 18, 158));
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
            if (index < 2) {
                if (!mergeItemStack(stack, 2, inventorySlots.size(), true)) return null;
            } else if (!mergeItemStack(stack, 0, 1, false)) return null;
            if (stack.stackSize == 0) slot.putStack(null);
            else slot.onSlotChanged();
        }
        return result;
    }
}
