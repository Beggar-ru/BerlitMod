package ru.beggar.berlit.ae2;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import appeng.api.AEApi;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;

public class SingularityPatternDetails implements ICraftingPatternDetails {

    private final ItemStack pattern;
    private final IAEItemStack input;
    private final IAEItemStack output;
    private final int cost;
    private int priority;

    public SingularityPatternDetails(ItemStack pattern) {
        this.pattern = pattern;
        NBTTagCompound tag = pattern.getTagCompound();
        this.cost = Math.max(1, tag.getInteger("cost"));

        ItemStack stack = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("input"));
        IAEItemStack ae = AEApi.instance()
            .storage()
            .createItemStack(stack);
        if (ae != null) ae.setStackSize(cost);
        this.input = ae;

        ItemStack out = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("out"));
        this.output = out == null ? null
            : AEApi.instance()
                .storage()
                .createItemStack(out);
    }

    public int getCost() {
        return cost;
    }

    public ItemStack getInputStack() {
        return input == null ? null : input.getItemStack();
    }

    @Override
    public ItemStack getPattern() {
        return pattern;
    }

    @Override
    public boolean isValidItemForSlot(int slot, ItemStack stack, World world) {
        return slot == 0 && input != null
            && stack != null
            && input.isSameType(
                AEApi.instance()
                    .storage()
                    .createItemStack(stack));
    }

    @Override
    public boolean isCraftable() {
        return false;
    }

    @Override
    public IAEItemStack[] getInputs() {
        return input == null ? new IAEItemStack[0] : new IAEItemStack[] { input };
    }

    @Override
    public IAEItemStack[] getCondensedInputs() {
        return getInputs();
    }

    @Override
    public IAEItemStack[] getCondensedOutputs() {
        return output == null ? new IAEItemStack[0] : new IAEItemStack[] { output };
    }

    @Override
    public IAEItemStack[] getOutputs() {
        return getCondensedOutputs();
    }

    @Override
    public boolean canSubstitute() {
        return false;
    }

    @Override
    public ItemStack getOutput(InventoryCrafting crafting, World world) {
        return output == null ? null : output.getItemStack();
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public void setPriority(int priority) {
        this.priority = priority;
    }
}
