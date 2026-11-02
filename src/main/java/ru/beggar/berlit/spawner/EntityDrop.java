package ru.beggar.berlit.spawner;

import net.minecraft.item.ItemStack;

public class EntityDrop {

    private final ItemStack stack;
    private final float chance;
    private final int min;
    private final int max;

    public EntityDrop(ItemStack stack, float chance, int min, int max) {
        this.stack = stack.copy();
        this.chance = chance;
        this.min = min;
        this.max = Math.max(min, max);
    }

    public ItemStack getStack() {
        return stack;
    }

    public float getChance() {
        return chance;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public boolean matches(ItemStack other) {
        return other != null && stack.getItem() == other.getItem() && stack.getItemDamage() == other.getItemDamage();
    }
}
