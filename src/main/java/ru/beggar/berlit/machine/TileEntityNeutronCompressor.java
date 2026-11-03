package ru.beggar.berlit.machine;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import fox.spiteful.avaritia.crafting.CompressorManager;
import ru.beggar.berlit.Berlit;
import ru.beggar.berlit.ae2.SingularityPatternDetails;

/** Compresses items into an Avaritia singularity, one item at a time. */
public class TileEntityNeutronCompressor extends TileEntity implements IInventory {

    public static final int INPUT = 0;
    public static final int OUTPUT = 1;
    public static final int PATTERN = 2;
    public static final int SIZE = 3;

    private final ItemStack[] inventory = new ItemStack[SIZE];
    private int progress;
    private int cost;
    private ItemStack pendingOutput;

    private SingularityPatternDetails getPattern() {
        ItemStack stack = inventory[PATTERN];
        if (stack == null || !(stack.getItem() instanceof ICraftingPatternItem)) return null;
        ICraftingPatternDetails details = ((ICraftingPatternItem) stack.getItem()).getPatternForItem(stack, worldObj);
        return details instanceof SingularityPatternDetails ? (SingularityPatternDetails) details : null;
    }

    @Override
    public void updateEntity() {
        if (worldObj == null || worldObj.isRemote) return;
        if (inventory[OUTPUT] != null) return;

        SingularityPatternDetails pattern = getPattern();
        ItemStack input = inventory[INPUT];
        if (input == null) {
            progress = 0;
            cost = 0;
            pendingOutput = null;
            return;
        }

        if (pattern != null) {
            ItemStack target = pattern.getInputStack();
            if (target == null || !input.isItemEqual(target)) return;
            if (cost <= 0) {
                cost = pattern.getCost();
                pendingOutput = pattern.getOutput(null, worldObj);
            }
        } else if (cost <= 0) {
            ItemStack result = CompressorManager.getOutput(input);
            if (result == null) return;
            cost = Math.max(1, CompressorManager.getCost(input));
            pendingOutput = result;
        }

        input.stackSize--;
        progress++;
        if (input.stackSize <= 0) inventory[INPUT] = null;

        if (progress >= cost) {
            if (pendingOutput != null) inventory[OUTPUT] = pendingOutput;
            pendingOutput = null;
            progress = 0;
            cost = 0;
        }
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
        return "container.berlit.neutron_compressor";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 4096;
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
        if (slot == PATTERN) return stack != null && stack.getItem() == Berlit.singularityPattern;
        if (slot != INPUT || stack == null) return false;

        SingularityPatternDetails pattern = getPattern();
        if (pattern != null) {
            ItemStack target = pattern.getInputStack();
            return target != null && stack.isItemEqual(target);
        }
        return CompressorManager.getOutput(stack) != null;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        progress = nbt.getInteger("Progress");
        cost = nbt.getInteger("Cost");
        for (int i = 0; i < SIZE; i++) {
            inventory[i] = nbt.hasKey("Item" + i) ? ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("Item" + i))
                : null;
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("Progress", progress);
        nbt.setInteger("Cost", cost);
        for (int i = 0; i < SIZE; i++) {
            if (inventory[i] != null) {
                NBTTagCompound tag = new NBTTagCompound();
                inventory[i].writeToNBT(tag);
                nbt.setTag("Item" + i, tag);
            }
        }
    }
}
