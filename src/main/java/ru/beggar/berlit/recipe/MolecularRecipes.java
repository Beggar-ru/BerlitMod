package ru.beggar.berlit.recipe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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

    public static Collection<MolecularRecipe> getRecipes() {
        return RECIPES.values();
    }

    public static boolean removeByInput(ItemStack input) {
        if (input == null) return false;
        return RECIPES.remove(key(input)) != null;
    }

    public static List<MolecularRecipe> removeByOutput(ItemStack output) {
        List<MolecularRecipe> removed = new ArrayList<MolecularRecipe>();
        if (output == null) return removed;

        Iterator<Map.Entry<String, MolecularRecipe>> iterator = RECIPES.entrySet()
            .iterator();
        while (iterator.hasNext()) {
            MolecularRecipe recipe = iterator.next()
                .getValue();
            if (recipe.getOutput()
                .getItem() == output.getItem()
                && recipe.getOutput()
                    .getItemDamage() == output.getItemDamage()) {
                removed.add(recipe);
                iterator.remove();
            }
        }
        return removed;
    }

    private static String key(ItemStack stack) {
        return Item.getIdFromItem(stack.getItem()) + ":" + stack.getItemDamage();
    }
}
