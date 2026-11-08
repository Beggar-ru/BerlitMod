package ru.beggar.berlit.farm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/** Registry of molecular-farm recipes. Outputs are per one seed; the tile multiplies by stack size. */
public final class FarmRecipes {

    private static final Map<String, FarmRecipe> RECIPES = new HashMap<String, FarmRecipe>();
    private static boolean initialised;

    private FarmRecipes() {}

    public static void init() {
        if (initialised) return;
        initialised = true;

        register(new ItemStack(Items.wheat_seeds), new ItemStack(Items.wheat), new ItemStack(Items.wheat_seeds));
        register(new ItemStack(Items.wheat), new ItemStack(Items.wheat));
        register(new ItemStack(Items.carrot), new ItemStack(Items.carrot));
        register(new ItemStack(Items.potato), new ItemStack(Items.potato));
        register(new ItemStack(Items.pumpkin_seeds), new ItemStack(Blocks.pumpkin), new ItemStack(Items.pumpkin_seeds));
        register(new ItemStack(Items.melon_seeds), new ItemStack(Blocks.melon_block), new ItemStack(Items.melon_seeds));
        register(new ItemStack(Items.nether_wart), new ItemStack(Items.nether_wart));
        register(new ItemStack(Items.dye, 1, 3), new ItemStack(Items.dye, 3, 3));
        register(new ItemStack(Items.reeds), new ItemStack(Items.reeds));
    }

    public static void register(ItemStack seed, ItemStack... outputs) {
        List<ItemStack> list = new ArrayList<ItemStack>();
        for (ItemStack output : outputs) {
            if (output != null) list.add(output.copy());
        }
        register(seed, list);
    }

    public static void register(ItemStack seed, List<ItemStack> outputs) {
        if (seed == null || outputs == null || outputs.isEmpty()) return;
        RECIPES.put(key(seed), new FarmRecipe(seed, outputs));
    }

    public static FarmRecipe find(ItemStack seed) {
        if (seed == null) return null;
        return RECIPES.get(key(seed));
    }

    public static Collection<FarmRecipe> getRecipes() {
        return RECIPES.values();
    }

    public static boolean remove(ItemStack seed) {
        return seed != null && RECIPES.remove(key(seed)) != null;
    }

    public static List<FarmRecipe> removeByOutput(ItemStack output) {
        List<FarmRecipe> removed = new ArrayList<FarmRecipe>();
        if (output == null) return removed;

        java.util.Iterator<Map.Entry<String, FarmRecipe>> iterator = RECIPES.entrySet()
            .iterator();
        while (iterator.hasNext()) {
            FarmRecipe recipe = iterator.next()
                .getValue();
            for (ItemStack candidate : recipe.getOutputsView()) {
                if (candidate.getItem() == output.getItem() && candidate.getItemDamage() == output.getItemDamage()) {
                    removed.add(recipe);
                    iterator.remove();
                    break;
                }
            }
        }
        return removed;
    }

    private static String key(ItemStack stack) {
        return Item.getIdFromItem(stack.getItem()) + ":" + stack.getItemDamage();
    }
}
