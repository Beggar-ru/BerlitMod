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

public final class MachineRecipes {

    private static final Map<MachineType, Map<String, MachineRecipe>> RECIPES = new EnumMap<MachineType, Map<String, MachineRecipe>>(
        MachineType.class);

    private MachineRecipes() {}

    public static void init() {
        if (!RECIPES.isEmpty()) return;

        // Placeholder defaults; real recipes can be added/removed with CraftTweaker.
        add(MachineType.MACERATOR, new ItemStack(Blocks.iron_ore), new ItemStack(Items.iron_ingot, 2), 300);
        add(MachineType.MACERATOR, new ItemStack(Blocks.gold_ore), new ItemStack(Items.gold_ingot, 2), 300);
        add(MachineType.MACERATOR, new ItemStack(Blocks.cobblestone), new ItemStack(Blocks.gravel), 200);

        add(MachineType.COMPRESSOR, new ItemStack(Items.coal), new ItemStack(Items.diamond), 800);
        add(MachineType.COMPRESSOR, new ItemStack(Items.redstone), new ItemStack(Blocks.redstone_block), 400);
        add(MachineType.COMPRESSOR, new ItemStack(Items.glowstone_dust, 4), new ItemStack(Blocks.glowstone), 400);

        add(MachineType.METAL_FORMER, new ItemStack(Items.iron_ingot), new ItemStack(Blocks.iron_block), 900);
        add(MachineType.METAL_FORMER, new ItemStack(Items.gold_ingot), new ItemStack(Blocks.gold_block), 900);

        add(MachineType.ELECTRIC_FURNACE, new ItemStack(Blocks.iron_ore), new ItemStack(Items.iron_ingot), 200);
        add(MachineType.ELECTRIC_FURNACE, new ItemStack(Blocks.gold_ore), new ItemStack(Items.gold_ingot), 200);
        add(MachineType.ELECTRIC_FURNACE, new ItemStack(Blocks.sand), new ItemStack(Blocks.glass), 150);
        add(MachineType.ELECTRIC_FURNACE, new ItemStack(Blocks.cobblestone), new ItemStack(Blocks.stone), 150);
    }

    public static void add(MachineType type, ItemStack input, ItemStack output, int energy) {
        Map<String, MachineRecipe> map = RECIPES.get(type);
        if (map == null) {
            map = new HashMap<String, MachineRecipe>();
            RECIPES.put(type, map);
        }
        map.put(key(input), new MachineRecipe(input, output, energy));
    }

    public static MachineRecipe find(MachineType type, ItemStack stack) {
        if (stack == null) return null;
        Map<String, MachineRecipe> map = RECIPES.get(type);
        return map == null ? null : map.get(key(stack));
    }

    public static Collection<MachineRecipe> getRecipes(MachineType type) {
        Map<String, MachineRecipe> map = RECIPES.get(type);
        return map == null ? new ArrayList<MachineRecipe>() : map.values();
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

    public static boolean removeByInput(MachineType type, ItemStack input) {
        Map<String, MachineRecipe> map = RECIPES.get(type);
        if (map == null || input == null) return false;
        return map.remove(key(input)) != null;
    }

    private static boolean matches(ItemStack a, ItemStack b) {
        return a.getItem() == b.getItem() && a.getItemDamage() == b.getItemDamage();
    }

    private static String key(ItemStack stack) {
        return Item.getIdFromItem(stack.getItem()) + ":" + stack.getItemDamage();
    }
}
