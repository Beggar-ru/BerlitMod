package ru.beggar.berlit.compat.nei;

import java.util.ArrayList;
import java.util.Collections;
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
import ru.beggar.berlit.Berlit;
import ru.beggar.berlit.machine.MachineRecipe;
import ru.beggar.berlit.machine.MachineRecipes;
import ru.beggar.berlit.machine.MachineType;

/**
 * NEI page for the Berlit processing machines.
 *
 * <p>
 * Several recipes share one page (three rows) instead of one recipe per page. Each row has its own
 * input and output; the machine icon and the EU value are shown once per row.
 */
@SideOnly(Side.CLIENT)
public class MachineNEI extends TemplateRecipeHandler {

    private static final int PER_PAGE = 3;
    private static final int ROW_HEIGHT = 22;

    public static void register() {
        API.registerRecipeHandler(new MachineNEI());
        API.registerUsageHandler(new MachineNEI());
    }

    public class CachedMachineRecipe extends CachedRecipe {

        private final MachineType type;
        private final int mode;
        private final List<MachineRecipe> recipes;
        private final PositionedStack machine;
        private final List<PositionedStack> inputs = new ArrayList<PositionedStack>();
        private final List<PositionedStack> outputs = new ArrayList<PositionedStack>();

        public CachedMachineRecipe(MachineType type, int mode, List<MachineRecipe> recipes) {
            this.type = type;
            this.mode = mode;
            this.recipes = new ArrayList<MachineRecipe>(recipes);
            this.machine = new PositionedStack(new ItemStack(Berlit.machineBlocks.get(type)), 8, 20);

            for (int i = 0; i < recipes.size(); i++) {
                MachineRecipe recipe = recipes.get(i);
                inputs.add(new PositionedStack(recipe.getInput(), 58, 20 + i * ROW_HEIGHT));
                outputs.add(new PositionedStack(recipe.getOutput(), 112, 20 + i * ROW_HEIGHT));
            }
        }

        @Override
        public PositionedStack getResult() {
            return outputs.isEmpty() ? null : outputs.get(0);
        }

        @Override
        public List<PositionedStack> getIngredients() {
            return inputs;
        }

        @Override
        public List<PositionedStack> getOtherStacks() {
            List<PositionedStack> result = new ArrayList<PositionedStack>();
            result.add(machine);
            for (int i = 1; i < outputs.size(); i++) {
                result.add(outputs.get(i));
            }
            return result;
        }

        public MachineType getMachineType() {
            return type;
        }

        public int getMode() {
            return mode;
        }

        public List<MachineRecipe> getRecipes() {
            return Collections.unmodifiableList(recipes);
        }
    }

    @Override
    public String getRecipeName() {
        return StatCollector.translateToLocal("nei.berlit.machines");
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

        for (MachineType type : MachineType.values()) {
            int mode = type.getDefaultMode();
            List<MachineRecipe> matches = new ArrayList<MachineRecipe>();
            for (MachineRecipe recipe : MachineRecipes.getRecipes(type, mode)) {
                ItemStack output = recipe.getOutput();
                if (output != null && output.getItem() == result.getItem()
                    && output.getItemDamage() == result.getItemDamage()) {
                    matches.add(recipe);
                }
            }
            addPages(type, mode, matches);
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        if (ingredient == null) return;

        for (MachineType type : MachineType.values()) {
            if (ingredient.getItem() == Item.getItemFromBlock(Berlit.machineBlocks.get(type))) {
                addPages(
                    type,
                    type.getDefaultMode(),
                    new ArrayList<MachineRecipe>(MachineRecipes.getRecipes(type, type.getDefaultMode())));
                return;
            }
        }

        for (MachineType type : MachineType.values()) {
            int mode = type.getDefaultMode();
            List<MachineRecipe> matches = new ArrayList<MachineRecipe>();
            for (MachineRecipe recipe : MachineRecipes.getRecipes(type, mode)) {
                ItemStack input = recipe.getInput();
                if (input != null && input.getItem() == ingredient.getItem()
                    && input.getItemDamage() == ingredient.getItemDamage()) {
                    matches.add(recipe);
                }
            }
            addPages(type, mode, matches);
        }
    }

    private void addPages(MachineType type, int mode, List<MachineRecipe> recipes) {
        if (recipes == null || recipes.isEmpty()) return;

        for (int start = 0; start < recipes.size(); start += PER_PAGE) {
            int end = Math.min(recipes.size(), start + PER_PAGE);
            arecipes.add(new CachedMachineRecipe(type, mode, recipes.subList(start, end)));
        }
    }

    @Override
    public void drawExtras(int recipe) {
        if (recipe < 0 || recipe >= arecipes.size()) return;

        CachedMachineRecipe cached = (CachedMachineRecipe) arecipes.get(recipe);
        MachineType type = cached.getMachineType();

        Minecraft.getMinecraft().fontRenderer
            .drawString(StatCollector.translateToLocal(type.getTitleKey()), 8, 6, 0x404040);

        List<MachineRecipe> recipes = cached.getRecipes();
        for (int i = 0; i < recipes.size(); i++) {
            int rowY = 20 + i * ROW_HEIGHT;
            MachineRecipe machineRecipe = recipes.get(i);
            Minecraft.getMinecraft().fontRenderer
                .drawString("EU: " + machineRecipe.getEnergy(), i == 0 ? 30 : 8, rowY + 4, 0x404040);
            Minecraft.getMinecraft().fontRenderer.drawString("->", 88, rowY + 4, 0x606060);
        }
    }
}
