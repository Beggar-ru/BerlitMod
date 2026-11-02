package ru.beggar.berlit.compat;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

import minetweaker.IUndoableAction;
import minetweaker.MineTweakerAPI;
import minetweaker.api.item.IItemStack;
import minetweaker.api.minecraft.MineTweakerMC;
import ru.beggar.berlit.machine.MachineRecipe;
import ru.beggar.berlit.machine.MachineRecipes;
import ru.beggar.berlit.machine.MachineType;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

/**
 * CraftTweaker integration for the Berlit processing machines.
 *
 * <pre>
 * import mods.berlit.Machines;
 *
 * Machines.addRecipe("macerator", &lt;minecraft:iron_ingot&gt;, &lt;minecraft:iron_ore&gt;, 300);
 * Machines.addRecipe("metal_former", &lt;minecraft:iron_ingot&gt;, &lt;minecraft:iron_ore&gt;, 300, 1);
 * Machines.removeRecipe("macerator", &lt;minecraft:iron_ingot&gt;);
 * </pre>
 */
@ZenClass("mods.berlit.Machines")
public final class MachinesTweaker {

    private MachinesTweaker() {}

    public static void register() {
        MineTweakerAPI.registerClass(MachinesTweaker.class);
    }

    @ZenMethod
    public static void addRecipe(String machine, IItemStack output, IItemStack input, int energy) {
        addRecipe(machine, output, input, energy, 0);
    }

    @ZenMethod
    public static void addRecipe(String machine, IItemStack output, IItemStack input, int energy, int mode) {
        MachineType type = type(machine);
        if (type == null) {
            MineTweakerAPI.logError("Machines.addRecipe: unknown machine '" + machine + "'");
            return;
        }

        ItemStack outputStack = MineTweakerMC.getItemStack(output);
        ItemStack inputStack = MineTweakerMC.getItemStack(input);
        if (outputStack == null || inputStack == null) {
            MineTweakerAPI.logError("Machines.addRecipe: input and output must not be null");
            return;
        }

        MineTweakerAPI.apply(new AddAction(type, mode, inputStack, outputStack, energy));
    }

    @ZenMethod
    public static void removeRecipe(String machine, IItemStack output) {
        MachineType type = type(machine);
        if (type == null) {
            MineTweakerAPI.logError("Machines.removeRecipe: unknown machine '" + machine + "'");
            return;
        }

        ItemStack outputStack = MineTweakerMC.getItemStack(output);
        if (outputStack == null) {
            MineTweakerAPI.logError("Machines.removeRecipe: output must not be null");
            return;
        }

        MineTweakerAPI.apply(new RemoveAction(type, outputStack));
    }

    @ZenMethod
    public static void removeRecipeByInput(String machine, IItemStack input) {
        removeRecipeByInput(machine, input, 0);
    }

    @ZenMethod
    public static void removeRecipeByInput(String machine, IItemStack input, int mode) {
        MachineType type = type(machine);
        if (type == null) {
            MineTweakerAPI.logError("Machines.removeRecipeByInput: unknown machine '" + machine + "'");
            return;
        }

        ItemStack inputStack = MineTweakerMC.getItemStack(input);
        if (inputStack == null) {
            MineTweakerAPI.logError("Machines.removeRecipeByInput: input must not be null");
            return;
        }

        MineTweakerAPI.apply(new RemoveByInputAction(type, mode, inputStack));
    }

    private static MachineType type(String id) {
        if (id == null) return null;
        for (MachineType type : MachineType.values()) {
            if (type.getId()
                .equalsIgnoreCase(id)) return type;
        }
        return null;
    }

    private static final class AddAction implements IUndoableAction {

        private final MachineType type;
        private final int mode;
        private final ItemStack input;
        private final ItemStack output;
        private final int energy;
        private MachineRecipe previous;

        AddAction(MachineType type, int mode, ItemStack input, ItemStack output, int energy) {
            this.type = type;
            this.mode = mode;
            this.input = input;
            this.output = output;
            this.energy = energy;
        }

        @Override
        public void apply() {
            previous = MachineRecipes.find(type, mode, input);
            MachineRecipes.add(type, mode, input, output, energy);
        }

        @Override
        public boolean canUndo() {
            return true;
        }

        @Override
        public void undo() {
            if (previous != null) {
                MachineRecipes.add(type, mode, previous.getInput(), previous.getOutput(), previous.getEnergy());
            } else {
                MachineRecipes.removeByInput(type, mode, input);
            }
        }

        @Override
        public String describe() {
            return "Adding " + type.getId() + " recipe " + input.getDisplayName() + " -> " + output.getDisplayName();
        }

        @Override
        public String describeUndo() {
            return "Removing " + type.getId() + " recipe for " + input.getDisplayName();
        }

        @Override
        public Object getOverrideKey() {
            return "berlit:machine:" + type.getId() + ":" + mode + ":" + input.getUnlocalizedName();
        }
    }

    private static final class RemoveAction implements IUndoableAction {

        private final MachineType type;
        private final ItemStack output;
        private final List<MachineRecipe> removed = new ArrayList<MachineRecipe>();

        RemoveAction(MachineType type, ItemStack output) {
            this.type = type;
            this.output = output;
        }

        @Override
        public void apply() {
            removed.addAll(MachineRecipes.removeByOutput(type, output));
        }

        @Override
        public boolean canUndo() {
            return true;
        }

        @Override
        public void undo() {
            for (MachineRecipe recipe : removed) {
                MachineRecipes.add(type, 0, recipe.getInput(), recipe.getOutput(), recipe.getEnergy());
            }
        }

        @Override
        public String describe() {
            return "Removing " + type.getId() + " recipes for " + output.getDisplayName();
        }

        @Override
        public String describeUndo() {
            return "Restoring " + type.getId() + " recipes for " + output.getDisplayName();
        }

        @Override
        public Object getOverrideKey() {
            return "berlit:machine-out:" + type.getId() + ":" + output.getUnlocalizedName();
        }
    }

    private static final class RemoveByInputAction implements IUndoableAction {

        private final MachineType type;
        private final int mode;
        private final ItemStack input;
        private MachineRecipe removed;

        RemoveByInputAction(MachineType type, int mode, ItemStack input) {
            this.type = type;
            this.mode = mode;
            this.input = input;
        }

        @Override
        public void apply() {
            removed = MachineRecipes.find(type, mode, input);
            MachineRecipes.removeByInput(type, mode, input);
        }

        @Override
        public boolean canUndo() {
            return removed != null;
        }

        @Override
        public void undo() {
            if (removed != null) {
                MachineRecipes.add(type, mode, removed.getInput(), removed.getOutput(), removed.getEnergy());
            }
        }

        @Override
        public String describe() {
            return "Removing " + type.getId() + " recipe for input " + input.getDisplayName();
        }

        @Override
        public String describeUndo() {
            return "Restoring " + type.getId() + " recipe for input " + input.getDisplayName();
        }

        @Override
        public Object getOverrideKey() {
            return "berlit:machine-in:" + type.getId() + ":" + mode + ":" + input.getUnlocalizedName();
        }
    }
}
