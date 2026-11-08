package ru.beggar.berlit.compat;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

import minetweaker.IUndoableAction;
import minetweaker.MineTweakerAPI;
import minetweaker.api.item.IItemStack;
import minetweaker.api.minecraft.MineTweakerMC;
import ru.beggar.berlit.farm.FarmRecipe;
import ru.beggar.berlit.farm.FarmRecipes;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

/**
 * CraftTweaker integration for the molecular farm.
 *
 * <pre>
 * import mods.berlit.MolecularFarm;
 * MolecularFarm.addRecipe(&lt;minecraft:wheat_seeds&gt;, &lt;minecraft:wheat&gt;);
 * MolecularFarm.addRecipe(&lt;minecraft:pumpkin_seeds&gt;, [&lt;minecraft:pumpkin&gt;, &lt;minecraft:pumpkin_seeds&gt;]);
 * MolecularFarm.removeRecipe(&lt;minecraft:wheat_seeds&gt;);
 * </pre>
 */
@ZenClass("mods.berlit.MolecularFarm")
public final class MolecularFarmTweaker {

    private MolecularFarmTweaker() {}

    public static void register() {
        MineTweakerAPI.registerClass(MolecularFarmTweaker.class);
    }

    @ZenMethod
    public static void addRecipe(IItemStack seed, IItemStack output) {
        addRecipe(seed, new IItemStack[] { output });
    }

    @ZenMethod
    public static void addRecipe(IItemStack seed, IItemStack[] outputs) {
        ItemStack seedStack = MineTweakerMC.getItemStack(seed);
        if (seedStack == null || outputs == null || outputs.length == 0) {
            MineTweakerAPI.logError("MolecularFarm.addRecipe: seed and at least one output are required");
            return;
        }

        List<ItemStack> outputStacks = new ArrayList<ItemStack>();
        for (IItemStack output : outputs) {
            ItemStack stack = MineTweakerMC.getItemStack(output);
            if (stack != null) outputStacks.add(stack);
        }
        if (outputStacks.isEmpty()) {
            MineTweakerAPI.logError("MolecularFarm.addRecipe: no valid outputs");
            return;
        }

        MineTweakerAPI.apply(new AddAction(seedStack, outputStacks));
    }

    @ZenMethod
    public static void removeRecipe(IItemStack seed) {
        ItemStack seedStack = MineTweakerMC.getItemStack(seed);
        if (seedStack == null) {
            MineTweakerAPI.logError("MolecularFarm.removeRecipe: seed must not be null");
            return;
        }
        MineTweakerAPI.apply(new RemoveAction(seedStack));
    }

    private static final class AddAction implements IUndoableAction {

        private final ItemStack seed;
        private final List<ItemStack> outputs;
        private FarmRecipe previous;

        AddAction(ItemStack seed, List<ItemStack> outputs) {
            this.seed = seed;
            this.outputs = outputs;
        }

        @Override
        public void apply() {
            previous = FarmRecipes.find(seed);
            FarmRecipes.register(seed, outputs);
        }

        @Override
        public boolean canUndo() {
            return true;
        }

        @Override
        public void undo() {
            if (previous != null) {
                FarmRecipes.register(previous.getInput(), previous.getOutputsView());
            } else {
                FarmRecipes.remove(seed);
            }
        }

        @Override
        public String describe() {
            return "Adding molecular farm recipe for " + seed.getDisplayName();
        }

        @Override
        public String describeUndo() {
            return "Removing molecular farm recipe for " + seed.getDisplayName();
        }

        @Override
        public Object getOverrideKey() {
            return "berlit:farm:" + seed.getItem()
                .getUnlocalizedName() + ":" + seed.getItemDamage();
        }
    }

    private static final class RemoveAction implements IUndoableAction {

        private final ItemStack seed;
        private FarmRecipe removed;

        RemoveAction(ItemStack seed) {
            this.seed = seed;
        }

        @Override
        public void apply() {
            removed = FarmRecipes.find(seed);
            FarmRecipes.remove(seed);
        }

        @Override
        public boolean canUndo() {
            return removed != null;
        }

        @Override
        public void undo() {
            if (removed != null) FarmRecipes.register(removed.getInput(), removed.getOutputsView());
        }

        @Override
        public String describe() {
            return "Removing molecular farm recipe for " + seed.getDisplayName();
        }

        @Override
        public String describeUndo() {
            return "Restoring molecular farm recipe for " + seed.getDisplayName();
        }

        @Override
        public Object getOverrideKey() {
            return "berlit:farm-remove:" + seed.getItem()
                .getUnlocalizedName() + ":" + seed.getItemDamage();
        }
    }
}
