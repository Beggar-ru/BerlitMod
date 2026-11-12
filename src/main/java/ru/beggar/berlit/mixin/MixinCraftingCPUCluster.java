package ru.beggar.berlit.mixin;

import java.lang.reflect.Field;
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

import appeng.api.networking.crafting.ICraftingMedium;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.ContainerNull;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.tile.crafting.TileCraftingTile;
import ru.beggar.berlit.Config;
import ru.beggar.berlit.ae2.AvaritiaPatternDetails;
import ru.beggar.berlit.ae2.IBatchAssembler;
import ru.beggar.berlit.ae2.TileEntityCraftingCoProcessor;

/**
 * Teaches the crafting CPU about the two things Berlit adds to AE2: a real 9x9 inventory for
 * Avaritia patterns, and machines that craft more than one item per push.
 *
 * <p>
 * AE2 assumes one push is one craft: it decrements the job's task counter by one and expects one
 * pattern's worth of output. A {@link IBatchAssembler} makes several crafts from ingredients it
 * pulls from the network itself, so this hook caps the batch to what the job still needs, subtracts
 * the extra crafts from the task counter and registers the extra output with the CPU. Without that
 * bookkeeping the CPU would keep asking for crafts that were already made and over-produce.
 */
@Mixin(value = CraftingCPUCluster.class, remap = false)
public class MixinCraftingCPUCluster {

    @Shadow
    private int accelerator;

    /** {@code Map<ICraftingPatternDetails, TaskProgress>} - how many crafts of each pattern remain. */
    @Shadow
    private Map tasks;

    /** Outputs the CPU is waiting for from pushed patterns. */
    @Shadow
    private IItemList waitingFor;

    /** Cached {@code CraftingCPUCluster$TaskProgress#value}; the class is private to AE2. */
    private static Field berlit$taskValue;

    @Inject(method = "addTile", at = @At("TAIL"))
    private void berlit$addCoProcessorWeight(TileCraftingTile tile, CallbackInfo callback) {
        if (tile instanceof TileEntityCraftingCoProcessor) {
            int operations = ((TileEntityCraftingCoProcessor) tile).getOperations();
            if (operations > 1) accelerator += operations - 1;
        }
    }

    @Redirect(
        method = "executeCrafting",
        at = @At(value = "INVOKE", target = "appeng/api/networking/crafting/ICraftingMedium.pushPattern"),
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

        if (pushed) {
            berlit$batchExtraCrafts(medium, details);
        }
        return pushed;
    }

    /**
     * Lets a batch assembler repeat the pattern that was just pushed. The batch never exceeds what
     * the job still wants, so a request for 1000 items cannot produce 64 000.
     */
    private void berlit$batchExtraCrafts(ICraftingMedium medium, ICraftingPatternDetails details) {
        int limit = Math.min(64, Config.fastAssemblerBatch) - 1;
        if (limit <= 0 || details == null || tasks == null || waitingFor == null) return;

        IBatchAssembler assembler = berlit$findAssembler(medium);
        if (assembler == null) return;

        Object task = tasks.get(details);
        if (task == null) return;
        long remaining = berlit$readTaskValue(task);
        if (remaining <= 1) return;

        int extra = assembler.craftExtraBatches(details, (int) Math.min(limit, remaining - 1));
        if (extra <= 0) return;

        berlit$writeTaskValue(task, remaining - extra);

        // The CPU adds one batch of outputs itself after this hook, so register only the extras.
        IAEItemStack[] outputs = details.getCondensedOutputs();
        for (IAEItemStack output : outputs) {
            if (output == null) continue;
            IAEItemStack copy = output.copy();
            copy.setStackSize(output.getStackSize() * extra);
            waitingFor.add(copy);
        }
    }

    private static IBatchAssembler berlit$findAssembler(ICraftingMedium medium) {
        if (medium instanceof IBatchAssembler) return (IBatchAssembler) medium;
        if (!(medium instanceof TileEntity)) return null;

        TileEntity tile = (TileEntity) medium;
        if (tile.getWorldObj() == null) return null;

        // Patterns usually live in an ME Interface that pushes into the machine next to it.
        for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
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
