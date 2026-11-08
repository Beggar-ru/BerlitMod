package ru.beggar.berlit.ae2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import fox.spiteful.avaritia.crafting.CompressorManager;
import fox.spiteful.avaritia.crafting.CompressorRecipe;
import ru.beggar.berlit.Berlit;

/** Digital ME neutron compressor that automatically exports every compressor recipe. */
public class TileEntityDigitalNeutronCompressor extends TileEntityDigitalCrafting {

    @Override
    protected List<ICraftingPatternDetails> createPatterns() {
        List<ICraftingPatternDetails> result = new ArrayList<ICraftingPatternDetails>();
        Set<String> seen = new HashSet<String>();

        for (CompressorRecipe recipe : CompressorManager.getRecipes()) {
            ItemStack input = stackFrom(recipe.getIngredient());
            ItemStack output = recipe.getOutput();
            int cost = Math.max(1, recipe.getCost());
            if (input == null || output == null) continue;

            String key = key(input, cost, output);
            if (!seen.add(key)) continue;

            ItemStack pattern = ItemSingularityPattern.create(input, cost, output);
            if (pattern == null) continue;
            result.add(new SingularityPatternDetails(pattern));
        }
        return result;
    }

    private static ItemStack stackFrom(Object object) {
        if (object instanceof ItemStack) return ((ItemStack) object).copy();
        if (object instanceof List) {
            for (Object value : (List<?>) object) {
                ItemStack stack = stackFrom(value);
                if (stack != null) return stack;
            }
        }
        if (object instanceof String) {
            List<ItemStack> ores = OreDictionary.getOres((String) object);
            if (!ores.isEmpty()) return ores.get(0)
                .copy();
        }
        return null;
    }

    private static String key(ItemStack input, int cost, ItemStack output) {
        return net.minecraft.item.Item.getIdFromItem(input.getItem()) + ":"
            + input.getItemDamage()
            + ":"
            + cost
            + "->"
            + net.minecraft.item.Item.getIdFromItem(output.getItem())
            + ":"
            + output.getItemDamage();
    }

    @Override
    protected boolean acceptPattern(ICraftingPatternDetails details) {
        return details instanceof SingularityPatternDetails;
    }

    @Override
    protected ICraftingPatternDetails parsePattern(ItemStack pattern) {
        return pattern != null && pattern.getItem() == Berlit.singularityPattern
            ? new SingularityPatternDetails(pattern)
            : null;
    }

    @Override
    protected int getBaseTicks(ICraftingPatternDetails details) {
        return 40;
    }

    @Override
    protected int getEnergyPerOperation(ICraftingPatternDetails details) {
        if (details instanceof SingularityPatternDetails) {
            return Math.max(2000, ((SingularityPatternDetails) details).getCost() * 2);
        }
        return 2000;
    }

    @Override
    protected ItemStack getMachineStack() {
        return new ItemStack(Berlit.digitalNeutronCompressor);
    }

    @Override
    public String getInventoryKey() {
        return "container.berlit.digital_neutron_compressor";
    }

    @Override
    public String getGuiTitleKey() {
        return "tile.berlit.digital_neutron_compressor.name";
    }
}
