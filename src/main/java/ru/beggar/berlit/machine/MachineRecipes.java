package ru.beggar.berlit.machine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraftforge.oredict.OreDictionary;

import ic2.api.recipe.IMachineRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.RecipeOutput;
import ic2.api.recipe.Recipes;

public final class MachineRecipes {

    public static final int MODE_EXTRUDING = 0;
    public static final int MODE_ROLLING = 1;

    private static final Map<MachineType, Map<String, MachineRecipe>> RECIPES = new EnumMap<MachineType, Map<String, MachineRecipe>>(
        MachineType.class);

    private MachineRecipes() {}

    public static void init() {
        // Real recipes come from the IC2 managers / FurnaceRecipes; CraftTweaker adds overrides.
        // Deliberately no placeholder recipes: they used to shadow the real imported recipe for
        // the same input (for example gold ingot -> gold plate vs. the old gold block stub).
    }

    public static void add(MachineType type, int mode, ItemStack input, ItemStack output, int energy) {
        Map<String, MachineRecipe> map = RECIPES.get(type);
        if (map == null) {
            map = new HashMap<String, MachineRecipe>();
            RECIPES.put(type, map);
        }
        map.put(key(mode, input), new MachineRecipe(input, output, energy));
    }

    /** Looks up a custom recipe first, then the matching IC2/vanilla recipe. */
    public static MachineRecipe find(MachineType type, int mode, ItemStack stack) {
        if (stack == null) return null;

        Map<String, MachineRecipe> map = RECIPES.get(type);
        if (map != null) {
            MachineRecipe custom = map.get(key(mode, stack));
            if (custom != null) return custom;
        }

        return imported(type, mode, stack);
    }

    public static Collection<MachineRecipe> getRecipes(MachineType type, int mode) {
        List<MachineRecipe> result = new ArrayList<MachineRecipe>();

        Map<String, MachineRecipe> map = RECIPES.get(type);
        if (map != null) {
            String prefix = mode + ":";
            for (Map.Entry<String, MachineRecipe> entry : map.entrySet()) {
                if (entry.getKey()
                    .startsWith(prefix)) {
                    result.add(entry.getValue());
                }
            }
        }

        result.addAll(importedRecipes(type, mode));
        return result;
    }

    public static List<MachineRecipe> removeByOutput(MachineType type, ItemStack output) {
        List<MachineRecipe> removed = new ArrayList<MachineRecipe>();
        Map<String, MachineRecipe> map = RECIPES.get(type);
        if (map == null || output == null) return removed;

        java.util.Iterator<Map.Entry<String, MachineRecipe>> iterator = map.entrySet()
            .iterator();
        while (iterator.hasNext()) {
            MachineRecipe recipe = iterator.next()
                .getValue();
            if (matches(recipe.getOutput(), output)) {
                removed.add(recipe);
                iterator.remove();
            }
        }
        return removed;
    }

    public static boolean removeByInput(MachineType type, int mode, ItemStack input) {
        Map<String, MachineRecipe> map = RECIPES.get(type);
        if (map == null || input == null) return false;
        return map.remove(key(mode, input)) != null;
    }

    private static MachineRecipe imported(MachineType type, int mode, ItemStack stack) {
        if (type == MachineType.ELECTRIC_FURNACE) {
            ItemStack result = FurnaceRecipes.smelting()
                .getSmeltingResult(stack);
            if (result == null) return null;
            return new MachineRecipe(stack, result, type.getEnergyPerItem());
        }

        RecipeOutput output = getOutput(type, mode, stack);
        if (output == null || output.items == null || output.items.isEmpty()) return null;
        return new MachineRecipe(Collections.singletonList(stack), output.items, type.getEnergyPerItem());
    }

