package ru.beggar.berlit.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;

/** 27x9 storage, up to 1024 items per slot (custom NBT, since ItemStack writes Count as a byte). */
public class TileEntitySuperChest extends TileEntity implements IInventory {

    public static final int WIDTH = 27;
    public static final int HEIGHT = 9;
    public static final int SIZE = WIDTH * HEIGHT;
    public static final int LIMIT = 1024;

    private final ItemStack[] inventory = new ItemStack[SIZE];

    @Override
    public int getSizeInventory() {
        return SIZE;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return inventory[slot];
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        if (inventory[slot] == null) return null;

        ItemStack result;
        if (inventory[slot].stackSize <= amount) {
            result = inventory[slot];
            inventory[slot] = null;
        } else {
            result = inventory[slot].splitStack(amount);
            if (inventory[slot].stackSize <= 0) inventory[slot] = null;
        }
        markDirty();
        return result;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        ItemStack stack = inventory[slot];
        inventory[slot] = null;
        return stack;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        inventory[slot] = stack;
        if (stack != null && stack.stackSize > LIMIT) stack.stackSize = LIMIT;
        markDirty();
    }

    @Override
    public String getInventoryName() {
        return "container.berlit.super_chest";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return LIMIT;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this
            && player.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 64.0D;
    }

    @Override
    public void openInventory() {}

    @Override
    public void closeInventory() {}

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return true;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        NBTTagList list = nbt.getTagList("Items", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            int slot = tag.getByte("Slot") & 255;
            if (slot < 0 || slot >= SIZE) continue;

            ItemStack stack = ItemStack.loadItemStackFromNBT(tag);
            if (stack == null) continue;
            if (tag.hasKey("Count")) stack.stackSize = tag.getInteger("Count");
            inventory[slot] = stack;
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < SIZE; i++) {
            if (inventory[i] == null) continue;

            NBTTagCompound tag = new NBTTagCompound();
            tag.setByte("Slot", (byte) i);
            inventory[i].writeToNBT(tag);
            tag.setInteger("Count", inventory[i].stackSize);
            list.appendTag(tag);
        }
        nbt.setTag("Items", list);
    }
}
