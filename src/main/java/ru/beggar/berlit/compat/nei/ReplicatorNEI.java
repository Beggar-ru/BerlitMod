package ru.beggar.berlit.compat.nei;

import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import codechicken.nei.PositionedStack;
import codechicken.nei.api.API;
import codechicken.nei.recipe.TemplateRecipeHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ru.beggar.berlit.Berlit;
import ru.beggar.berlit.gui.GuiTheme;
import ru.beggar.berlit.recipe.ReplicatorPattern;
import ru.beggar.berlit.recipe.ReplicatorRecipes;

/**
 * NEI page for the replicator.
 *
 * <p>
 * The page works for any item that has a UU value, so pressing R or U on an item shows how much
 * liquid UU matter one copy and one whole output stack cost. The machine item itself shows the
 * patterns registered by CraftTweaker.
 */
@SideOnly(Side.CLIENT)
public class ReplicatorNEI extends TemplateRecipeHandler {

    public static void register() {
        API.registerRecipeHandler(new ReplicatorNEI());
        API.registerUsageHandler(new ReplicatorNEI());
    }

    public class CachedReplicatorRecipe extends CachedRecipe {

        private final ReplicatorPattern pattern;
        private final PositionedStack machine;
        private final PositionedStack input;
        private final PositionedStack output;

        public CachedReplicatorRecipe(ReplicatorPattern pattern) {
            this.pattern = pattern;
            this.machine = new PositionedStack(new ItemStack(Berlit.replicator), 8, 20);
            this.input = new PositionedStack(pattern.getStack(), 58, 20);
            this.output = new PositionedStack(pattern.copyStack(pattern.getStackSize()), 112, 20);
        }

        @Override
        public PositionedStack getResult() {
            return output;
        }

        @Override
        public List<PositionedStack> getIngredients() {
            return Collections.singletonList(input);
        }

        @Override
        public List<PositionedStack> getOtherStacks() {
            return Collections.singletonList(machine);
        }

        public ReplicatorPattern getPattern() {
            return pattern;
        }
    }

    @Override
    public String getRecipeName() {
        return StatCollector.translateToLocal("nei.berlit.replicator");
    }

    @Override
    public String getGuiTexture() {
        return "berlit:textures/gui/nei_replicator.png";
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if ("item".equals(outputId) && results.length > 0 && results[0] instanceof ItemStack) {
            loadCraftingRecipes((ItemStack) results[0]);
        }
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        addPattern(result);
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        if (ingredient == null) return;

        if (ingredient.getItem() == Item.getItemFromBlock(Berlit.replicator)) {
            int before = arecipes.size();
            for (ReplicatorPattern pattern : ReplicatorRecipes.getCustomPatterns()) {
                arecipes.add(new CachedReplicatorRecipe(pattern));
            }

            // An empty page on U would look like a broken handler; show a few standard values
            // (the real full list is IC2's UU graph, which is far too large for one NEI page).
            if (arecipes.size() == before) {
                addPattern(new ItemStack(Items.diamond));
                addPattern(new ItemStack(Items.iron_ingot));
                addPattern(new ItemStack(Items.gold_ingot));
                addPattern(new ItemStack(Items.redstone));
                addPattern(new ItemStack(Items.emerald));
                addPattern(new ItemStack(Blocks.cobblestone));
            }
            return;
        }

        // Scanning uses the item as both the pattern and the output.
        addPattern(ingredient);
    }

    private void addPattern(ItemStack stack) {
        ReplicatorPattern pattern = ReplicatorRecipes.createPattern(stack);
        if (pattern != null) {
            arecipes.add(new CachedReplicatorRecipe(pattern));
        }
    }

    @Override
    public void drawExtras(int recipe) {
        if (recipe < 0 || recipe >= arecipes.size()) return;

        CachedReplicatorRecipe cached = (CachedReplicatorRecipe) arecipes.get(recipe);
        ReplicatorPattern pattern = cached.getPattern();

        Minecraft.getMinecraft().fontRenderer
            .drawString(StatCollector.translateToLocal("tile.berlit.replicator.name"), 8, 4, GuiTheme.TITLE);
        String scan = StatCollector.translateToLocalFormatted(
            "nei.berlit.replicator.scan",
            pattern.getStack()
                .getDisplayName());
        Minecraft.getMinecraft().fontRenderer.drawString(
            Minecraft.getMinecraft().fontRenderer.trimStringToWidth(scan, 150),
            8,
            38,
            GuiTheme.TEXT_DISABLED);
        Minecraft.getMinecraft().fontRenderer.drawString(
            StatCollector
                .translateToLocalFormatted("nei.berlit.replicator.matter_per_item", pattern.getMatterPerItem()),
            8,
            47,
            GuiTheme.TEXT);
        Minecraft.getMinecraft().fontRenderer.drawString(
            StatCollector.translateToLocalFormatted(
                "nei.berlit.replicator.matter_per_stack",
                pattern.getMatterPerStack(),
                pattern.getStackSize()),
            8,
            56,
            GuiTheme.TEXT);
    }
}
