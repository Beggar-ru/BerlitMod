package ru.beggar.berlit.mixin;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appeng.api.networking.IGrid;
import appeng.api.networking.crafting.ICraftingMedium;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.ContainerNull;
import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import appeng.me.cache.CraftingGridCache;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.tile.crafting.TileCraftingTile;
import ru.beggar.berlit.Config;
import ru.beggar.berlit.ae2.AvaritiaPatternDetails;
import ru.beggar.berlit.ae2.BatchJobPrecraft;
import ru.beggar.berlit.ae2.IBatchAssembler;
import ru.beggar.berlit.ae2.TileEntityCraftingCoProcessor;

/**
 * Teaches the crafting CPU about the two things Berlit adds to AE2: a real 9x9 inventory for
 * Avaritia patterns, and machines that craft more than one item per push.
 *
 * <p>
 * AE2 assumes one push is one craft: it decrements the job's task counter by one and expects one
 * pattern's worth of output. A {@link IBatchAssembler} makes several crafts from ingredients it
 * pulls from the CPU's reserved inventory, so this hook caps the batch to what the job still needs,
 * subtracts the extra crafts from the task counter and registers the extra output with the CPU.
 * Without that bookkeeping the CPU would keep asking for crafts that were already made and
 * over-produce.
 *
 * <p>
 * The second half of the same idea is {@link #berlit$precraftWholeJob}: before AE2 starts its
 * normal one-pattern-at-a-time pass, all intermediate tasks of the job are completed from the
 * CPU's reserved inventory. That removes the network round-trip between "logs to planks" and
 * "planks to workbenches", so the fast assembler can finish the entire request as one batch.
 */
@Mixin(value = CraftingCPUCluster.class, remap = false)
public abstract class MixinCraftingCPUCluster {

    @Shadow
    private int accelerator;

    /** {@code Map<ICraftingPatternDetails, TaskProgress>} - how many crafts of each pattern remain. */
    @Shadow
    private Map tasks;

    /** Outputs the CPU is waiting for from pushed patterns. */
    @Shadow
    private IItemList waitingFor;

    /** The item the whole job is supposed to produce. */
    @Shadow
    private IAEItemStack finalOutput;

    /** The CPU's reserved job inventory, including ingredients not yet in the network. */
    @Shadow
    public abstract IMEInventory<IAEItemStack> getInventory();

    /** Source used for inventory operations performed by the CPU. */
    @Shadow
    public abstract BaseActionSource getActionSource();

    /** The grid the CPU is attached to. */
    @Shadow
    private IGrid getGrid() {
        throw new AssertionError();
    }

    /** Notifies the CPU's listeners about a change in its reserved inventory. */
    @Shadow
    private void postChange(IAEItemStack diff, BaseActionSource src) {
        throw new AssertionError();
    }

    /** Persists the CPU's task list and inventory. */
    @Shadow
    private void markDirty() {
        throw new AssertionError();
    }

    /** Cached {@code CraftingCPUCluster$TaskProgress#value}; the class is private to AE2. */
    private static Field berlit$taskValue;

    @Inject(method = "addTile", at = @At("TAIL"))
    private void berlit$addCoProcessorWeight(TileCraftingTile tile, CallbackInfo callback) {
        if (tile instanceof TileEntityCraftingCoProcessor) {
            int operations = ((TileEntityCraftingCoProcessor) tile).getOperations();
            if (operations > 1) accelerator += operations - 1;
        }
    }

