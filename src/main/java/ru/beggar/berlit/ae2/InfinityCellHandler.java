package ru.beggar.berlit.ae2;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import appeng.api.implementations.tiles.IChestOrDrive;
import appeng.api.storage.ICellHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.StorageChannel;

/** Registers the infinite fluid cell with AE2. */
public class InfinityCellHandler implements ICellHandler {

    /**
     * The fluid cell implements the ExtraCells API, so referencing its class directly makes the whole mod fail to load
     * on packs without ExtraCells (NoClassDefFoundError during constructMod). Compare the class name instead: nothing
     * is resolved unless an actual fluid cell is seen.
     */
    private static final String FLUID_CELL_CLASS = "ru.beggar.berlit.ae2.ItemInfinityFluidCell";

    private static boolean isFluidCell(ItemStack stack) {
        if (stack == null || stack.getItem() == null) return false;
        return FLUID_CELL_CLASS.equals(
            stack.getItem()
                .getClass()
                .getName());
    }

    @Override
    public boolean isCell(ItemStack stack) {
        return isFluidCell(stack);
    }

    @Override
    public IMEInventoryHandler getCellInventory(ItemStack stack, ISaveProvider saver, StorageChannel channel) {
        if (stack == null) return null;
        if (channel == StorageChannel.FLUIDS && isFluidCell(stack)) {
            return new InfinityFluidInventory(stack, saver);
        }
        return null;
    }

    @Override
    public IIcon getTopTexture_Light() {
        return null;
    }

    @Override
    public IIcon getTopTexture_Medium() {
        return null;
    }

    @Override
    public IIcon getTopTexture_Dark() {
        return null;
    }

    @Override
    public void openChestGui(EntityPlayer player, IChestOrDrive chest, ICellHandler handler, IMEInventoryHandler inv,
        ItemStack stack, StorageChannel channel) {}

    @Override
    public int getStatusForCell(ItemStack stack, IMEInventory inv) {
        return 0;
    }

    @Override
    public double cellIdleDrain(ItemStack stack, IMEInventory inv) {
        return 0;
    }
}
