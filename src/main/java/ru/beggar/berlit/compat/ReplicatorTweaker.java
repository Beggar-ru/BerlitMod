package ru.beggar.berlit.compat;

import net.minecraft.item.ItemStack;

import minetweaker.IUndoableAction;
import minetweaker.MineTweakerAPI;
import minetweaker.api.item.IItemStack;
import minetweaker.api.minecraft.MineTweakerMC;
import ru.beggar.berlit.recipe.ReplicatorPattern;
import ru.beggar.berlit.recipe.ReplicatorRecipes;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

/**
 * CraftTweaker integration for the replicator.
 *
 * <p>
 * Every item that has an IC2 UU value can already be scanned. These methods add items that are
 * missing from IC2's graph or override the computed cost. The amount is in millibuckets of liquid
 * UU matter per single item; a replication operation always makes a whole stack, so it costs
 * {@code amount * stackSize}.
 *
 * <pre>
 * import mods.berlit.Replicator;
 *
 * Replicator.addScan(&lt;minecraft:diamond&gt;, 45);
 * Replicator.addPattern(&lt;minecraft:emerald&gt;, 120);
 * Replicator.removePattern(&lt;minecraft:emerald&gt;);
 * </pre>
 */
@ZenClass("mods.berlit.Replicator")
public final class ReplicatorTweaker {

    private ReplicatorTweaker() {}

    /** Called from {@code Berlit#init} through reflection, only when CraftTweaker is loaded. */
    public static void register() {
        MineTweakerAPI.registerClass(ReplicatorTweaker.class);
    }

    /** Adds (or overrides) the liquid UU matter cost of one item, in millibuckets. */
    @ZenMethod
    public static void addPattern(IItemStack item, int matterPerItem) {
        ItemStack stack = MineTweakerMC.getItemStack(item);
        if (stack == null) {
            MineTweakerAPI.logError("Replicator.addPattern: item must not be null");
            return;
        }
        if (matterPerItem <= 0) {
            MineTweakerAPI.logError("Replicator.addPattern: matter must be greater than zero");
            return;
        }

        MineTweakerAPI.apply(new AddPatternAction(stack, matterPerItem));
    }

    /** Alias that reads naturally when the script only wants to make an item scannable. */
    @ZenMethod
    public static void addScan(IItemStack item, int matterPerItem) {
        addPattern(item, matterPerItem);
    }

    /** Recipe-style alias for {@link #addPattern(IItemStack, int)}. */
    @ZenMethod
    public static void addRecipe(IItemStack item, int matterPerItem) {
        addPattern(item, matterPerItem);
    }

    @ZenMethod
    public static void removePattern(IItemStack item) {
        ItemStack stack = MineTweakerMC.getItemStack(item);
        if (stack == null) {
            MineTweakerAPI.logError("Replicator.removePattern: item must not be null");
            return;
        }
        MineTweakerAPI.apply(new RemovePatternAction(stack));
    }

    @ZenMethod
    public static void removeRecipe(IItemStack item) {
        removePattern(item);
    }

    /** Effective cost in mB per item, or -1 when the item cannot be replicated. */
    @ZenMethod
    public static int getMatter(IItemStack item) {
        ItemStack stack = MineTweakerMC.getItemStack(item);
        return stack == null ? -1 : ReplicatorRecipes.getMatterPerItem(stack);
    }

    private static String stackName(ItemStack stack) {
        return stack.getDisplayName() + " x" + stack.stackSize;
    }

    private static final class AddPatternAction implements IUndoableAction {

        private final ItemStack input;
        private final int matterPerItem;
        private Integer previous;

        AddPatternAction(ItemStack input, int matterPerItem) {
            this.input = input;
            this.matterPerItem = matterPerItem;
        }

        @Override
        public void apply() {
            previous = ReplicatorRecipes.getCustomMatter(input);
            ReplicatorRecipes.setMatter(input, matterPerItem);
        }

        @Override
        public boolean canUndo() {
            return true;
        }

        @Override
        public void undo() {
            if (previous == null) {
                ReplicatorRecipes.removeMatter(input);
            } else {
                ReplicatorRecipes.setMatter(input, previous);
            }
        }

        @Override
        public String describe() {
            return "Adding replicator pattern " + stackName(input) + " for " + matterPerItem + " mB";
        }

        @Override
        public String describeUndo() {
            return "Removing replicator pattern " + stackName(input);
        }

        @Override
        public Object getOverrideKey() {
            return "berlit:replicator:" + ReplicatorPattern.key(input);
        }
    }

    private static final class RemovePatternAction implements IUndoableAction {

        private final ItemStack input;
        private ReplicatorPattern removed;

        RemovePatternAction(ItemStack input) {
            this.input = input;
        }

        @Override
        public void apply() {
            removed = ReplicatorRecipes.removeMatter(input);
        }

        @Override
        public boolean canUndo() {
            return removed != null;
        }

        @Override
        public void undo() {
            if (removed != null) {
                ReplicatorRecipes.setMatter(removed.getStack(), removed.getMatterPerItem());
            }
        }

        @Override
        public String describe() {
            return "Removing replicator pattern " + stackName(input);
        }

        @Override
        public String describeUndo() {
            return "Restoring replicator pattern " + stackName(input);
        }

        @Override
        public Object getOverrideKey() {
            return "berlit:replicator-remove:" + ReplicatorPattern.key(input);
        }
    }
}
