package ru.beggar.berlit.mixin;

import net.minecraft.inventory.InventoryCrafting;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import appeng.api.networking.crafting.ICraftingMedium;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.ContainerNull;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import ru.beggar.berlit.ae2.AvaritiaPatternDetails;

/** Passes a real 9x9 inventory to Berlit's Avaritia assembling machine. */
@Mixin(value = CraftingCPUCluster.class, remap = false)
public class MixinCraftingCPUCluster {

    @Redirect(
        method = "executeCrafting",
        at = @At(value = "INVOKE", target = "appeng/api/networking/crafting/ICraftingMedium.pushPattern"),
        remap = false)
    private boolean berlit$pushPattern(ICraftingMedium medium, ICraftingPatternDetails details,
        InventoryCrafting crafting) {
        if (details instanceof AvaritiaPatternDetails) {
            InventoryCrafting big = new InventoryCrafting(new ContainerNull(), 9, 9);
            IAEItemStack[] inputs = details.getInputs();
            for (int i = 0; i < inputs.length && i < 81; i++) {
                big.setInventorySlotContents(i, inputs[i] == null ? null : inputs[i].getItemStack());
            }
            return medium.pushPattern(details, big);
        }
        return medium.pushPattern(details, crafting);
    }
}
