package ru.beggar.berlit.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import ru.beggar.berlit.tileentity.TileEntityMatterGenerator;

/** Input and output slot plus the player inventory; no upgrade slots - the generator is a plain converter. */
public class ContainerMatterGenerator extends Container {

    private final TileEntityMatterGenerator tile;

    public ContainerMatterGenerator(InventoryPlayer playerInventory, TileEntityMatterGenerator tile) {
        this.tile = tile;

        addSlotToContainer(
            new Slot(tile, TileEntityMatterGenerator.SLOT_INPUT, MatterLayout.INPUT_X, MatterLayout.SLOT_Y));
        addSlotToContainer(
            new Slot(tile, TileEntityMatterGenerator.SLOT_OUTPUT, MatterLayout.OUTPUT_X, MatterLayout.SLOT_Y));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(
                    new Slot(
                        playerInventory,
                        col + row * 9 + 9,
                        MatterLayout.PLAYER_X + col * MatterLayout.SLOT,
                        MatterLayout.PLAYER_TOP + row * MatterLayout.SLOT));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(
                new Slot(playerInventory, col, MatterLayout.PLAYER_X + col * MatterLayout.SLOT, MatterLayout.HOTBAR_Y));
        }
    }

    public TileEntityMatterGenerator getTile() {
        return tile;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return tile.isUseableByPlayer(player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack copy = null;
        Slot slot = (Slot) inventorySlots.get(index);
        if (slot == null || !slot.getHasStack()) return null;

        ItemStack stack = slot.getStack();
        copy = stack.copy();

        int machineSlots = 2;
        if (index < machineSlots) {
            if (!mergeItemStack(stack, machineSlots, inventorySlots.size(), true)) return null;
        } else if (!mergeItemStack(
            stack,
            TileEntityMatterGenerator.SLOT_INPUT,
            TileEntityMatterGenerator.SLOT_INPUT + 1,
            false)) {
                return null;
            }

        if (stack.stackSize == 0) {
            slot.putStack(null);
        } else {
            slot.onSlotChanged();
        }
        return copy;
    }
}
