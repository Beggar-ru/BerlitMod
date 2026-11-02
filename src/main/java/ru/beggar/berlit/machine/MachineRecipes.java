package ru.beggar.berlit.machine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;

import ic2.api.recipe.IMachineRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.RecipeOutput;
import ic2.api.recipe.Recipes;

public final class MachineRecipes {

    public static final int MODE_EXTRUDING = 0;
    public static final int MODE_CUTTING = 1;
    public static final int MODE_ROLLING = 2;

    private static final Map<MachineType, Map<String, MachineRecipe>> RECIPES = new EnumMap<MachineType, Map<String, MachineRecipe>>(
        MachineType.class);

    private MachineRecipes() {}

    public static void init() {
        if (!RECIPES.isEmpty()) return;

        // Placeholder defaults; real recipes come from IC2/FurnaceRecipes and CraftTweaker.
        add(MachineType.MACERATOR, 0, new ItemStack(Blocks.iron_ore), new ItemStack(Items.iron_ingot, 2), 300);
        add(MachineType.MACERATOR, 0, new ItemStack(Blocks.gold_ore), new ItemStack(Items.gold_ingot, 2), 300);

        add(MachineType.COMPRESSOR, 0, new ItemStack(Items.redstone), new ItemStack(Blocks.redstone_block), 400);
        add(MachineType.COMPRESSOR, 0, new ItemStack(Items.glowstone_dust, 4), new ItemStack(Blocks.glowstone), 400);

        add(
            MachineType.METAL_FORMER,
            MODE_EXTRUDING,
            new ItemStack(Items.iron_ingot),
            new ItemStack(Blocks.iron_block),
            900);
        add(
            MachineType.METAL_FORMER,
            MODE_ROLLING,
            new ItemStack(Items.gold_ingot),
            new ItemStack(Blocks.gold_block),
            900);
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
        ItemStack result = output.items.get(0)
            .copy();
        if (result == null) return null;
        return new MachineRecipe(stack, result, type.getEnergyPerItem());
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
                    result.add(new MachineRecipe(input, output, type.getEnergyPerItem()));
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

                List<ItemStack> inputs = input.getInputs();
                if (inputs == null || inputs.isEmpty()) continue;

                ItemStack inputStack = inputs.get(0);
                ItemStack outputStack = output.items.get(0);
                if (inputStack != null && outputStack != null) {
                    result.add(new MachineRecipe(inputStack, outputStack, type.getEnergyPerItem()));
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
            case METAL_FORMER:
                if (mode == MODE_CUTTING) return Recipes.metalformerCutting;
                if (mode == MODE_ROLLING) return Recipes.metalformerRolling;
                return Recipes.metalformerExtruding;
            default:
                return null;
        }
    }

    private static boolean matches(ItemStack a, ItemStack b) {
        return a.getItem() == b.getItem() && a.getItemDamage() == b.getItemDamage();
    }

    private static String key(int mode, ItemStack stack) {
        return mode + ":" + Item.getIdFromItem(stack.getItem()) + ":" + stack.getItemDamage();
    }
}
