package ru.beggar.berlit.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * One item remembered by a replicator: the stack that was scanned and the amount of UU matter
 * needed for a single copy, in millibuckets.
 *
 * <p>
 * Replication always works in whole stacks, so the cost shown for an operation is
 * {@link #getMatterPerItem()} multiplied by {@link #getStackSize()}.
 */
public final class ReplicatorPattern {

    private final ItemStack stack;
    private int matterPerItem;

    public ReplicatorPattern(ItemStack stack, int matterPerItem) {
        if (stack == null) throw new IllegalArgumentException("stack must not be null");

        this.stack = stack.copy();
        this.stack.stackSize = 1;
        this.matterPerItem = Math.max(1, matterPerItem);
    }

    /** A copy of the remembered item with its stack size forced to one. */
    public ItemStack getStack() {
        return stack;
    }

    /** A copy ready to be placed into the output slot. */
    public ItemStack copyStack(int amount) {
        ItemStack copy = stack.copy();
        copy.stackSize = Math.max(1, Math.min(amount, copy.getMaxStackSize()));
        return copy;
    }

    public int getMatterPerItem() {
        return matterPerItem;
    }

    public void setMatterPerItem(int matterPerItem) {
        this.matterPerItem = Math.max(1, matterPerItem);
    }

    /** How many items one replication operation produces: a whole stack whenever the item allows it. */
    public int getStackSize() {
        return Math.min(64, stack.getMaxStackSize());
    }

    public int getMatterPerStack() {
        long amount = (long) getStackSize() * matterPerItem;
        return amount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) amount;
    }

    /** Item, damage and NBT have to match; the stack size is deliberately ignored. */
    public static boolean sameItem(ItemStack first, ItemStack second) {
        if (first == second) return true;
        if (first == null || second == null) return false;
        if (first.getItem() != second.getItem()) return false;
        if (first.getItemDamage() != second.getItemDamage()) return false;

        NBTTagCompound firstTag = first.getTagCompound();
        NBTTagCompound secondTag = second.getTagCompound();
        if (firstTag == null) return secondTag == null;
        return firstTag.equals(secondTag);
    }

    /** Stable identity of the item, used to notice that the scan slot changed. */
    public static String key(ItemStack stack) {
        if (stack == null) return null;
        NBTTagCompound tag = stack.getTagCompound();
        return net.minecraft.item.Item.getIdFromItem(stack.getItem()) + ":"
            + stack.getItemDamage()
            + ":"
            + (tag == null ? "" : tag.toString());
    }
}
