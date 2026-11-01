package ru.beggar.berlit.recipe;

import net.minecraft.item.ItemStack;

public class MolecularRecipe {

    private final ItemStack input;
    private final ItemStack output;
    private final int energy;

    public MolecularRecipe(ItemStack input, ItemStack output, int energy) {
        this.input = input;
        this.output = output;
        this.energy = energy;
    }

    public ItemStack getInput() {
        return input;
    }

    public ItemStack getOutput() {
        return output;
    }

    public int getEnergy() {
        return energy;
    }
}
