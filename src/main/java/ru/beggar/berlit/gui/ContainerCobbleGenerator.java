package ru.beggar.berlit.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import ru.beggar.berlit.tileentity.TileEntityCobbleGenerator;
import ru.beggar.berlit.upgrade.UpgradeHelper;

/**
 * Container for the cobblestone generator: one output slot plus the four upgrade slots, laid out with the shared
 * {@link MachineLayout} geometry so the screen matches the rest of the Berlit machines.
 */
public class ContainerCobbleGenerator extends Container {

    private static final int SLOT_SIZE = MachineLayout.SLOT;

    private final TileEntityCobbleGenerator tile;

    public ContainerCobbleGenerator(InventoryPlayer playerInventory, TileEntityCobbleGenerator tile) {
        this.tile = tile;

        addSlotToContainer(
            new Slot(
                tile,
                TileEntityCobbleGenerator.SLOT_OUTPUT,
                MachineLayout.OUTPUT_X + SLOT_SIZE,
                MachineLayout.OUTPUT_Y + SLOT_SIZE) {

                @Override
                public boolean isItemValid(ItemStack stack) {
                    return false;
                }
            });

        for (int i = 0; i < TileEntityCobbleGenerator.SLOT_UPGRADE_COUNT; i++) {
            addSlotToContainer(
                new Slot(
                    tile,
                    TileEntityCobbleGenerator.SLOT_UPGRADE_START + i,
                    MachineLayout.UPGRADE_X + i * SLOT_SIZE,
                    MachineLayout.UPGRADE_Y) {

                    @Override
                    public boolean isItemValid(ItemStack stack) {
                        return UpgradeHelper.isMachineUpgrade(stack);
                    }
                });
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(
                    new Slot(
                        playerInventory,
                        col + row * 9 + 9,
                        MachineLayout.PLAYER_X + col * SLOT_SIZE,
                        MachineLayout.PLAYER_TOP + row * SLOT_SIZE));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(
                new Slot(playerInventory, col, MachineLayout.PLAYER_X + col * SLOT_SIZE, MachineLayout.HOTBAR_Y));
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

            int machineSlots = TileEntityCobbleGenerator.SLOT_COUNT;
            if (index < machineSlots) {
                if (!mergeItemStack(stack, machineSlots, inventorySlots.size(), true)) return null;
            } else if (UpgradeHelper.isMachineUpgrade(stack)) {
                if (!mergeItemStack(stack, TileEntityCobbleGenerator.SLOT_UPGRADE_START, machineSlots, false)) {
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
