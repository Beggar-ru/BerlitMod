package ru.beggar.berlit.compat.nei;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import codechicken.nei.PositionedStack;
import codechicken.nei.api.API;
import codechicken.nei.recipe.TemplateRecipeHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ru.beggar.berlit.Berlit;
import ru.beggar.berlit.recipe.MolecularRecipe;
import ru.beggar.berlit.recipe.MolecularRecipes;

/** NEI page for the Berlit molecular transformer, including recipes imported from ASP. */
@SideOnly(Side.CLIENT)
public class MolecularTransformerNEI extends TemplateRecipeHandler {

    public static void register() {
        API.registerRecipeHandler(new MolecularTransformerNEI());
        API.registerUsageHandler(new MolecularTransformerNEI());
    }

    public class CachedMolecularRecipe extends CachedRecipe {

        private final MolecularRecipe recipe;
        private final PositionedStack machine;
        private final PositionedStack input;
        private final PositionedStack output;

        public CachedMolecularRecipe(MolecularRecipe recipe) {
            this.recipe = recipe;
            this.machine = new PositionedStack(new ItemStack(Berlit.molecularTransformer), 8, 20);
            this.input = new PositionedStack(recipe.getInput(), 58, 20);
            this.output = new PositionedStack(recipe.getOutput(), 112, 20);
        }

        @Override
        public PositionedStack getResult() {
            return output;
        }

        @Override
        public List<PositionedStack> getIngredients() {
            List<PositionedStack> result = new ArrayList<PositionedStack>();
            result.add(machine);
            result.add(input);
            return result;
        }

        public MolecularRecipe getRecipe() {
            return recipe;
        }
    }

    @Override
    public String getRecipeName() {
        return StatCollector.translateToLocal("nei.berlit.molecular_transformer");
    }

    @Override
    public String getGuiTexture() {
        return "berlit:textures/gui/nei_page.png";
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if (!"item".equals(outputId)) return;
        for (Object result : results) {
            if (result instanceof ItemStack) loadCraftingRecipes((ItemStack) result);
        }
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        if (result == null) return;
        for (MolecularRecipe recipe : MolecularRecipes.getRecipes()) {
            ItemStack output = recipe.getOutput();
            if (output != null && output.getItem() == result.getItem()
                && output.getItemDamage() == result.getItemDamage()) {
                arecipes.add(new CachedMolecularRecipe(recipe));
            }
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        if (ingredient == null) return;
        for (MolecularRecipe recipe : MolecularRecipes.getRecipes()) {
            ItemStack input = recipe.getInput();
            if (input != null && input.getItem() == ingredient.getItem()
                && input.getItemDamage() == ingredient.getItemDamage()) {
                arecipes.add(new CachedMolecularRecipe(recipe));
            }
        }
    }

    @Override
    public void drawExtras(int recipe) {
        if (recipe < 0 || recipe >= arecipes.size()) return;
        CachedMolecularRecipe cached = (CachedMolecularRecipe) arecipes.get(recipe);
        Minecraft.getMinecraft().fontRenderer.drawString(
            StatCollector.translateToLocalFormatted(
                "nei.berlit.molecular_transformer.energy",
                cached.getRecipe()
                    .getEnergy()),
            8,
            46,
            0x404040);
    }
}
