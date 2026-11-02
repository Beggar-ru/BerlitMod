package ru.beggar.berlit.compat.nei;

import java.util.Collections;
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
import ru.beggar.berlit.machine.MachineRecipe;
import ru.beggar.berlit.machine.MachineRecipes;
import ru.beggar.berlit.machine.MachineType;

@SideOnly(Side.CLIENT)
public class MachineNEI extends TemplateRecipeHandler {

    public static void register() {
        API.registerRecipeHandler(new MachineNEI());
        API.registerUsageHandler(new MachineNEI());
    }

    public class CachedMachineRecipe extends CachedRecipe {

        private final MachineType type;
        private final int mode;
        private final PositionedStack machine;
        private final PositionedStack input;
        private final PositionedStack output;

        public CachedMachineRecipe(MachineType type, int mode, MachineRecipe recipe) {
            this.type = type;
            this.mode = mode;
            this.machine = new PositionedStack(new ItemStack(Berlit.machineBlocks.get(type)), 8, 20);
            this.input = new PositionedStack(recipe.getInput(), 50, 20);
            this.output = new PositionedStack(recipe.getOutput(), 110, 20);
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

        public MachineType getMachineType() {
            return type;
        }

        public int getMode() {
            return mode;
        }
    }

    @Override
    public String getRecipeName() {
        return StatCollector.translateToLocal("nei.berlit.machines");
    }

    @Override
    public String getGuiTexture() {
        return "berlit:textures/gui/nei_spawner.png";
    }

    private static int modes(MachineType type) {
        return type == MachineType.METAL_FORMER ? 3 : 1;
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
            for (int mode = 0; mode < modes(type); mode++) {
                for (MachineRecipe recipe : MachineRecipes.getRecipes(type, mode)) {
                    if (recipe.getOutput()
                        .getItem() == result.getItem()
                        && recipe.getOutput()
                            .getItemDamage() == result.getItemDamage()) {
                        arecipes.add(new CachedMachineRecipe(type, mode, recipe));
                    }
                }
            }
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        if (ingredient == null) return;

        for (MachineType type : MachineType.values()) {
            for (int mode = 0; mode < modes(type); mode++) {
                for (MachineRecipe recipe : MachineRecipes.getRecipes(type, mode)) {
                    if (recipe.getInput()
                        .getItem() == ingredient.getItem()
                        && recipe.getInput()
                            .getItemDamage() == ingredient.getItemDamage()) {
                        arecipes.add(new CachedMachineRecipe(type, mode, recipe));
                    }
                }
            }
        }
    }

    @Override
    public void drawExtras(int recipe) {
        if (recipe < 0 || recipe >= arecipes.size()) return;
        CachedMachineRecipe cached = (CachedMachineRecipe) arecipes.get(recipe);
        Minecraft.getMinecraft().fontRenderer.drawString(
            StatCollector.translateToLocal(
                cached.getMachineType()
                    .getTitleKey()),
            8,
            6,
            0x404040);

        if (cached.getMachineType() == MachineType.METAL_FORMER) {
            Minecraft.getMinecraft().fontRenderer
                .drawString(StatCollector.translateToLocal("berlit.gui.mode." + cached.getMode()), 8, 150, 0x404040);
        }
    }
}
