package ru.beggar.berlit.mixin;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.crafting.CraftingJob;
import appeng.crafting.CraftingTreeNode;
import appeng.crafting.CraftingTreeProcess;
import ru.beggar.berlit.ae2.AvaritiaPatternDetails;

/** Lets AE2 build a 9x9 crafting inventory for Berlit's Avaritia patterns. */
@Mixin(value = CraftingTreeProcess.class, remap = false)
public class MixinCraftingTreeProcess {

    private static final ThreadLocal<ICraftingPatternDetails> BERLIT_PATTERN = new ThreadLocal<ICraftingPatternDetails>();

    @Inject(method = "<init>", at = @At("HEAD"))
    private void berlit$head(ICraftingGrid cc, CraftingJob job, ICraftingPatternDetails details, CraftingTreeNode node,
        int depth, CallbackInfo ci) {
        BERLIT_PATTERN.set(details);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void berlit$tail(ICraftingGrid cc, CraftingJob job, ICraftingPatternDetails details, CraftingTreeNode node,
        int depth, CallbackInfo ci) {
        BERLIT_PATTERN.remove();
    }

    @Redirect(
        method = "<init>",
        at = @At(value = "NEW", target = "net/minecraft/inventory/InventoryCrafting"),
        remap = false)
    private InventoryCrafting berlit$inventory(Container container, int width, int height) {
        ICraftingPatternDetails details = BERLIT_PATTERN.get();
        if (details instanceof AvaritiaPatternDetails) {
            return new InventoryCrafting(container, 9, 9);
        }
        return new InventoryCrafting(container, width, height);
    }
}
