package ru.beggar.berlit.farm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.item.ItemStack;

/** One molecular-farm recipe: one seed item produces its listed crops per seed in the input stack. */
public final class FarmRecipe {

    private final ItemStack input;
    private final List<ItemStack> outputs;

    public FarmRecipe(ItemStack input, List<ItemStack> outputs) {
        this.input = input.copy();
        this.outputs = new ArrayList<ItemStack>(outputs.size());
        for (ItemStack output : outputs) {
            if (output != null) this.outputs.add(output.copy());
        }
    }

    public ItemStack getInput() {
        return input.copy();
    }

    public List<ItemStack> getOutputs() {
        List<ItemStack> copy = new ArrayList<ItemStack>(outputs.size());
        for (ItemStack output : outputs) copy.add(output.copy());
        return copy;
    }

    public List<ItemStack> getOutputsView() {
        return Collections.unmodifiableList(outputs);
    }
}
