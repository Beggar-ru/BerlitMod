package ru.beggar.berlit.compat.nei;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.oredict.OreDictionary;

import codechicken.nei.PositionedStack;
import codechicken.nei.api.API;
import codechicken.nei.recipe.TemplateRecipeHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fox.spiteful.avaritia.crafting.CompressorManager;
import fox.spiteful.avaritia.crafting.CompressorRecipe;
import ru.beggar.berlit.Berlit;

/** NEI page for the neutron compressor: input x cost -> singularity. */
@SideOnly(Side.CLIENT)
public class CompressorNEI extends TemplateRecipeHandler {

    public static void register() {
        API.registerRecipeHandler(new CompressorNEI());
        API.registerUsageHandler(new CompressorNEI());
    }

    private static ItemStack ingredientStack(CompressorRecipe recipe) {
        Object ingredient = recipe.getIngredient();
        if (ingredient instanceof ItemStack) return ((ItemStack) ingredient).copy();
        if (ingredient instanceof String) {
            List<ItemStack> ores = OreDictionary.getOres((String) ingredient);
            if (!ores.isEmpty()) return ores.get(0)
                .copy();
        }
        return null;
    }

    private static boolean isValid(CompressorRecipe recipe) {
        return ingredientStack(recipe) != null && recipe.getOutput() != null;
    }

    public class CachedCompressor extends CachedRecipe {

        private final PositionedStack machine;
        private final PositionedStack input;
        private final PositionedStack output;

        public CachedCompressor(CompressorRecipe recipe) {
            this.machine = new PositionedStack(new ItemStack(Berlit.neutronCompressor), 8, 20);

            ItemStack stack = ingredientStack(recipe);
            if (stack != null) stack.stackSize = Math.max(1, recipe.getCost());
            this.input = new PositionedStack(stack, 50, 20);
            this.output = new PositionedStack(recipe.getOutput(), 116, 20);
        }

        @Override
        public PositionedStack getResult() {
            return output;
        }

        @Override
        public List<PositionedStack> getIngredients() {
            List<PositionedStack> list = new ArrayList<PositionedStack>();
            list.add(machine);
            list.add(input);
            return list;
        }
    }

    @Override
    public String getRecipeName() {
        return StatCollector.translateToLocal("tile.berlit.neutron_compressor.name");
    }

    @Override
    public String getGuiTexture() {
        return "berlit:textures/gui/nei_page.png";
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if (!"item".equals(outputId)) return;
        for (Object result : results) {
            if (!(result instanceof ItemStack)) continue;
            loadCraftingRecipes((ItemStack) result);
        }
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        for (CompressorRecipe recipe : CompressorManager.getRecipes()) {
            if (!isValid(recipe)) continue;
            if (recipe.getOutput()
                .isItemEqual(result)) {
                arecipes.add(new CachedCompressor(recipe));
            }
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        for (CompressorRecipe recipe : CompressorManager.getRecipes()) {
            if (!isValid(recipe)) continue;
            if (recipe.validInput(ingredient)) {
                arecipes.add(new CachedCompressor(recipe));
            }
        }
    }
}
