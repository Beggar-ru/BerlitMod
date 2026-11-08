package ru.beggar.berlit.compat.nei;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import codechicken.nei.PositionedStack;
import codechicken.nei.api.API;
import codechicken.nei.recipe.TemplateRecipeHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ic2.api.item.IC2Items;
import ru.beggar.berlit.Berlit;
import ru.beggar.berlit.matter.MatterType;

/** NEI page for the eight solid matter generators: scrap or the previous kind in, the next kind out. */
@SideOnly(Side.CLIENT)
public class MatterNEI extends TemplateRecipeHandler {

    public static void register() {
        API.registerRecipeHandler(new MatterNEI());
        API.registerUsageHandler(new MatterNEI());
    }

    public class CachedMatterRecipe extends CachedRecipe {

        private final MatterType type;
        private final PositionedStack input;
        private final PositionedStack output;

        public CachedMatterRecipe(MatterType type) {
            this.type = type;
            this.input = new PositionedStack(inputFor(type), 30, 20);
            this.output = new PositionedStack(new ItemStack(Berlit.matterItems[type.ordinal()]), 92, 20);
        }

        @Override
        public PositionedStack getResult() {
            return output;
        }

        @Override
        public List<PositionedStack> getIngredients() {
            List<PositionedStack> ingredients = new ArrayList<PositionedStack>();
            ingredients.add(input);
            return ingredients;
        }
    }

    private static ItemStack inputFor(MatterType type) {
        if (type.isFirst()) {
            return IC2Items.getItem("scrap");
        }
        return new ItemStack(
            Berlit.matterItems[type.getPrevious()
                .ordinal()]);
    }

    private static boolean matches(Item item, ItemStack stack) {
        return item != null && stack != null && stack.getItem() == item;
    }

    @Override
    public String getRecipeName() {
        return StatCollector.translateToLocal("nei.berlit.matter");
    }

    @Override
    public String getGuiTexture() {
        return "berlit:textures/gui/nei_page.png";
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
        for (MatterType type : MatterType.values()) {
            if (matches(Berlit.matterItems[type.ordinal()], result)) {
                arecipes.add(new CachedMatterRecipe(type));
            }
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        if (ingredient == null) return;
        for (MatterType type : MatterType.values()) {
            if (matches(ingredient.getItem(), inputFor(type))) {
                arecipes.add(new CachedMatterRecipe(type));
            }
        }
    }

    @Override
    public void drawExtras(int recipe) {
        if (recipe < 0 || recipe >= arecipes.size()) return;
        MatterType type = ((CachedMatterRecipe) arecipes.get(recipe)).type;

        Minecraft.getMinecraft().fontRenderer.drawString(
            StatCollector.translateToLocalFormatted("nei.berlit.matter.cost", type.getEnergyPerOperation()),
            8,
            44,
            0x606060);
        Minecraft.getMinecraft().fontRenderer.drawString(
            StatCollector.translateToLocalFormatted("nei.berlit.matter.time", type.getTicksPerOperation()),
            8,
            56,
            0x606060);
    }
}
