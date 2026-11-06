package ru.beggar.berlit.machine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.item.ItemStack;

public class MachineRecipe {

    private final List<ItemStack> inputs;
    private final List<ItemStack> outputs;
    private final int energy;

    public MachineRecipe(ItemStack input, ItemStack output, int energy) {
        this(Collections.singletonList(input), Collections.singletonList(output), energy);
    }

    public MachineRecipe(List<ItemStack> inputs, List<ItemStack> outputs, int energy) {
        this.inputs = copyStacks(inputs);
        this.outputs = copyStacks(outputs);
        this.energy = energy;
    }

    private static List<ItemStack> copyStacks(List<ItemStack> stacks) {
        List<ItemStack> result = new ArrayList<ItemStack>();
        if (stacks == null) return result;

        for (ItemStack stack : stacks) {
            if (stack != null && stack.stackSize > 0) {
                result.add(stack.copy());
            }
        }
        return Collections.unmodifiableList(result);
    }

    /** The first input, kept for the existing single-input machines and NEI page. */
    public ItemStack getInput() {
        return inputs.isEmpty() ? null : inputs.get(0);
    }

    /** The first output, kept for the existing single-output machines and NEI page. */
    public ItemStack getOutput() {
        return outputs.isEmpty() ? null : outputs.get(0);
    }

    /** All concrete input variants of the IC2 recipe (ore dictionary entries, etc.). */
    public List<ItemStack> getInputs() {
        return inputs;
    }

    /** All outputs of the IC2 recipe, not just the first one. */
    public List<ItemStack> getOutputs() {
        return outputs;
    }

    public int getEnergy() {
        return energy;
    }
}
