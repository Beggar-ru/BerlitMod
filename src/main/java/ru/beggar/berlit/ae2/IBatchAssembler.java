package ru.beggar.berlit.ae2;

import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.data.IAEItemStack;

/**
 * A crafting machine that can repeat one pattern several times per ME push.
 *
 * <p>
 * AE2 hands a machine the ingredients for exactly one craft and expects one craft back, so a
 * machine that produces more has to pay for it itself and the crafting CPU has to be told.
 * {@link ru.beggar.berlit.mixin.MixinCraftingCPUCluster} does the second half: it caps the batch
 * to what the job still needs and subtracts the extra crafts from the CPU's own task counter.
 */
public interface IBatchAssembler {

    /**
     * Crafts up to {@code maxBatches} extra copies of the pattern.
     *
     * <p>
     * The first craft is already done by the push that precedes this call. Ingredients for the
     * extra crafts are taken from the CPU's reserved job inventory; when that is unavailable the
     * machine falls back to the ME network.
     *
     * @return the number of extra crafts actually performed, 0 when nothing could be made
     */
    int craftExtraBatches(ICraftingPatternDetails details, int maxBatches, IMEInventory<IAEItemStack> cpuInventory,
        BaseActionSource cpuSource);

    /**
     * Number of complete extra crafts currently possible from the CPU's reserved inventory (or
     * the ME network when the CPU inventory cannot supply them). This is the same measurement the
     * batch hook uses before it asks for extra crafts; it is exposed for diagnostics and tests.
     */
    int availableCrafts(ICraftingPatternDetails details, IMEInventory<IAEItemStack> cpuInventory,
        BaseActionSource cpuSource);

    /**
     * True when this machine belongs to the same ME grid as the crafting CPU. The mixin uses this
     * to avoid batching a pattern through an assembler that is only physically nearby but cannot
     * deliver its output back to the CPU.
     */
    default boolean isOnGrid(IGrid grid) {
        return true;
    }
}
