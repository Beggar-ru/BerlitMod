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
import ru.beggar.berlit.farm.FarmRecipe;
import ru.beggar.berlit.farm.FarmRecipes;
import ru.beggar.berlit.gui.GuiTheme;

/** NEI page for the molecular farm: one seed -> up to six products, per one seed. */
@SideOnly(Side.CLIENT)
public class MolecularFarmNEI extends TemplateRecipeHandler {

    public static void register() {
        API.registerRecipeHandler(new MolecularFarmNEI());
        API.registerUsageHandler(new MolecularFarmNEI());
    }

    public class CachedFarmRecipe extends CachedRecipe {

        private final PositionedStack input;
        private final List<PositionedStack> outputs = new ArrayList<PositionedStack>();

        public CachedFarmRecipe(FarmRecipe recipe) {
            input = new PositionedStack(recipe.getInput(), 30, 30);
            List<ItemStack> products = recipe.getOutputs();
            for (int i = 0; i < products.size() && i < 6; i++) {
                outputs.add(new PositionedStack(products.get(i), 78 + (i % 3) * 20, 18 + (i / 3) * 22));
            }
        }

        @Override
        public PositionedStack getResult() {
            return outputs.isEmpty() ? null : outputs.get(0);
        }

        @Override
        public List<PositionedStack> getIngredients() {
            List<PositionedStack> ingredients = new ArrayList<PositionedStack>();
            ingredients.add(input);
            return ingredients;
        }

        @Override
        public List<PositionedStack> getOtherStacks() {
            List<PositionedStack> result = new ArrayList<PositionedStack>();
            for (int i = 1; i < outputs.size(); i++) result.add(outputs.get(i));
            return result;
        }
    }

    @Override
    public String getRecipeName() {
        return StatCollector.translateToLocal("nei.berlit.molecular_farm");
    }

    @Override
    public String getGuiTexture() {
        return "berlit:textures/gui/nei_molecular_farm.png";
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if ("item".equals(outputId) && results.length > 0 && results[0] instanceof ItemStack) {
            loadCraftingRecipes((ItemStack) results[0]);
        }
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        if (result == null) return;
        for (FarmRecipe recipe : FarmRecipes.getRecipes()) {
            for (ItemStack output : recipe.getOutputsView()) {
                if (output.getItem() == result.getItem() && output.getItemDamage() == result.getItemDamage()) {
                    arecipes.add(new CachedFarmRecipe(recipe));
                    break;
                }
            }
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        FarmRecipe recipe = FarmRecipes.find(ingredient);
        if (recipe != null) arecipes.add(new CachedFarmRecipe(recipe));
    }

    @Override
    public void drawExtras(int recipe) {
        if (recipe < 0 || recipe >= arecipes.size()) return;
        Minecraft.getMinecraft().fontRenderer.drawString(
            StatCollector.translateToLocal("nei.berlit.molecular_farm.per_seed"),
            8,
            66,
            GuiTheme.TEXT_DISABLED);
    }
}
