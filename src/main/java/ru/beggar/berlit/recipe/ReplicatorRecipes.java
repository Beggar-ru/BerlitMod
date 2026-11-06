package ru.beggar.berlit.recipe;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.Loader;

/**
 * Matter values used by the replicator.
 *
 * <p>
 * Every item with a value in IC2's UU index can be scanned; that index is built from vanilla,
 * Forge and IC2 recipes (crafting, smelting, macerator, compressor, metal former, ...), so the
 * standard IC2 recipes are supported without a separate table. CraftTweaker can add an item that
 * has no UU value or override the computed one via {@code mods.berlit.Replicator}.
 */
public final class ReplicatorRecipes {

    /** Explicit values added by CraftTweaker, in millibuckets per item. */
    private static final Map<String, ReplicatorPattern> CUSTOM = new LinkedHashMap<String, ReplicatorPattern>();

    private ReplicatorRecipes() {}

    /** Kept for symmetry with the other recipe registries; there are no built-in patterns. */
    public static void init() {
        // IC2's UuIndex is the default source of matter values.
    }

    /**
     * Effective UU matter cost of one item, in millibuckets, or {@code -1} when the item has no
     * known value and can therefore not be scanned.
     */
    public static int getMatterPerItem(ItemStack stack) {
        if (stack == null) return -1;

        Integer custom = getCustomMatter(stack);
        if (custom != null) return custom;

        return ic2Matter(stack);
    }

    /** Builds a pattern for this stack, or returns {@code null} if the item cannot be replicated. */
    public static ReplicatorPattern createPattern(ItemStack stack) {
        int matter = getMatterPerItem(stack);
        if (matter <= 0) return null;
        return new ReplicatorPattern(stack, matter);
    }

    public static Integer getCustomMatter(ItemStack stack) {
        ReplicatorPattern pattern = getCustomPattern(stack);
        return pattern == null ? null : pattern.getMatterPerItem();
    }

    public static ReplicatorPattern getCustomPattern(ItemStack stack) {
        if (stack == null) return null;

        ReplicatorPattern pattern = CUSTOM.get(ReplicatorPattern.key(stack));
        if (pattern == null) {
            // CraftTweaker scripts usually address the plain item, while the scanned stack may
            // carry NBT (enchants, GT tool data, ...). Fall back to item + damage.
            pattern = CUSTOM.get(baseKey(stack));
        }
        return pattern;
    }

    /** Adds or replaces an explicit value. Returns the previous explicit value, or {@code null}. */
    public static Integer setMatter(ItemStack stack, int matterPerItem) {
        if (stack == null || matterPerItem <= 0) return null;

        String key = ReplicatorPattern.key(stack);
        ReplicatorPattern previous = CUSTOM.get(key);
        CUSTOM.put(key, new ReplicatorPattern(stack, matterPerItem));
        return previous == null ? null : previous.getMatterPerItem();
    }

    /** Removes an explicit value. Returns the removed pattern, or {@code null}. */
    public static ReplicatorPattern removeMatter(ItemStack stack) {
        if (stack == null) return null;
        return CUSTOM.remove(ReplicatorPattern.key(stack));
    }

    public static Collection<ReplicatorPattern> getCustomPatterns() {
        return CUSTOM.values();
    }

    private static String baseKey(ItemStack stack) {
        return net.minecraft.item.Item.getIdFromItem(stack.getItem()) + ":" + stack.getItemDamage();
    }

    /**
     * Reads the value IC2 computed for this item. {@code UuIndex.getInBuckets} returns infinity
     * for items that are not in the graph, so anything non-finite or non-positive is rejected.
     */
    private static int ic2Matter(ItemStack stack) {
        if (!Loader.isModLoaded("IC2")) return -1;

        try {
            ItemStack probe = stack.copy();
            probe.stackSize = 1;
            // The UU graph is keyed by item and damage; NBT variants share the base value.
            probe.setTagCompound(null);

            double buckets = ic2.core.uu.UuIndex.instance.getInBuckets(probe);
            if (Double.isNaN(buckets) || Double.isInfinite(buckets) || buckets <= 0.0D) return -1;

            double millibuckets = buckets * 1000.0D;
            if (millibuckets >= Integer.MAX_VALUE) return Integer.MAX_VALUE;
            return Math.max(1, (int) Math.ceil(millibuckets));
        } catch (Throwable ignored) {
            // IC2 can still be initialising while the first world is being loaded.
            return -1;
        }
    }
}
