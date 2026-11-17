package ru.beggar.berlit.recipe;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.item.ItemStack;

/**
 * Matter values used by the Berlit replicator.
 *
 * <p>
 * Berlit has no built-in patterns. The only accepted recipes are explicit patterns added through
 * CraftTweaker/MineTweaker: {@code mods.berlit.Replicator.addPattern/addScan/addRecipe}. IC2's UU
 * graph is intentionally ignored, so an item that is not in the user's script is never scanned.
 */
public final class ReplicatorRecipes {

    /** Explicit values added by CraftTweaker, in millibuckets per item. */
    private static final Map<String, ReplicatorPattern> CUSTOM = new LinkedHashMap<String, ReplicatorPattern>();

    private ReplicatorRecipes() {}

    /** Kept for symmetry with the other recipe registries; there are no built-in patterns. */
    public static void init() {
        // All patterns come from CraftTweaker/MineTweaker.
    }

    /**
     * Effective UU matter cost of one item, in millibuckets, or {@code -1} when the item has no
     * explicit pattern and can therefore not be scanned.
     */
    public static int getMatterPerItem(ItemStack stack) {
        Integer custom = getCustomMatter(stack);
        return custom == null ? -1 : custom;
    }

    /** Builds a pattern for this stack, or returns {@code null} if the item cannot be replicated. */
    public static ReplicatorPattern createPattern(ItemStack stack) {
        ReplicatorPattern custom = getCustomPattern(stack);
        return custom == null ? null : new ReplicatorPattern(custom.getStack(), custom.getMatterPerItem());
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
}
