package ru.beggar.berlit.mixin;

import java.util.Iterator;
import java.util.List;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import appeng.helpers.DualityInterface;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.tile.inventory.AppEngInternalInventory;

/**
 * AE2 hardcodes a 9-entry Boolean array in updateCraftingList and asserts that it matches the
 * pattern inventory. Our expanded interfaces have 18/27/36 slots, so patterns after slot 9 were
 * never registered as crafting options. Reimplement the method for non-default inventories.
 */
@Mixin(value = DualityInterface.class, remap = false)
public class MixinDualityInterface {

    @Shadow
    private AppEngInternalInventory patterns;

    @Shadow
    private List<ICraftingPatternDetails> craftingList;

    @Shadow
    private AENetworkProxy gridProxy;

    @Shadow
    private void addToCraftingList(ItemStack stack) {}

    @Inject(method = "updateCraftingList", at = @At("HEAD"), cancellable = true)
    private void berlit$updateLargeCraftingList(CallbackInfo callback) {
        int size = patterns == null ? 0 : patterns.getSizeInventory();
        if (size == DualityInterface.NUMBER_OF_PATTERN_SLOTS) return;

        callback.cancel();
        if (!gridProxy.isReady()) return;

        boolean[] matched = new boolean[size];
        if (craftingList != null) {
            Iterator<ICraftingPatternDetails> iterator = craftingList.iterator();
            while (iterator.hasNext()) {
                ICraftingPatternDetails details = iterator.next();
                boolean found = false;
                for (int slot = 0; slot < size; slot++) {
                    ItemStack stack = patterns.getStackInSlot(slot);
                    if (details.getPattern() == stack) {
                        matched[slot] = true;
                        found = true;
                    }
                }
                if (!found) iterator.remove();
            }
        }

        for (int slot = 0; slot < size; slot++) {
            if (!matched[slot]) addToCraftingList(patterns.getStackInSlot(slot));
        }

        try {
            gridProxy.getGrid()
                .postEvent(new MENetworkCraftingPatternChange((ICraftingProvider) this, gridProxy.getNode()));
        } catch (GridAccessException ignored) {}
    }
}
