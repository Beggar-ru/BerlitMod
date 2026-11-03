package ru.beggar.berlit.compat.nei;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

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

    public class CachedCompressor extends CachedRecipe {

        private final PositionedStack machine;
        private final PositionedStack input;
        private final PositionedStack output;

        public CachedCompressor(CompressorRecipe recipe) {
            this.machine = new PositionedStack(new ItemStack(Berlit.neutronCompressor), 8, 20);

            Object ingredient = recipe.getIngredient();
            ItemStack stack = ingredient instanceof ItemStack ? ((ItemStack) ingredient).copy() : null;
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
        return "berlit:textures/gui/neutron_compressor.png";
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if (!"item".equals(outputId)) return;
        for (Object result : results) {
            if (!(result instanceof ItemStack)) continue;
            for (CompressorRecipe recipe : CompressorManager.getRecipes()) {
                if (recipe.getOutput()
                    .isItemEqual((ItemStack) result)) {
                    arecipes.add(new CachedCompressor(recipe));
                }
            }
        }
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        for (CompressorRecipe recipe : CompressorManager.getRecipes()) {
            if (recipe.getOutput()
                .isItemEqual(result)) {
                arecipes.add(new CachedCompressor(recipe));
            }
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        for (CompressorRecipe recipe : CompressorManager.getRecipes()) {
            Object input = recipe.getIngredient();
            if (input instanceof ItemStack && ((ItemStack) input).isItemEqual(ingredient)) {
                arecipes.add(new CachedCompressor(recipe));
            }
        }
    }
}
