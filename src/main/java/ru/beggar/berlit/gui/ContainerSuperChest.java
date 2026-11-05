package ru.beggar.berlit.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import ru.beggar.berlit.tileentity.TileEntitySuperChest;

public class ContainerSuperChest extends Container {

    private static final int SLOT = 18;

    private final TileEntitySuperChest tile;

    public ContainerSuperChest(InventoryPlayer playerInventory, TileEntitySuperChest tile) {
        this.tile = tile;

        for (int row = 0; row < TileEntitySuperChest.HEIGHT; row++) {
            for (int col = 0; col < TileEntitySuperChest.WIDTH; col++) {
                addSlotToContainer(
                    new Slot(tile, col + row * TileEntitySuperChest.WIDTH, 8 + col * SLOT, 18 + row * SLOT) {

                        @Override
                        public int getSlotStackLimit() {
                            return TileEntitySuperChest.LIMIT;
                        }
                    });
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(playerInventory, col + row * 9 + 9, 171 + col * SLOT, 216 + row * SLOT));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(playerInventory, col, 171 + col * SLOT, 272));
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

            int chestSlots = TileEntitySuperChest.SIZE;
            if (index < chestSlots) {
                if (!mergeItemStack(stack, chestSlots, inventorySlots.size(), true)) return null;
            } else {
                if (!mergeItemStack(stack, 0, chestSlots, false)) return null;
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
