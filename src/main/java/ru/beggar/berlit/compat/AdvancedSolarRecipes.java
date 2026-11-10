package ru.beggar.berlit.compat;

import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.Loader;
import ru.beggar.berlit.recipe.MolecularRecipe;
import ru.beggar.berlit.recipe.MolecularRecipes;

/**
 * Imports every Molecular Transformer recipe registered by Advanced Solar Panels.
 *
 * <p>
 * ASP keeps its recipes in {@code MTRecipeManager.transformerRecipes}; the list is filled during
 * ASP's post-init from its config. This class is only loaded when ASP is present, so ASP stays an
 * optional runtime dependency.
 */
public final class AdvancedSolarRecipes {

    private AdvancedSolarRecipes() {}

    public static int importRecipes() {
        if (!Loader.isModLoaded("AdvancedSolarPanel")) return 0;

        int imported = 0;
        try {
            if (advsolar.utils.MTRecipeManager.transformerRecipes == null) return 0;
            for (advsolar.utils.MTRecipeRecord recipe : advsolar.utils.MTRecipeManager.transformerRecipes) {
                if (recipe == null || recipe.inputStack == null || recipe.outputStack == null) continue;
                ItemStack input = recipe.inputStack.copy();
                ItemStack output = recipe.outputStack.copy();
                MolecularRecipes.register(input, output, Math.max(1, recipe.energyPerOperation));
                imported++;
            }
        } catch (Throwable ignored) {
            // ASP optional: a broken/missing integration must not prevent Berlit from loading.
        }
        return imported;
    }

    /** Vanilla/ASP-independent helper used by the NEI page for a displayable recipe count. */
    public static int recipeCount() {
        return MolecularRecipes.getRecipes()
            .size();
    }

    /** Kept for callers that need a concrete recipe while iterating. */
    public static MolecularRecipe recipeFor(ItemStack stack) {
        return MolecularRecipes.find(stack);
    }
}
