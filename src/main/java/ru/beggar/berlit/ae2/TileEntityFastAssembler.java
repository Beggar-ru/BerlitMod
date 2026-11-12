package ru.beggar.berlit.ae2;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.util.Platform;
import ru.beggar.berlit.Berlit;
import ru.beggar.berlit.Config;

/**
 * Fast 3x3 molecular assembler: standard AE2 patterns, instant crafts.
 *
 * <p>
 * A single AE2 push normally means exactly one craft. This machine repeats the pushed pattern from
 * the crafting CPU's reserved inventory, and {@link BatchJobPrecraft} completes the job's
 * intermediate tasks before the final push. Together they let one CPU job run as one batch: all
 * planks for a workbench order are made first, then the workbenches themselves are produced in the
 * same tick. {@link Config#fastAssemblerFullTask} and {@link Config#fastAssemblerWholeJob} can turn
 * the acceleration off, and {@link Config#fastAssemblerBatch} caps the batch size when full-task
 * mode is disabled.
 */
public class TileEntityFastAssembler extends TileEntityAvaritiaAssembler implements IBatchAssembler {

    /** Marks the tick of the last push so a stray batch call cannot craft out of nowhere. */
    private long lastPushTick = Long.MIN_VALUE;

    @Override
    protected ItemStack machineStack() {
        return new ItemStack(Berlit.fastAssembler);
    }

    @Override
    public String getInventoryName() {
        return "tile.berlit.fast_assembler.name";
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (stack == null || worldObj == null || !(stack.getItem() instanceof ICraftingPatternItem)) return false;
        ICraftingPatternDetails details = ((ICraftingPatternItem) stack.getItem()).getPatternForItem(stack, worldObj);
        // Only ordinary 3x3 crafting patterns belong here; processing patterns and the 9x9
        // Avaritia patterns have their own machines.
        return details != null && details.isCraftable() && !(details instanceof AvaritiaPatternDetails);
    }

    @Override
    public boolean pushPattern(ICraftingPatternDetails details, InventoryCrafting crafting) {
        if (worldObj == null || worldObj.isRemote) return false;
        if (details == null || !details.isCraftable() || crafting == null) return false;

        ItemStack result = details.getOutput(crafting, worldObj);
        if (result == null) return false;

        // Queue every output of the pattern, not just the one getOutput validates against; custom
        // crafting patterns may have more than one result. Standard AE2 patterns have exactly one.
        IAEItemStack[] outputs = details.getCondensedOutputs();
        boolean queued = false;
        if (outputs != null && outputs.length > 0) {
            for (IAEItemStack output : outputs) {
                if (output == null) continue;
                ItemStack stack = output.getItemStack();
                if (stack == null) continue;
                queueOutput(stack.copy());
                queued = true;
            }
        }
        if (!queued) queueOutput(result);
        queueContainerItems(crafting);
        lastPushTick = worldObj.getTotalWorldTime();
        return true;
    }

    @Override
    public int craftExtraBatches(ICraftingPatternDetails details, int maxBatches,
        IMEInventory<IAEItemStack> cpuInventory, BaseActionSource cpuSource) {
        if (details == null || maxBatches <= 0 || worldObj == null || worldObj.isRemote) return 0;
        if (!details.isCraftable()) return 0;
        // Only right after this machine was pushed; the CPU calls us in the same tick.
        if (lastPushTick != worldObj.getTotalWorldTime()) return 0;
        lastPushTick = Long.MIN_VALUE;

        IAEItemStack[] produced = details.getCondensedOutputs();
        if (produced == null || produced.length == 0) return 0;

        IAEItemStack[] required = details.getCondensedInputs();
        if (required == null || required.length == 0) return 0;

        BatchInput input = resolveInputs(details, cpuInventory, cpuSource);
        if (input == null) return 0;

        int crafts = Math.min(maxBatches, input.crafts);
        if (crafts <= 0) return 0;

        // Pay for all extra crafts first, then queue one output batch per produced stack type.
        for (IAEItemStack need : required) {
            if (need == null) continue;
            IAEItemStack take = need.copy();
            take.setStackSize(need.getStackSize() * crafts);
            input.inventory.extractItems(take, Actionable.MODULATE, input.source);
        }

        for (IAEItemStack output : produced) {
            if (output == null) continue;
            ItemStack result = output.getItemStack();
            if (result == null) continue;
            queueOutput(result.copy(), crafts);
        }
        if (cpuInventory != null) {
            queueContainerItems(details, crafts, cpuInventory, cpuSource);
        }
        return crafts;
    }