    /**
     * Runs the intermediate tasks of the current job in dependency order, directly against the
     * CPU's reserved inventory. The final task is left to AE2's normal push, where
     * {@link #berlit$batchExtraCrafts} repeats it in one go.
     */
    @Inject(method = "executeCrafting", at = @At("HEAD"))
    private void berlit$precraftWholeJob(IEnergyGrid energy, CraftingGridCache craftingGrid, CallbackInfo callback) {
        if (!Config.fastAssemblerFullTask || !Config.fastAssemblerWholeJob) return;
        if (tasks == null || tasks.isEmpty() || finalOutput == null) return;

        IMEInventory<IAEItemStack> inventory = getInventory();
        if (inventory == null) return;

        List<BatchJobPrecraft.Task> views = berlit$taskViews();
        if (views.isEmpty()) return;

        final IGrid cpuGrid = getGrid();
        final Map<ICraftingPatternDetails, Boolean> gateCache = new HashMap<ICraftingPatternDetails, Boolean>();
        BatchJobPrecraft.Gate gate = new BatchJobPrecraft.Gate() {

            @Override
            public boolean canCraft(ICraftingPatternDetails pattern) {
                Boolean cached = gateCache.get(pattern);
                if (cached != null) return cached.booleanValue();

                boolean result = berlit$fastAssemblerCanCraft(pattern, craftingGrid, cpuGrid);
                gateCache.put(pattern, Boolean.valueOf(result));
                return result;
            }
        };

        long crafted = BatchJobPrecraft.run(inventory, getActionSource(), views, finalOutput, gate, views.size() + 2);
        if (crafted > 0 && cpuGrid != null) markDirty();
    }

    private List<BatchJobPrecraft.Task> berlit$taskViews() {
        List<BatchJobPrecraft.Task> views = new ArrayList<BatchJobPrecraft.Task>(tasks.size());
        for (Object object : tasks.entrySet()) {
            final Map.Entry entry = (Map.Entry) object;
            final Object pattern = entry.getKey();
            final Object progress = entry.getValue();
            if (!(pattern instanceof ICraftingPatternDetails) || progress == null) continue;

            views.add(new BatchJobPrecraft.Task() {

                @Override
                public ICraftingPatternDetails pattern() {
                    return (ICraftingPatternDetails) pattern;
                }

                @Override
                public long remaining() {
                    return berlit$readTaskValue(progress);
                }

                @Override
                public void consume(long crafts) {
                    long left = berlit$readTaskValue(progress) - crafts;
                    berlit$writeTaskValue(progress, left);
                }
            });
        }
        return views;
    }

    /** True when some fast assembler on the CPU's grid publishes this pattern. */
    private boolean berlit$fastAssemblerCanCraft(ICraftingPatternDetails details, CraftingGridCache craftingGrid,
        IGrid cpuGrid) {
        if (details == null || craftingGrid == null) return false;

        List<ICraftingMedium> mediums = craftingGrid.getMediums(details);
        if (mediums == null || mediums.isEmpty()) return false;

        for (ICraftingMedium medium : mediums) {
            IBatchAssembler assembler = berlit$findAssembler(medium);
            if (assembler != null && assembler.isOnGrid(cpuGrid)) return true;
        }
        return false;
    }

    @Redirect(
        method = "executeCrafting",
        at = @At(
            value = "INVOKE",
            target = "Lappeng/api/networking/crafting/ICraftingMedium;pushPattern"
                + "(Lappeng/api/networking/crafting/ICraftingPatternDetails;Lnet/minecraft/inventory/InventoryCrafting;)Z"),
        remap = false)
    private boolean berlit$pushPattern(ICraftingMedium medium, ICraftingPatternDetails details,
        InventoryCrafting crafting) {
        boolean pushed;
        if (details instanceof AvaritiaPatternDetails) {
            InventoryCrafting big = new InventoryCrafting(new ContainerNull(), 9, 9);
            IAEItemStack[] inputs = details.getInputs();
            for (int i = 0; i < inputs.length && i < 81; i++) {
                big.setInventorySlotContents(i, inputs[i] == null ? null : inputs[i].getItemStack());
            }
            pushed = medium.pushPattern(details, big);
        } else {
            pushed = medium.pushPattern(details, crafting);
        }

        if (pushed && !(details instanceof AvaritiaPatternDetails)) {
            berlit$batchExtraCrafts(medium, details);
        }
        return pushed;
    }

