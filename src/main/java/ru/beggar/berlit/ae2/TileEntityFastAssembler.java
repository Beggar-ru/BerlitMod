package ru.beggar.berlit.ae2;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import ru.beggar.berlit.Berlit;
import ru.beggar.berlit.Config;

/**
 * Fast 3x3 molecular assembler: standard AE2 patterns, instant crafts.
 *
 * <p>
 * On top of the plain assembler it can repeat the pushed pattern up to
 * {@link Config#fastAssemblerBatch} times, paying for the extra crafts from the ME network. That
 * turns one push per tick into a whole stack per tick, which is what makes it worth using over a
 * regular assembler behind an accelerated interface.
 */
public class TileEntityFastAssembler extends TileEntityAvaritiaAssembler implements IBatchAssembler {

    /** Marks the tick of the last push so a stray batch call cannot craft out of nowhere. */
    private long lastPushTick = Long.MIN_VALUE;

    @Override
    protected ItemStack machineStack() {
        return new ItemStack(Berlit.fastAssembler);
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return stack != null && stack.getItem() instanceof ICraftingPatternItem;
    }

    @Override
    public boolean pushPattern(ICraftingPatternDetails details, InventoryCrafting crafting) {
        if (worldObj == null || worldObj.isRemote) return false;

        ItemStack result = details.getOutput(crafting, worldObj);
        if (result == null) return false;

        queueOutput(result);
        lastPushTick = worldObj.getTotalWorldTime();
        return true;
    }

    @Override
    public int craftExtraBatches(ICraftingPatternDetails details, int maxBatches) {
        if (details == null || maxBatches <= 0 || worldObj == null || worldObj.isRemote) return 0;
        // Only right after this machine was pushed; the CPU calls us in the same tick.
        if (lastPushTick != worldObj.getTotalWorldTime()) return 0;
        lastPushTick = Long.MIN_VALUE;

        IAEItemStack[] required = details.getCondensedInputs();
        IAEItemStack[] produced = details.getCondensedOutputs();
        // Batched crafts keep one ingredient set and one result, like a normal 3x3 pattern.
        if (required.length == 0 || produced.length != 1) return 0;

        IGrid grid = networkGrid();
        if (grid == null) return 0;
        IStorageGrid storage = grid.getCache(IStorageGrid.class);
        if (storage == null) return 0;
        IMEMonitor<IAEItemStack> items = storage.getItemInventory();
        if (items == null) return 0;
        MachineSource source = new MachineSource(this);

        int crafts = maxBatches;
        for (IAEItemStack input : required) {
            if (input == null) continue;
            IAEItemStack available = items.extractItems(input.copy(), Actionable.SIMULATE, source);
            long count = available == null ? 0 : available.getStackSize();
            crafts = (int) Math.min(crafts, count / Math.max(1, input.getStackSize()));
            if (crafts <= 0) return 0;
        }
        if (crafts <= 0) return 0;

        // Take everything in one go, then hand out one result per craft.
        for (IAEItemStack input : required) {
            if (input == null) continue;
            IAEItemStack need = input.copy();
            need.setStackSize(input.getStackSize() * crafts);
            items.extractItems(need, Actionable.MODULATE, source);
        }

        ItemStack result = produced[0].getItemStack()
            .copy();
        for (int i = 0; i < crafts; i++) {
            queueOutput(result.copy());
        }
        return crafts;
    }
}
