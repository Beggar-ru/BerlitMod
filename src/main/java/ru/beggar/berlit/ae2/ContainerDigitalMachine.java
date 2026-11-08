package ru.beggar.berlit.ae2;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import ru.beggar.berlit.gui.MachineLayout;
import ru.beggar.berlit.upgrade.UpgradeHelper;

public class ContainerDigitalMachine extends Container {

    private final DigitalMachineView tile;

    public ContainerDigitalMachine(InventoryPlayer playerInventory, DigitalMachineView tile) {
        this.tile = tile;

        for (int i = 0; i < TileEntityDigitalMachine.UPGRADE_SLOTS; i++) {
            addSlotToContainer(
                new Slot(tile, i, MachineLayout.UPGRADE_X + i * MachineLayout.SLOT, MachineLayout.UPGRADE_Y) {

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
                        MachineLayout.PLAYER_X + col * MachineLayout.SLOT,
                        MachineLayout.PLAYER_TOP + row * MachineLayout.SLOT));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(
                new Slot(inventory, col, MachineLayout.PLAYER_X + col * MachineLayout.SLOT, MachineLayout.HOTBAR_Y));
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

            int machineSlots = TileEntityDigitalMachine.UPGRADE_SLOTS;
            if (index < machineSlots) {
                if (!mergeItemStack(stack, machineSlots, inventorySlots.size(), true)) {
                    return null;
                }
            } else if (UpgradeHelper.isMachineUpgrade(stack)) {
                if (!mergeItemStack(stack, 0, machineSlots, false)) {
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
