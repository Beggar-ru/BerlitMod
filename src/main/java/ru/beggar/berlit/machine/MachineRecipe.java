package ru.beggar.berlit.machine;

import net.minecraft.item.ItemStack;

public class MachineRecipe {

    private final ItemStack input;
    private final ItemStack output;
    private final int energy;

    public MachineRecipe(ItemStack input, ItemStack output, int energy) {
        this.input = input.copy();
        this.output = output.copy();
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
