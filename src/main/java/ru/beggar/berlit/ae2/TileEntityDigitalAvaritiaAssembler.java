package ru.beggar.berlit.ae2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import fox.spiteful.avaritia.crafting.ExtremeCraftingManager;
import fox.spiteful.avaritia.crafting.ExtremeShapedOreRecipe;
import fox.spiteful.avaritia.crafting.ExtremeShapedRecipe;
import fox.spiteful.avaritia.crafting.ExtremeShapelessRecipe;
import ru.beggar.berlit.Berlit;

/** Digital ME assembler that automatically exports every Avaritia 9x9 recipe. */
public class TileEntityDigitalAvaritiaAssembler extends TileEntityDigitalCrafting {

    private static final int GRID = 9;
    private static final int SLOTS = GRID * GRID;

    @Override
    protected List<ICraftingPatternDetails> createPatterns() {
        List<ICraftingPatternDetails> result = new ArrayList<ICraftingPatternDetails>();
        Set<String> seen = new HashSet<String>();

        for (Object object : ExtremeCraftingManager.getInstance()
            .getRecipeList()) {
            if (!(object instanceof IRecipe)) continue;
            IRecipe recipe = (IRecipe) object;

            ItemStack[] grid = gridFor(recipe);
            ItemStack output = recipe.getRecipeOutput();
            if (grid == null || output == null || !hasInput(grid)) continue;

            String key = key(grid, output);
            if (!seen.add(key)) continue;

            ItemStack pattern = ItemAvaritiaPattern.create(grid, output);
            if (pattern == null) continue;
            result.add(new AvaritiaPatternDetails(pattern));
        }
        return result;
    }

    private static ItemStack[] gridFor(IRecipe recipe) {
        ItemStack[] grid = new ItemStack[SLOTS];

        if (recipe instanceof ExtremeShapedRecipe) {
            ExtremeShapedRecipe shaped = (ExtremeShapedRecipe) recipe;
            for (int row = 0; row < shaped.recipeHeight; row++) {
                for (int col = 0; col < shaped.recipeWidth; col++) {
                    int source = row * shaped.recipeWidth + col;
                    if (source < shaped.recipeItems.length) {
                        grid[row * GRID + col] = copy(shaped.recipeItems[source]);
                    }
                }
            }
            return grid;
        }

        if (recipe instanceof ExtremeShapedOreRecipe) {
            ExtremeShapedOreRecipe shaped = (ExtremeShapedOreRecipe) recipe;
            Object[] input = shaped.getInput();
            for (int row = 0; row < shaped.height; row++) {
                for (int col = 0; col < shaped.width; col++) {
                    int source = row * shaped.width + col;
                    if (source < input.length) {
                        grid[row * GRID + col] = stackFrom(input[source]);
                    }
                }
            }
            return grid;
        }

        if (recipe instanceof ExtremeShapelessRecipe) {
            List<?> items = ((ExtremeShapelessRecipe) recipe).recipeItems;
            for (int i = 0; i < items.size() && i < SLOTS; i++) {
                grid[i] = stackFrom(items.get(i));
            }
            return grid;
        }

        if (recipe instanceof ShapelessOreRecipe) {
            List<?> items = ((ShapelessOreRecipe) recipe).getInput();
            for (int i = 0; i < items.size() && i < SLOTS; i++) {
                grid[i] = stackFrom(items.get(i));
            }
            return grid;
        }

        return null;
    }

    private static ItemStack stackFrom(Object object) {
        if (object instanceof ItemStack) return copy((ItemStack) object);
        if (object instanceof List) {
            for (Object value : (List<?>) object) {
                ItemStack stack = stackFrom(value);
                if (stack != null) return stack;
            }
        }
        if (object instanceof String) {
            List<ItemStack> ores = OreDictionary.getOres((String) object);
            if (!ores.isEmpty()) return copy(ores.get(0));
        }
        return null;
    }

    private static ItemStack copy(ItemStack stack) {
        return stack == null ? null : stack.copy();
    }

    private static boolean hasInput(ItemStack[] grid) {
        for (ItemStack stack : grid) {
            if (stack != null) return true;
        }
        return false;
    }

    private static String key(ItemStack[] grid, ItemStack output) {
        StringBuilder result = new StringBuilder();
        result.append(net.minecraft.item.Item.getIdFromItem(output.getItem()))
            .append(':')
            .append(output.getItemDamage())
            .append(':')
            .append(output.stackSize)
            .append('|');
        for (ItemStack stack : grid) {
            if (stack == null) {
                result.append("0:0,");
            } else {
                result.append(net.minecraft.item.Item.getIdFromItem(stack.getItem()))
                    .append(':')
                    .append(stack.getItemDamage())
                    .append(',');
            }
        }
        return result.toString();
    }

    @Override
    protected boolean acceptPattern(ICraftingPatternDetails details) {
        return details instanceof AvaritiaPatternDetails;
    }

    @Override
    protected ICraftingPatternDetails parsePattern(ItemStack pattern) {
        return pattern != null && pattern.getItem() == Berlit.avaritiaPattern ? new AvaritiaPatternDetails(pattern)
            : null;
    }

    @Override
    protected int getBaseTicks(ICraftingPatternDetails details) {
        return 100;
    }

    @Override
    protected int getEnergyPerOperation(ICraftingPatternDetails details) {
        return 5000;
    }

    @Override
    protected ItemStack getMachineStack() {
        return new ItemStack(Berlit.digitalAvaritiaAssembler);
    }

    @Override
    public String getInventoryKey() {
        return "container.berlit.digital_avaritia_assembler";
    }

    @Override
    public String getGuiTitleKey() {
        return "tile.berlit.digital_avaritia_assembler.name";
    }
}