    /** Returns empty buckets and similar container items for the craft that AE2 just pushed. */
    private void queueContainerItems(InventoryCrafting crafting) {
        for (int slot = 0; slot < crafting.getSizeInventory(); slot++) {
            ItemStack input = crafting.getStackInSlot(slot);
            if (input == null) continue;
            ItemStack container = Platform.getContainerItem(input.copy());
            if (container != null) queueOutput(container);
        }
    }

    /**
     * Container items for the extra crafts cannot travel through AE2's waiting list (the CPU only
     * expects the pattern's normal outputs from us), so they are put straight into the CPU's
     * reserved inventory. They are stored back into the ME network when the job finishes.
     */
    private void queueContainerItems(ICraftingPatternDetails details, int crafts,
        IMEInventory<IAEItemStack> cpuInventory, BaseActionSource cpuSource) {
        IAEItemStack[] slots = details.getInputs();
        if (slots == null) return;

        BaseActionSource source = cpuSource == null ? new MachineSource(this) : cpuSource;
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
            cpuInventory.injectItems(produced, Actionable.MODULATE, source);
        }
    }

    @Override
    public int availableCrafts(ICraftingPatternDetails details, IMEInventory<IAEItemStack> cpuInventory,
        BaseActionSource cpuSource) {
        if (details == null || worldObj == null || worldObj.isRemote) return 0;
        if (!details.isCraftable()) return 0;

        IAEItemStack[] required = details.getCondensedInputs();
        IAEItemStack[] produced = details.getCondensedOutputs();
        if (required == null || required.length == 0 || produced == null || produced.length == 0) return 0;

        BatchInput input = resolveInputs(details, cpuInventory, cpuSource);
        return input == null ? 0 : input.crafts;
    }

    @Override
    public boolean isOnGrid(IGrid grid) {
        return grid == null || networkGrid() == grid;
    }

    /**
     * Chooses the inventory that can supply the most complete crafts. The CPU's reserved
     * inventory usually has the current craft, while the ME network can have the rest of a large
     * job; neither source alone is always complete.
     */
    private BatchInput resolveInputs(ICraftingPatternDetails details, IMEInventory<IAEItemStack> cpuInventory,
        BaseActionSource cpuSource) {
        IAEItemStack[] required = details.getCondensedInputs();
        if (required == null || required.length == 0) return null;

        BatchInput best = measure(required, cpuInventory, cpuSource);

        IGrid grid = networkGrid();
        if (grid != null) {
            IStorageGrid storage = grid.getCache(IStorageGrid.class);
            if (storage != null) {
                IMEMonitor<IAEItemStack> network = storage.getItemInventory();
                BatchInput fromNetwork = measure(required, network, new MachineSource(this));
                if (fromNetwork != null && (best == null || fromNetwork.crafts > best.crafts)) {
                    best = fromNetwork;
                }
            }
        }
        return best;
    }

    /**
     * Counts complete crafts. Note that {@link IMEInventory#extractItems} never returns more than
     * the requested stack size, so the probe has to request a huge stack instead of one recipe's
     * worth; otherwise every batch would look like a single craft.
     */
    private BatchInput measure(IAEItemStack[] required, IMEInventory<IAEItemStack> inventory, BaseActionSource source) {
        if (inventory == null) return null;
        if (source == null) source = new MachineSource(this);

        long crafts = Integer.MAX_VALUE;
        for (IAEItemStack input : required) {
            if (input == null) continue;
            long perCraft = input.getStackSize();
            if (perCraft <= 0) continue;

            IAEItemStack probe = input.copy();
            probe.setStackSize(Integer.MAX_VALUE);
            IAEItemStack available = inventory.extractItems(probe, Actionable.SIMULATE, source);
            long count = available == null ? 0 : available.getStackSize();

            crafts = Math.min(crafts, count / perCraft);
            if (crafts <= 0) return null;
        }
        if (crafts == Integer.MAX_VALUE) return null;
        return new BatchInput(inventory, source, (int) Math.min(Integer.MAX_VALUE, crafts));
    }

    private static final class BatchInput {

        private final IMEInventory<IAEItemStack> inventory;
        private final BaseActionSource source;
        private final int crafts;

        private BatchInput(IMEInventory<IAEItemStack> inventory, BaseActionSource source, int crafts) {
            this.inventory = inventory;
            this.source = source;
            this.crafts = crafts;
        }
    }
}
