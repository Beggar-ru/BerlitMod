package ru.beggar.berlit.ae2;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.data.IAEItemStack;
import appeng.util.Platform;

/**
 * Executes the intermediate steps of an AE2 crafting job directly against the crafting CPU's
 * reserved inventory.
 *
 * <p>
 * AE2 normally pushes one pattern, waits for the result to travel back through the ME network and
 * only then starts the task that consumes it. That turns a multi-step job (logs &rarr; planks &rarr;
 * workbenches) into several separate rounds. The fast assembler can already repeat the last
 * pattern of a job; this helper removes the waiting between the rounds by crafting every
 * intermediate task whose ingredients are already reserved by the CPU. The produced items are
 * injected into the CPU inventory, so the final task becomes ready in the same tick and is then
 * pushed to the assembler as one batch.
 *
 * <p>
 * The task that produces the job's final output is deliberately left alone: that output has to
 * travel through the machine and the ME network so the CPU can finish the job and hand the result
 * to the requester.
 */
public final class BatchJobPrecraft {

    /** One remaining task of a crafting CPU job. */
    public interface Task {

        ICraftingPatternDetails pattern();

        long remaining();

        /** Removes {@code crafts} completed crafts from the task counter. */
        void consume(long crafts);
    }

    /** Decides whether a task can be handled by a fast assembler on the CPU's grid. */
    public interface Gate {

        boolean canCraft(ICraftingPatternDetails pattern);
    }

    private BatchJobPrecraft() {}

    /**
     * Crafts every intermediate task that can be completed from {@code inventory}, in dependency
     * order, until no further progress can be made.
     *
     * @return the number of pre-crafts performed (not the number of produced items)
     */
    public static long run(IMEInventory<IAEItemStack> inventory, BaseActionSource source, List<Task> tasks,
        IAEItemStack finalOutput, Gate gate, int maxPasses) {
        if (inventory == null || tasks == null || tasks.isEmpty()) return 0;

        long total = 0;
        List<IAEItemStack> needed = collectNeeded(tasks);
        int passes = Math.max(1, maxPasses);
        for (int pass = 0; pass < passes; pass++) {
            boolean progress = false;

            for (int i = 0; i < tasks.size(); i++) {
                Task task = tasks.get(i);
                if (task == null) continue;

                long remaining = task.remaining();
                if (remaining <= 0) continue;

                ICraftingPatternDetails details = task.pattern();
                if (!isIntermediate(details, finalOutput, needed)) continue;

                IAEItemStack[] required = details.getCondensedInputs();
                long crafts = possibleCrafts(inventory, source, required);
                if (crafts <= 0) continue;
                if (crafts > remaining) crafts = remaining;

                // The gate is last: it may have to look up the pattern's mediums in the grid.
                if (gate != null && !gate.canCraft(details)) continue;

                if (!consume(inventory, source, required, crafts)) continue;
                produce(inventory, source, details.getCondensedOutputs(), crafts);
                produceContainerItems(inventory, source, details, crafts);

                task.consume(crafts);
                total += crafts;
                progress = true;
            }

            if (!progress) break;
        }
        return total;
    }

    /** All ingredient types consumed by the remaining tasks. */
    private static List<IAEItemStack> collectNeeded(List<Task> tasks) {
        List<IAEItemStack> needed = new ArrayList<IAEItemStack>();
        for (Task task : tasks) {
            if (task == null || task.remaining() <= 0) continue;
            ICraftingPatternDetails details = task.pattern();
            if (details == null) continue;
            IAEItemStack[] inputs = details.getCondensedInputs();
            if (inputs == null) continue;
            for (IAEItemStack input : inputs) {
                if (input != null) needed.add(input);
            }
        }
        return needed;
    }

    /**
     * True for a task that only produces ingredients for another task. The root task (the one
     * producing the job's final output) is left for the normal AE2 push so the CPU can deliver it
     * to the requester. The dependency check catches the root even if the requested stack carries
     * NBT that the recipe output does not.
     */
    private static boolean isIntermediate(ICraftingPatternDetails details, IAEItemStack finalOutput,
        List<IAEItemStack> needed) {
        if (details == null || !details.isCraftable()) return false;
        // Avaritia 9x9 patterns have their own assembler and are never pushed through the 3x3 one.
        if (details instanceof AvaritiaPatternDetails) return false;
        if (produces(details, finalOutput)) return false;

        IAEItemStack[] outputs = details.getCondensedOutputs();
        if (outputs == null) return false;
        // Nothing consumes this pattern's output: it is a root task, not a pre-craft.
        for (IAEItemStack output : outputs) {
            if (output != null && containsType(needed, output)) return true;
        }
        return false;
    }