    /**
     * Lets a batch assembler repeat the pattern that was just pushed. By default it takes the
     * entire remaining task; with full-task mode off it is capped by {@code fastAssemblerBatch}.
     * Either way it never exceeds what the job still wants, so a request for 1000 items cannot
     * produce 64 000.
     */
    private void berlit$batchExtraCrafts(ICraftingMedium medium, ICraftingPatternDetails details) {
        if (details == null || tasks == null || waitingFor == null) return;
        if (!details.isCraftable() || details instanceof AvaritiaPatternDetails) return;

        IGrid cpuGrid = getGrid();
        IBatchAssembler assembler = berlit$findAssembler(medium);
        if (assembler == null || !assembler.isOnGrid(cpuGrid)) return;

        Object task = tasks.get(details);
        if (task == null) return;
        long remaining = berlit$readTaskValue(task);
        if (remaining <= 1) return;

        long allowed = remaining - 1;
        if (!Config.fastAssemblerFullTask) {
            allowed = Math.min(allowed, Math.max(0, Config.fastAssemblerBatch - 1));
        }
        IAEItemStack[] outputs = details.getCondensedOutputs();
        if (outputs == null || outputs.length == 0) return;
        if (allowed <= 0) return;

        int extra = assembler
            .craftExtraBatches(details, (int) Math.min(Integer.MAX_VALUE, allowed), getInventory(), getActionSource());
        if (extra <= 0) return;

        berlit$writeTaskValue(task, remaining - extra);

        // The CPU adds one batch of outputs itself after this hook, so register only the extras.
        for (IAEItemStack output : outputs) {
            if (output == null) continue;
            IAEItemStack copy = output.copy();
            copy.setStackSize(output.getStackSize() * extra);
            waitingFor.add(copy);
            postChange(copy.copy(), getActionSource());
        }
        if (cpuGrid != null) markDirty();
    }

    private static IBatchAssembler berlit$findAssembler(ICraftingMedium medium) {
        if (medium instanceof IBatchAssembler) return (IBatchAssembler) medium;
        // Only ME Interfaces push patterns into an adjacent machine. A molecular assembler or a
        // digital machine is a medium itself, so an unrelated fast assembler next to it must not
        // be mistaken for the machine that will execute the pattern.
        if (!(medium instanceof DualityInterface)) return null;

        DualityInterface duality = (DualityInterface) medium;
        TileEntity tile;
        java.util.EnumSet<ForgeDirection> sides;
        if (duality.getHost() instanceof IInterfaceHost) {
            IInterfaceHost host = (IInterfaceHost) duality.getHost();
            tile = host.getTileEntity();
            sides = host.getTargets();
        } else {
            tile = duality.getTile();
            sides = null;
        }
        if (tile == null || tile.getWorldObj() == null) return null;

        // The interface only pushes to its configured target sides; the assembler has to be there.
        ForgeDirection[] directions = sides == null ? ForgeDirection.VALID_DIRECTIONS
            : sides.toArray(new ForgeDirection[sides.size()]);
        for (ForgeDirection direction : directions) {
            TileEntity adjacent = tile.getWorldObj()
                .getTileEntity(
                    tile.xCoord + direction.offsetX,
                    tile.yCoord + direction.offsetY,
                    tile.zCoord + direction.offsetZ);
            if (adjacent instanceof IBatchAssembler) return (IBatchAssembler) adjacent;
        }
        return null;
    }

    private static long berlit$readTaskValue(Object task) {
        try {
            if (berlit$taskValue == null) {
                Field field = task.getClass()
                    .getDeclaredField("value");
                field.setAccessible(true);
                berlit$taskValue = field;
            }
            return berlit$taskValue.getLong(task);
        } catch (Throwable ignored) {
            // Unknown AE2 build: fall back to plain one-craft-per-push behaviour.
            return 0;
        }
    }

    private static void berlit$writeTaskValue(Object task, long value) {
        try {
            if (berlit$taskValue == null) return;
            berlit$taskValue.setLong(task, Math.max(0, value));
        } catch (Throwable ignored) {}
    }
}
