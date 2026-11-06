package ru.beggar.berlit.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import ru.beggar.berlit.tileentity.TileEntityReplicator;
import ru.beggar.berlit.upgrade.UpgradeHelper;
import ru.beggar.berlit.util.ReplicatorFluid;

public class ContainerReplicator extends Container {

    private static final int SLOT_SIZE = MachineLayout.SLOT;

    private final TileEntityReplicator tile;

    public ContainerReplicator(InventoryPlayer playerInventory, TileEntityReplicator tile) {
        this.tile = tile;

        addSlotToContainer(
            new Slot(tile, TileEntityReplicator.SLOT_SCAN, ReplicatorLayout.SCAN_X, ReplicatorLayout.SCAN_Y));

        addSlotToContainer(
            new Slot(tile, TileEntityReplicator.SLOT_OUTPUT, ReplicatorLayout.OUTPUT_X, ReplicatorLayout.OUTPUT_Y) {

                @Override
                public boolean isItemValid(ItemStack stack) {
                    return false;
                }
            });

        addSlotToContainer(
            new Slot(tile, TileEntityReplicator.SLOT_FLUID_INPUT, ReplicatorLayout.FLUID_X, ReplicatorLayout.FLUID_Y) {

                @Override
                public boolean isItemValid(ItemStack stack) {
                    return ReplicatorFluid.isUuMatterContainer(stack);
                }
            });

        addSlotToContainer(
            new Slot(tile, TileEntityReplicator.SLOT_EMPTY_OUTPUT, ReplicatorLayout.EMPTY_X, ReplicatorLayout.EMPTY_Y) {

                @Override
                public boolean isItemValid(ItemStack stack) {
                    return false;
                }
            });

        for (int i = 0; i < TileEntityReplicator.SLOT_UPGRADE_COUNT; i++) {
            addSlotToContainer(
                new Slot(
                    tile,
                    TileEntityReplicator.SLOT_UPGRADE_START + i,
                    MachineLayout.UPGRADE_X + i * SLOT_SIZE,
                    MachineLayout.UPGRADE_Y) {

                    @Override
                    public boolean isItemValid(ItemStack stack) {
                        return UpgradeHelper.isMachineUpgrade(stack);
                    }
                });
        }

        bindPlayerInventory(playerInventory);
    }

    private void bindPlayerInventory(InventoryPlayer inventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(
                    new Slot(
                        inventory,
                        col + row * 9 + 9,
                        MachineLayout.PLAYER_X + col * SLOT_SIZE,
                        MachineLayout.PLAYER_TOP + row * SLOT_SIZE));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(
                new Slot(inventory, col, MachineLayout.PLAYER_X + col * SLOT_SIZE, MachineLayout.HOTBAR_Y));
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

            int machineSlots = TileEntityReplicator.SLOT_COUNT;
            if (index < machineSlots) {
                if (!mergeItemStack(stack, machineSlots, inventorySlots.size(), true)) {
                    return null;
                }
            } else if (UpgradeHelper.isMachineUpgrade(stack)) {
                if (!mergeItemStack(stack, TileEntityReplicator.SLOT_UPGRADE_START, machineSlots, false)) {
                    return null;
                }
            } else if (ReplicatorFluid.isUuMatterContainer(stack)) {
                if (!mergeItemStack(
                    stack,
                    TileEntityReplicator.SLOT_FLUID_INPUT,
                    TileEntityReplicator.SLOT_FLUID_INPUT + 1,
                    false)) {
                    return null;
                }
            } else {
                if (!mergeItemStack(stack, TileEntityReplicator.SLOT_SCAN, TileEntityReplicator.SLOT_SCAN + 1, false)) {
                    return null;
                }
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