    private static boolean containsType(List<IAEItemStack> stacks, IAEItemStack type) {
        for (IAEItemStack stack : stacks) {
            if (stack != null && stack.isSameType(type)) return true;
        }
        return false;
    }

    private static boolean produces(ICraftingPatternDetails details, IAEItemStack target) {
        if (target == null) return false;
        IAEItemStack[] outputs = details.getCondensedOutputs();
        if (outputs == null) return false;
        for (IAEItemStack output : outputs) {
            if (output != null && output.isSameType(target)) return true;
        }
        return false;
    }

    private static long possibleCrafts(IMEInventory<IAEItemStack> inventory, BaseActionSource source,
        IAEItemStack[] required) {
        if (required == null || required.length == 0) return 0;

        long crafts = Long.MAX_VALUE;
        for (IAEItemStack input : required) {
            if (input == null) continue;
            long perCraft = input.getStackSize();
            if (perCraft <= 0) continue;

            long available = countAvailable(inventory, source, input);
            crafts = Math.min(crafts, available / perCraft);
            if (crafts <= 0) return 0;
        }
        return crafts == Long.MAX_VALUE ? 0 : crafts;
    }

    /**
     * Counts the whole available amount of {@code stack}. A normal extraction request is capped at
     * the request size, so a probe has to ask for far more than one craft's worth.
     */
    private static long countAvailable(IMEInventory<IAEItemStack> inventory, BaseActionSource source,
        IAEItemStack stack) {
        IAEItemStack probe = stack.copy();
        probe.setStackSize(Integer.MAX_VALUE);
        IAEItemStack available = inventory.extractItems(probe, Actionable.SIMULATE, source);
        return available == null ? 0 : available.getStackSize();
    }

    private static boolean consume(IMEInventory<IAEItemStack> inventory, BaseActionSource source,
        IAEItemStack[] required, long crafts) {
        List<IAEItemStack> taken = new ArrayList<IAEItemStack>(required.length);
        for (IAEItemStack input : required) {
            if (input == null) continue;

            IAEItemStack need = input.copy();
            need.setStackSize(input.getStackSize() * crafts);
            IAEItemStack got = inventory.extractItems(need, Actionable.MODULATE, source);

            if (got == null || got.getStackSize() < need.getStackSize()) {
                // The simulation promised this would fit; if another inventory changed under us,
                // put back what was already taken instead of producing items for free.
                if (got != null && got.getStackSize() > 0) taken.add(got);
                for (IAEItemStack restore : taken) {
                    inventory.injectItems(restore, Actionable.MODULATE, source);
                }
                return false;
            }
            taken.add(got);
        }
        return true;
    }

    private static void produce(IMEInventory<IAEItemStack> inventory, BaseActionSource source, IAEItemStack[] outputs,
        long crafts) {
        if (outputs == null) return;
        for (IAEItemStack output : outputs) {
            if (output == null) continue;
            IAEItemStack produced = output.copy();
            produced.setStackSize(output.getStackSize() * crafts);
            inventory.injectItems(produced, Actionable.MODULATE, source);
        }
    }

    /**
     * Mirrors AE2's container-item handling for intermediate crafts. It matters for recipes that
     * use buckets or similar items: the empty container has to stay in the CPU inventory.
     */
    private static void produceContainerItems(IMEInventory<IAEItemStack> inventory, BaseActionSource source,
        ICraftingPatternDetails details, long crafts) {
        IAEItemStack[] slots = details.getInputs();
        if (slots == null) return;

        for (IAEItemStack slot : slots) {
            if (slot == null) continue;
            ItemStack input = slot.getItemStack();
            if (input == null) continue;

            ItemStack container = Platform.getContainerItem(input.copy());
            if (container == null) continue;

            IAEItemStack produced = AEApi.instance()
                .storage()
                .createItemStack(container);
            if (produced == null) continue;
            produced.setStackSize(produced.getStackSize() * crafts);
            inventory.injectItems(produced, Actionable.MODULATE, source);
        }
    }
}
