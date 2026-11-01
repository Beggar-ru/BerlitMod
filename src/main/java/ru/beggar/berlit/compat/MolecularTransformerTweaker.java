package ru.beggar.berlit.compat;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

import minetweaker.IUndoableAction;
import minetweaker.MineTweakerAPI;
import minetweaker.api.item.IItemStack;
import minetweaker.api.minecraft.MineTweakerMC;
import ru.beggar.berlit.recipe.MolecularRecipe;
import ru.beggar.berlit.recipe.MolecularRecipes;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

/**
 * CraftTweaker (MineTweaker3) integration.
 *
 * <pre>
 * import mods.berlit.MolecularTransformer;
 *
 * MolecularTransformer.addRecipe(&lt;minecraft:diamond&gt;, &lt;minecraft:emerald&gt;, 2000);
 * MolecularTransformer.removeRecipe(&lt;minecraft:diamond&gt;);
 * </pre>
 */
@ZenClass("mods.berlit.MolecularTransformer")
public final class MolecularTransformerTweaker {

    private MolecularTransformerTweaker() {}

    /** Called from {@code Berlit#init} through reflection, only when CraftTweaker is loaded. */
    public static void register() {
        MineTweakerAPI.registerClass(MolecularTransformerTweaker.class);
    }

    @ZenMethod
    public static void addRecipe(IItemStack output, IItemStack input, int energy) {
        ItemStack outputStack = MineTweakerMC.getItemStack(output);
        ItemStack inputStack = MineTweakerMC.getItemStack(input);

        if (outputStack == null || inputStack == null) {
            MineTweakerAPI.logError("MolecularTransformer.addRecipe: input and output must not be null");
            return;
        }

        MineTweakerAPI.apply(new AddRecipeAction(inputStack, outputStack, energy));
    }

    @ZenMethod
    public static void removeRecipe(IItemStack output) {
        ItemStack outputStack = MineTweakerMC.getItemStack(output);
        if (outputStack == null) {
            MineTweakerAPI.logError("MolecularTransformer.removeRecipe: output must not be null");
            return;
        }
        MineTweakerAPI.apply(new RemoveRecipeAction(outputStack));
    }

    @ZenMethod
    public static void removeRecipeByInput(IItemStack input) {
        ItemStack inputStack = MineTweakerMC.getItemStack(input);
        if (inputStack == null) {
            MineTweakerAPI.logError("MolecularTransformer.removeRecipeByInput: input must not be null");
            return;
        }
        MineTweakerAPI.apply(new RemoveRecipeByInputAction(inputStack));
    }

    private static String stackName(ItemStack stack) {
        return stack.getDisplayName() + " x" + stack.stackSize;
    }

    private static final class AddRecipeAction implements IUndoableAction {

        private final ItemStack input;
        private final ItemStack output;
        private final int energy;
        private MolecularRecipe previous;

        AddRecipeAction(ItemStack input, ItemStack output, int energy) {
            this.input = input;
            this.output = output;
            this.energy = energy;
        }

        @Override
        public void apply() {
            previous = MolecularRecipes.find(input);
            MolecularRecipes.register(input, output, energy);
        }

        @Override
        public boolean canUndo() {
            return true;
        }

        @Override
        public void undo() {
            if (previous != null) {
                MolecularRecipes.register(previous.getInput(), previous.getOutput(), previous.getEnergy());
            } else {
                MolecularRecipes.removeByInput(input);
            }
        }

        @Override
        public String describe() {
            return "Adding molecular transformer recipe " + stackName(input) + " -> " + stackName(output);
        }

        @Override
        public String describeUndo() {
            return "Removing molecular transformer recipe " + stackName(input) + " -> " + stackName(output);
        }

        @Override
        public Object getOverrideKey() {
            return "berlit:molecular:" + input.getItem()
                .getUnlocalizedName() + ":" + input.getItemDamage();
        }
    }

    private static final class RemoveRecipeAction implements IUndoableAction {

        private final ItemStack output;
        private final List<MolecularRecipe> removed = new ArrayList<MolecularRecipe>();

        RemoveRecipeAction(ItemStack output) {
            this.output = output;
        }

        @Override
        public void apply() {
            removed.addAll(MolecularRecipes.removeByOutput(output));
        }

        @Override
        public boolean canUndo() {
            return true;
        }

        @Override
        public void undo() {
            for (MolecularRecipe recipe : removed) {
                MolecularRecipes.register(recipe.getInput(), recipe.getOutput(), recipe.getEnergy());
            }
        }

        @Override
        public String describe() {
            return "Removing molecular transformer recipes for " + stackName(output);
        }

        @Override
        public String describeUndo() {
            return "Restoring molecular transformer recipes for " + stackName(output);
        }

        @Override
        public Object getOverrideKey() {
            return "berlit:molecular-output:" + output.getItem()
                .getUnlocalizedName() + ":" + output.getItemDamage();
        }
    }

    private static final class RemoveRecipeByInputAction implements IUndoableAction {

        private final ItemStack input;
        private MolecularRecipe removed;

        RemoveRecipeByInputAction(ItemStack input) {
            this.input = input;
        }

        @Override
        public void apply() {
            removed = MolecularRecipes.find(input);
            MolecularRecipes.removeByInput(input);
        }

        @Override
        public boolean canUndo() {
            return true;
        }

        @Override
        public void undo() {
            if (removed != null) {
                MolecularRecipes.register(removed.getInput(), removed.getOutput(), removed.getEnergy());
            }
        }

        @Override
        public String describe() {
            return "Removing molecular transformer recipe for input " + stackName(input);
        }

        @Override
        public String describeUndo() {
            return "Restoring molecular transformer recipe for input " + stackName(input);
        }

        @Override
        public Object getOverrideKey() {
            return "berlit:molecular-input:" + input.getItem()
                .getUnlocalizedName() + ":" + input.getItemDamage();
        }
    }
}
