package ru.beggar.berlit.ae2;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import fox.spiteful.avaritia.crafting.CompressorManager;

/** Creates an AE2 pattern for one neutron-compressor recipe. */
public class TileEntitySingularityEncoder extends TileEntity implements IInventory {

    public static final int INPUT = 0;
    public static final int OUTPUT = 1;
    public static final int SIZE = 2;

    private final ItemStack[] inventory = new ItemStack[SIZE];

    public void encode() {
        if (worldObj == null || worldObj.isRemote) return;
        if (inventory[OUTPUT] != null || inventory[INPUT] == null) return;

        int cost = CompressorManager.getCost(inventory[INPUT]);
        ItemStack result = CompressorManager.getOutput(inventory[INPUT]);
        if (cost <= 0 || result == null) return;

        inventory[OUTPUT] = ItemSingularityPattern.create(inventory[INPUT], cost, result);
        markDirty();
    }

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
        ItemStack result = inventory[slot];
        inventory[slot] = null;
        markDirty();
        return result;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        inventory[slot] = stack;
        markDirty();
    }

    @Override
    public String getInventoryName() {
        return "container.berlit.singularity_encoder";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
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
        return slot == INPUT && stack != null && CompressorManager.getOutput(stack) != null;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        inventory[INPUT] = nbt.hasKey("In") ? ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("In")) : null;
        inventory[OUTPUT] = nbt.hasKey("Out") ? ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("Out")) : null;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        if (inventory[INPUT] != null) {
            NBTTagCompound tag = new NBTTagCompound();
            inventory[INPUT].writeToNBT(tag);
            nbt.setTag("In", tag);
        }
        if (inventory[OUTPUT] != null) {
            NBTTagCompound tag = new NBTTagCompound();
            inventory[OUTPUT].writeToNBT(tag);
            nbt.setTag("Out", tag);
        }
    }
}