    private static List<MachineRecipe> importedRecipes(MachineType type, int mode) {
        List<MachineRecipe> result = new ArrayList<MachineRecipe>();

        if (type == MachineType.ELECTRIC_FURNACE) {
            for (Object entryObject : FurnaceRecipes.smelting()
                .getSmeltingList()
                .entrySet()) {
                Map.Entry<?, ?> entry = (Map.Entry<?, ?>) entryObject;
                ItemStack input = (ItemStack) entry.getKey();
                ItemStack output = (ItemStack) entry.getValue();
                if (input != null && output != null) {
                    result.add(
                        new MachineRecipe(
                            normalizeWildcard(input),
                            normalizeWildcard(output),
                            type.getEnergyPerItem()));
                }
            }
            return result;
        }

        IMachineRecipeManager manager = manager(type, mode);
        if (manager == null) return result;

        try {
            for (Map.Entry<IRecipeInput, RecipeOutput> entry : manager.getRecipes()
                .entrySet()) {
                IRecipeInput input = entry.getKey();
                RecipeOutput output = entry.getValue();
                if (input == null || output == null || output.items == null || output.items.isEmpty()) continue;

                List<ItemStack> variants = input.getInputs();
                if (variants == null || variants.isEmpty()) continue;

                int amount = Math.max(1, input.getAmount());
                List<ItemStack> inputs = new ArrayList<ItemStack>(variants.size());
                for (ItemStack variant : variants) {
                    if (variant == null) continue;
                    ItemStack copy = variant.copy();
                    copy.stackSize = amount;
                    inputs.add(copy);
                }
                if (!inputs.isEmpty() && !containsSmallDust(inputs) && !containsSmallDust(output.items)) {
                    result.add(new MachineRecipe(inputs, output.items, type.getEnergyPerItem()));
                }
            }
        } catch (Throwable ignored) {
            // IC2 recipe managers can be partially initialised; ignore.
        }

        return result;
    }

    private static RecipeOutput getOutput(MachineType type, int mode, ItemStack stack) {
        IMachineRecipeManager manager = manager(type, mode);
        if (manager == null) return null;
        try {
            return manager.getOutputFor(stack, false);
        } catch (Throwable t) {
            return null;
        }
    }

    private static IMachineRecipeManager manager(MachineType type, int mode) {
        switch (type) {
            case MACERATOR:
                return Recipes.macerator;
            case COMPRESSOR:
                return Recipes.compressor;
            case METAL_EXTRUDER:
                return Recipes.metalformerExtruding;
            case METAL_ROLLER:
                return Recipes.metalformerRolling;
            default:
                return null;
        }
    }

    private static boolean matches(ItemStack a, ItemStack b) {
        return a.getItem() == b.getItem() && a.getItemDamage() == b.getItemDamage();
    }

    /**
     * Vanilla stores some furnace inputs with {@link OreDictionary#WILDCARD_VALUE} (32767) as
     * "any metadata". AE2 compares damage values exactly, so raw pork with damage 32767 never
     * matched the player's raw pork with damage 0. Normalise wildcard entries to damage 0.
     */
    private static ItemStack normalizeWildcard(ItemStack stack) {
        if (stack == null) return null;
        if (stack.getItemDamage() != OreDictionary.WILDCARD_VALUE) return stack;

        ItemStack copy = stack.copy();
        copy.setItemDamage(0);
        return copy;
    }

    /**
     * IC2's "small pile of dust" recipes turn a plate into eight tiny dusts. In AE2 they make
     * the crafting graph loop (dust -> small dust -> plate -> dust) and are never what a player
     * wants a digital machine to export, so they are skipped everywhere.
     */
    public static boolean isSmallDust(ItemStack stack) {
        if (stack == null || stack.getItem() == null) return false;

        String className = stack.getItem()
            .getClass()
            .getName();
        if (className.contains("smallDust") || className.contains("SmallDust")) return true;

        String name = stack.getUnlocalizedName();
        if (name == null) return false;

        String lower = name.toLowerCase(Locale.ENGLISH);
        return lower.contains("dustsmall") || lower.contains("smalldust") || lower.contains("dust_small");
    }

    private static boolean containsSmallDust(List<ItemStack> stacks) {
        if (stacks == null) return false;
        for (ItemStack stack : stacks) {
            if (isSmallDust(stack)) return true;
        }
        return false;
    }

    private static String key(int mode, ItemStack stack) {
        return mode + ":" + Item.getIdFromItem(stack.getItem()) + ":" + stack.getItemDamage();
    }
}
