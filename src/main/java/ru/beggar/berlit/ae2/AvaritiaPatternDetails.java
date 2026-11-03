package ru.beggar.berlit.ae2;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

import appeng.api.AEApi;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;

public class AvaritiaPatternDetails implements ICraftingPatternDetails {

    private final ItemStack pattern;
    private final IAEItemStack[] inputs = new IAEItemStack[ItemAvaritiaPattern.SLOTS];
    private final IAEItemStack output;
    private int priority;

    public AvaritiaPatternDetails(ItemStack pattern) {
        this.pattern = pattern;

        NBTTagCompound tag = pattern.getTagCompound();
        NBTTagList in = tag.getTagList("in", 10);
        for (int i = 0; i < in.tagCount() && i < inputs.length; i++) {
            NBTTagCompound entry = in.getCompoundTagAt(i);
            if (entry.hasNoTags()) continue;
            ItemStack stack = ItemStack.loadItemStackFromNBT(entry);
            if (stack != null) {
                inputs[i] = AEApi.instance()
                    .storage()
                    .createItemStack(stack);
            }
        }

        NBTTagList out = tag.getTagList("out", 10);
        this.output = out.tagCount() > 0 ? AEApi.instance()
            .storage()
            .createItemStack(ItemStack.loadItemStackFromNBT(out.getCompoundTagAt(0))) : null;
    }

    public IAEItemStack[] getGrid() {
        return inputs;
    }

    @Override
    public ItemStack getPattern() {
        return pattern;
    }

    @Override
    public boolean isValidItemForSlot(int slot, ItemStack stack, World world) {
        if (slot < 0 || slot >= inputs.length) return false;
        IAEItemStack input = inputs[slot];
        if (input == null || stack == null) return false;
        return input.getItem() == stack.getItem() && input.getItemDamage() == stack.getItemDamage();
    }

    @Override
    public boolean isCraftable() {
        return false;
    }

    @Override
    public IAEItemStack[] getInputs() {
        // AE2's crafting CPU only has 9 slots; it gets the condensed totals and the
        // assembling machine reconstructs the real 9x9 grid from getGrid().
        return condensed();
    }

    @Override
    public IAEItemStack[] getCondensedInputs() {
        return condensed();
    }

    private IAEItemStack[] condensed() {
        List<IAEItemStack> result = new ArrayList<IAEItemStack>();
        for (IAEItemStack input : inputs) {
            if (input == null) continue;
            IAEItemStack found = null;
            for (IAEItemStack existing : result) {
                if (existing.isSameType(input)) {
                    found = existing;
                    break;
                }
            }
            if (found != null) {
                found.setStackSize(found.getStackSize() + input.getStackSize());
            } else {
                result.add(input.copy());
            }
        }
        return result.toArray(new IAEItemStack[0]);
    }

    @Override
    public IAEItemStack[] getCondensedOutputs() {
        return output == null ? new IAEItemStack[0] : new IAEItemStack[] { output.copy() };
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
