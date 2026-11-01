package ru.beggar.berlit.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import ru.beggar.berlit.tileentity.TileEntityMolecularTransformer;

public class ContainerMolecularTransformer extends Container {

    private static final int SLOT_SIZE = 18;
    private static final int INPUT_X = 26;
    private static final int INPUT_Y = 17;
    private static final int OUTPUT_X = 98;
    private static final int OUTPUT_Y = 17;

    private final TileEntityMolecularTransformer tile;

    public ContainerMolecularTransformer(InventoryPlayer playerInventory, TileEntityMolecularTransformer tile) {
        this.tile = tile;

        // Input slots: 6 (3 x 2).
        for (int i = 0; i < TileEntityMolecularTransformer.SLOT_INPUT_COUNT; i++) {
            addSlotToContainer(new Slot(tile, i, INPUT_X + (i % 3) * SLOT_SIZE, INPUT_Y + (i / 3) * SLOT_SIZE));
        }

        // Output slots: 6 (3 x 2), not insertable by hand.
        for (int i = 0; i < TileEntityMolecularTransformer.SLOT_OUTPUT_COUNT; i++) {
            addSlotToContainer(
                new Slot(
                    tile,
                    TileEntityMolecularTransformer.SLOT_OUTPUT_START + i,
                    OUTPUT_X + (i % 3) * SLOT_SIZE,
                    OUTPUT_Y + (i / 3) * SLOT_SIZE) {

                    @Override
                    public boolean isItemValid(ItemStack stack) {
                        return false;
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

            int machineSlots = TileEntityMolecularTransformer.SLOT_COUNT;
            if (index < machineSlots) {
                if (!mergeItemStack(stack, machineSlots, inventorySlots.size(), true)) {
                    return null;
                }
            } else if (!mergeItemStack(stack, 0, TileEntityMolecularTransformer.SLOT_INPUT_COUNT, false)) {
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
