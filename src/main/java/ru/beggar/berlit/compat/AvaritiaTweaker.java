package ru.beggar.berlit.compat;

import java.util.Iterator;

import net.minecraft.item.ItemStack;

import fox.spiteful.avaritia.crafting.CompressorManager;
import fox.spiteful.avaritia.crafting.CompressorRecipe;
import minetweaker.MineTweakerAPI;
import minetweaker.api.item.IItemStack;
import minetweaker.api.minecraft.MineTweakerMC;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

/**
 * CraftTweaker integration for the Avaritia neutron compressor.
 *
 * <pre>
 * import mods.berlit.Avaritia;
 *
 * Avaritia.addCompressor(&lt;avaritia:singularity:0&gt;, 900, &lt;minecraft:iron_block&gt;);
 * Avaritia.removeCompressor(&lt;minecraft:iron_block&gt;);
 * </pre>
 */
@ZenClass("mods.berlit.Avaritia")
public final class AvaritiaTweaker {

    private AvaritiaTweaker() {}

    public static void register() {
        MineTweakerAPI.registerClass(AvaritiaTweaker.class);
    }

    @ZenMethod
    public static void addCompressor(IItemStack output, int cost, IItemStack input) {
        ItemStack out = MineTweakerMC.getItemStack(output);
        ItemStack in = MineTweakerMC.getItemStack(input);
        if (out == null || in == null || cost <= 0) {
            MineTweakerAPI.logError("Avaritia.addCompressor: invalid arguments");
            return;
        }
        CompressorManager.addRecipe(out, cost, in);
    }

    @ZenMethod
    public static void removeCompressor(IItemStack input) {
        ItemStack in = MineTweakerMC.getItemStack(input);
        if (in == null) return;

        Iterator<CompressorRecipe> iterator = CompressorManager.getRecipes()
            .iterator();
        while (iterator.hasNext()) {
            CompressorRecipe recipe = iterator.next();
            Object ingredient = recipe.getIngredient();
            if (ingredient instanceof ItemStack && ((ItemStack) ingredient).isItemEqual(in)) {
                iterator.remove();
            }
        }
    }
}
