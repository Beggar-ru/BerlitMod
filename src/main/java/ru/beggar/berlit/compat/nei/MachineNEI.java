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
        private final int energy;

        public CachedMachineRecipe(MachineType type, int mode, MachineRecipe recipe) {
            this.type = type;
            this.mode = mode;
            this.energy = recipe.getEnergy();
            this.machine = new PositionedStack(new ItemStack(Berlit.machineBlocks.get(type)), 8, 20);
            this.input = new PositionedStack(recipe.getInput(), 58, 20);
            this.output = new PositionedStack(recipe.getOutput(), 112, 20);
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
            for (MachineRecipe recipe : MachineRecipes.getRecipes(type, mode)) {
                ItemStack output = recipe.getOutput();
                if (output.getItem() == result.getItem() && output.getItemDamage() == result.getItemDamage()) {
                    arecipes.add(new CachedMachineRecipe(type, mode, recipe));
                }
            }
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        if (ingredient == null) return;

        for (MachineType type : MachineType.values()) {
            ItemStack machine = new ItemStack(Berlit.machineBlocks.get(type));
            if (machine.getItem() != ingredient.getItem() || machine.getItemDamage() != ingredient.getItemDamage()) {
                continue;
            }

            int mode = type.getDefaultMode();
            for (MachineRecipe recipe : MachineRecipes.getRecipes(type, mode)) {
                arecipes.add(new CachedMachineRecipe(type, mode, recipe));
            }
            return;
        }

        for (MachineType type : MachineType.values()) {
            int mode = type.getDefaultMode();
            for (MachineRecipe recipe : MachineRecipes.getRecipes(type, mode)) {
                ItemStack input = recipe.getInput();
                if (input.getItem() == ingredient.getItem() && input.getItemDamage() == ingredient.getItemDamage()) {
                    arecipes.add(new CachedMachineRecipe(type, mode, recipe));
                }
            }
        }
    }

    @Override
    public void drawExtras(int recipe) {
        if (recipe < 0 || recipe >= arecipes.size()) return;
        CachedMachineRecipe cached = (CachedMachineRecipe) arecipes.get(recipe);
        MachineType type = cached.getMachineType();

        Minecraft.getMinecraft().fontRenderer
            .drawString(StatCollector.translateToLocal(type.getTitleKey()), 8, 6, 0x404040);

        String operation = operationKey(type);
        if (operation != null) {
            Minecraft.getMinecraft().fontRenderer
                .drawString(StatCollector.translateToLocal(operation), 8, 46, 0x606060);
        }

        Minecraft.getMinecraft().fontRenderer.drawString("->", 88, 22, 0x606060);
        Minecraft.getMinecraft().fontRenderer.drawString("EU: " + cached.energy, 8, 34, 0x404040);
    }

    /** Names the IC2 operation shown on the page, for the two metal machines. */
    private static String operationKey(MachineType type) {
        if (type == MachineType.METAL_EXTRUDER) return "nei.berlit.operation.extruding";
        if (type == MachineType.METAL_ROLLER) return "nei.berlit.operation.rolling";
        return null;
    }
}
