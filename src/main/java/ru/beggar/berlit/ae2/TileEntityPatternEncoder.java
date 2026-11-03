package ru.beggar.berlit.ae2;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;

import appeng.container.ContainerNull;
import fox.spiteful.avaritia.crafting.ExtremeCraftingManager;

/** Encodes a 9x9 Avaritia recipe into an AE2-compatible pattern item. */
public class TileEntityPatternEncoder extends TileEntity implements IInventory {

    public static final int GRID = 9;
    public static final int GRID_SLOTS = GRID * GRID;
    public static final int OUTPUT = GRID_SLOTS;
    public static final int SIZE = GRID_SLOTS + 1;

    private final ItemStack[] inventory = new ItemStack[SIZE];

    public void encode() {
        if (worldObj == null || worldObj.isRemote) return;
        if (inventory[OUTPUT] != null) return;

        InventoryCrafting grid = new InventoryCrafting(new ContainerNull(), GRID, GRID);
        for (int i = 0; i < GRID_SLOTS; i++) {
            grid.setInventorySlotContents(i, inventory[i]);
        }

        if (uniqueInputs() > 9) return;

        ItemStack result = ExtremeCraftingManager.getInstance()
            .findMatchingRecipe(grid, worldObj);
        if (result == null) return;

        inventory[OUTPUT] = ItemAvaritiaPattern.create(inventory, result);
        markDirty();
    }

    private int uniqueInputs() {
        java.util.Set<String> types = new java.util.HashSet<String>();
        for (int i = 0; i < GRID_SLOTS; i++) {
            ItemStack stack = inventory[i];
            if (stack == null) continue;
            types.add(
                stack.getItem()
                    .getUnlocalizedName() + "|"
                    + stack.getItemDamage());
        }
        return types.size();
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
        return "container.berlit.pattern_encoder";
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
        return slot < GRID_SLOTS;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        NBTTagList list = nbt.getTagList("Items", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            int slot = tag.getByte("Slot") & 255;
            if (slot < SIZE) inventory[slot] = ItemStack.loadItemStackFromNBT(tag);
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
            list.appendTag(tag);
        }
        nbt.setTag("Items", list);
    }
}
