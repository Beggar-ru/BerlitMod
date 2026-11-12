package ru.beggar.berlit.ae2;

import appeng.api.networking.crafting.ICraftingPatternDetails;

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
     * The first craft is already done by the push that precedes this call; the ingredients for
     * every extra craft are pulled from the ME network this machine is connected to.
     *
     * @return the number of extra crafts actually performed, 0 when nothing could be made
     */
    int craftExtraBatches(ICraftingPatternDetails details, int maxBatches);
}
