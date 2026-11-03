package ru.beggar.berlit.machine;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerNeutronCompressor extends Container {

    private final TileEntityNeutronCompressor tile;

    public ContainerNeutronCompressor(InventoryPlayer playerInventory, TileEntityNeutronCompressor tile) {
        this.tile = tile;
        addSlotToContainer(new Slot(tile, TileEntityNeutronCompressor.INPUT, 44, 20) {

            @Override
            public int getSlotStackLimit() {
                return 4096;
            }
        });
        addSlotToContainer(new Slot(tile, TileEntityNeutronCompressor.PATTERN, 80, 20));
        addSlotToContainer(new Slot(tile, TileEntityNeutronCompressor.OUTPUT, 116, 20) {

            @Override
            public boolean isItemValid(ItemStack stack) {
                return false;
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(playerInventory, col, 8 + col * 18, 142));
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
