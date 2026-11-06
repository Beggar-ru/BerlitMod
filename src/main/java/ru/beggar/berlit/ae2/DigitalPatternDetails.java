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
import ru.beggar.berlit.machine.MachineType;

/**
 * AE2 pattern generated from one machine recipe. It has no encoded-pattern item in the world;
 * the machine creates these objects on the fly and hands them to the crafting grid.
 */
public class DigitalPatternDetails implements ICraftingPatternDetails {

    private final ItemStack pattern;
    private final MachineType machineType;
    private final ItemStack input;
    private final List<ItemStack> outputs;
    private final int energy;
    private int priority;

    public DigitalPatternDetails(ItemStack pattern) {
        this.pattern = pattern;
        NBTTagCompound tag = pattern == null ? null : pattern.getTagCompound();
        if (tag == null) {
            this.machineType = null;
            this.input = null;
            this.outputs = new ArrayList<ItemStack>();
            this.energy = 0;
            return;
        }

        this.machineType = MachineType.byId(tag.getString("machine"));
        this.input = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("in"));
        this.outputs = new ArrayList<ItemStack>();
        NBTTagList out = tag.getTagList("out", 10);
        for (int i = 0; i < out.tagCount(); i++) {
            ItemStack stack = ItemStack.loadItemStackFromNBT(out.getCompoundTagAt(i));
            if (stack != null) this.outputs.add(stack);
        }
        this.energy = tag.getInteger("energy");
    }

    public DigitalPatternDetails(MachineType machineType, ItemStack input, List<ItemStack> outputs, int energy) {
        this.machineType = machineType;
        this.input = input == null ? null : input.copy();
        this.outputs = ItemDigitalPattern.copyOutputs(outputs);
        this.energy = energy;
        this.pattern = ItemDigitalPattern.create(machineType, this.input, this.outputs, energy);
    }

    public boolean isValid() {
        return pattern != null && machineType != null && input != null && input.stackSize > 0 && !outputs.isEmpty();
    }

    public MachineType getMachineType() {
        return machineType;
    }

    public ItemStack getInput() {
        return input == null ? null : input.copy();
    }

    public List<ItemStack> getItemOutputs() {
        return ItemDigitalPattern.copyOutputs(outputs);
    }

    public int getEnergy() {
        return energy;
    }

    @Override
    public ItemStack getPattern() {
        return pattern;
    }

    @Override
    public boolean isValidItemForSlot(int slot, ItemStack stack, World world) {
        if (slot != 0 || input == null || stack == null) return false;
        if (input.getItem() != stack.getItem() || input.getItemDamage() != stack.getItemDamage()) return false;

        NBTTagCompound expected = input.getTagCompound();
        NBTTagCompound actual = stack.getTagCompound();
        if (expected == null) return actual == null;
        return expected.equals(actual);
    }

    @Override
    public boolean isCraftable() {
        return false;
    }

    @Override
    public IAEItemStack[] getInputs() {
        if (input == null) return new IAEItemStack[0];
        return new IAEItemStack[] { AEApi.instance()
            .storage()
            .createItemStack(input.copy()) };
    }

    @Override
    public IAEItemStack[] getCondensedInputs() {
        return getInputs();
    }

    @Override
    public IAEItemStack[] getCondensedOutputs() {
        return getOutputs();
    }

    @Override
    public IAEItemStack[] getOutputs() {
        List<IAEItemStack> result = new ArrayList<IAEItemStack>(outputs.size());
        for (ItemStack output : outputs) {
            if (output == null) continue;
            result.add(
                AEApi.instance()
                    .storage()
                    .createItemStack(output.copy()));
        }
        return result.toArray(new IAEItemStack[0]);
    }

    @Override
    public boolean canSubstitute() {
        return false;
    }

    @Override
    public ItemStack getOutput(InventoryCrafting crafting, World world) {
        return outputs.isEmpty() ? null
            : outputs.get(0)
                .copy();
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public String toString() {
        return "DigitalPattern[" + (machineType == null ? "?" : machineType.getId())
            + " "
            + (input == null ? "?" : input.getDisplayName() + " x" + input.stackSize)
            + " -> "
            + outputs
            + "]";
    }
}
