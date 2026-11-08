package ru.beggar.berlit.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;

import ru.beggar.berlit.farm.FarmRecipe;
import ru.beggar.berlit.farm.FarmRecipes;

/**
 * Molecular farm: one seed slot and six output slots. Seeds are never consumed; each harvest
 * multiplies the recipe output by the number of seeds in the input stack.
 */
public class TileEntityMolecularFarm extends TileEntity implements IInventory {

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT_START = 1;
    public static final int SLOT_OUTPUT_COUNT = 6;
    public static final int SLOT_COUNT = SLOT_OUTPUT_START + SLOT_OUTPUT_COUNT;

    public static final int TICKS_PER_HARVEST = 100;

    private final ItemStack[] inventory = new ItemStack[SLOT_COUNT];
    private int progress;

    @Override
    public void updateEntity() {
        super.updateEntity();
        if (worldObj == null || worldObj.isRemote) return;

        ItemStack seeds = inventory[SLOT_INPUT];
        FarmRecipe recipe = FarmRecipes.find(seeds);
        if (recipe == null || seeds == null || seeds.stackSize <= 0) {
            progress = 0;
            return;
        }

        if (!canFit(recipe, seeds.stackSize)) {
            progress = 0;
            return;
        }

        if (++progress >= TICKS_PER_HARVEST) {
            produce(recipe, seeds.stackSize);
            progress = 0;
        }
    }

    private boolean canFit(FarmRecipe recipe, int seedCount) {
        for (ItemStack output : recipe.getOutputsView()) {
            if (capacity(output) < output.stackSize * seedCount) return false;
        }
        return true;
    }

    private int capacity(ItemStack output) {
        int capacity = 0;
        for (int i = SLOT_OUTPUT_START; i < SLOT_COUNT; i++) {
            ItemStack slot = inventory[i];
            if (slot == null) {
                capacity += 64;
            } else if (sameItem(slot, output) && slot.stackSize < 64) {
                capacity += 64 - slot.stackSize;
            }
        }
        return capacity;
    }

    private void produce(FarmRecipe recipe, int seedCount) {
        for (ItemStack output : recipe.getOutputsView()) {
            int remaining = output.stackSize * seedCount;
            for (int i = SLOT_OUTPUT_START; i < SLOT_COUNT && remaining > 0; i++) {
                ItemStack slot = inventory[i];
                if (slot == null) {
                    ItemStack copy = output.copy();
                    copy.stackSize = Math.min(64, remaining);
                    inventory[i] = copy;
                    remaining -= copy.stackSize;
                } else if (sameItem(slot, output) && slot.stackSize < 64) {
                    int moved = Math.min(64 - slot.stackSize, remaining);
                    slot.stackSize += moved;
                    remaining -= moved;
                }
            }
        }
        markDirty();
    }

    private static boolean sameItem(ItemStack a, ItemStack b) {
        return a.getItem() == b.getItem() && a.getItemDamage() == b.getItemDamage()
            && ItemStack.areItemStackTagsEqual(a, b);
    }

    public int getProgress() {
        return progress;
    }

    public int getProgressScaled(int scale) {
        return Math.min(scale, progress * scale / TICKS_PER_HARVEST);
    }

    public int getSeedCount() {
        return inventory[SLOT_INPUT] == null ? 0 : inventory[SLOT_INPUT].stackSize;
    }

    @Override
    public int getSizeInventory() {
        return SLOT_COUNT;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return slot >= 0 && slot < SLOT_COUNT ? inventory[slot] : null;
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        if (slot < 0 || slot >= SLOT_COUNT || inventory[slot] == null) return null;

        ItemStack result;
        if (inventory[slot].stackSize <= amount) {
            result = inventory[slot];
            inventory[slot] = null;
        } else {
            result = inventory[slot].splitStack(amount);
            if (inventory[slot].stackSize <= 0) inventory[slot] = null;
        }
        if (slot == SLOT_INPUT) progress = 0;
        markDirty();
        return result;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        if (slot < 0 || slot >= SLOT_COUNT) return null;
        ItemStack stack = inventory[slot];
        inventory[slot] = null;
        return stack;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        if (slot < 0 || slot >= SLOT_COUNT) return;
        inventory[slot] = stack;
        if (stack != null && stack.stackSize > getInventoryStackLimit()) stack.stackSize = getInventoryStackLimit();
        if (slot == SLOT_INPUT) progress = 0;
        markDirty();
    }

    @Override
    public String getInventoryName() {
        return "tile.berlit.molecular_farm.name";
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
        if (slot != SLOT_INPUT || stack == null) return false;
        return FarmRecipes.find(stack) != null;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        progress = nbt.getInteger("Progress");
        NBTTagList list = nbt.getTagList("Items", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            int slot = tag.getByte("Slot") & 255;
            if (slot >= 0 && slot < SLOT_COUNT) inventory[slot] = ItemStack.loadItemStackFromNBT(tag);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("Progress", progress);
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (inventory[i] != null) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setByte("Slot", (byte) i);
                inventory[i].writeToNBT(tag);
                list.appendTag(tag);
            }
        }
        nbt.setTag("Items", list);
    }
}
