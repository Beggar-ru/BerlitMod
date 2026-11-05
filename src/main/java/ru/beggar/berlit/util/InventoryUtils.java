package ru.beggar.berlit.util;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import net.minecraftforge.common.util.ForgeDirection;

/**
 * Small helpers shared by the Berlit machines that push their output into neighbouring inventories.
 */
public final class InventoryUtils {

    private InventoryUtils() {}

    /** Tries to insert the stack into the target inventory, returns the leftover (or null). */
    public static ItemStack insert(IInventory target, ItemStack stack) {
        ItemStack remaining = stack.copy();

        for (int i = 0; i < target.getSizeInventory() && remaining.stackSize > 0; i++) {
            ItemStack slot = target.getStackInSlot(i);
            if (slot == null) continue;
            if (slot.getItem() != remaining.getItem() || slot.getItemDamage() != remaining.getItemDamage()) continue;

            int limit = Math.min(target.getInventoryStackLimit(), slot.getMaxStackSize());
            int space = limit - slot.stackSize;
            if (space <= 0) continue;

            int moved = Math.min(space, remaining.stackSize);
            slot.stackSize += moved;
            remaining.stackSize -= moved;
            target.markDirty();
        }

        for (int i = 0; i < target.getSizeInventory() && remaining.stackSize > 0; i++) {
            if (target.getStackInSlot(i) != null) continue;
            if (!target.isItemValidForSlot(i, remaining)) continue;

            int limit = Math.min(target.getInventoryStackLimit(), remaining.getMaxStackSize());
            ItemStack copy = remaining.copy();
            copy.stackSize = Math.min(limit, remaining.stackSize);
            target.setInventorySlotContents(i, copy);
            remaining.stackSize -= copy.stackSize;
        }

        return remaining.stackSize > 0 ? remaining : null;
    }

    /** Pushes the stack into every adjacent inventory, returns the leftover (or null). */
    public static ItemStack insertIntoNeighbours(World world, TileEntity self, ItemStack stack) {
        ItemStack remaining = stack.copy();
        for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
            TileEntity tile = world.getTileEntity(
                self.xCoord + direction.offsetX,
                self.yCoord + direction.offsetY,
                self.zCoord + direction.offsetZ);
            if (tile instanceof IInventory && tile != self) {
                remaining = insert((IInventory) tile, remaining);
                if (remaining == null) return null;
            }
        }
        return remaining;
    }
}
