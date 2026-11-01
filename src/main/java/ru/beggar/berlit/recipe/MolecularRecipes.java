package ru.beggar.berlit.recipe;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public final class MolecularRecipes {

    private static final Map<String, MolecularRecipe> RECIPES = new HashMap<String, MolecularRecipe>();

    private MolecularRecipes() {}

    public static void init() {
        // Test recipe: emerald -> diamond.
        register(new ItemStack(Items.emerald), new ItemStack(Items.diamond), 2000);
    }

    public static void register(ItemStack input, ItemStack output, int energy) {
        RECIPES.put(key(input), new MolecularRecipe(input.copy(), output.copy(), energy));
    }

    public static MolecularRecipe find(ItemStack stack) {
        if (stack == null) return null;
        return RECIPES.get(key(stack));
    }

    private static String key(ItemStack stack) {
        return Item.getIdFromItem(stack.getItem()) + ":" + stack.getItemDamage();
    }
}
