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

/** Registers the two infinite cells with AE2. */
public class InfinityCellHandler implements ICellHandler {

    @Override
    public boolean isCell(ItemStack stack) {
        return stack != null
            && (stack.getItem() instanceof ItemInfinityCell || stack.getItem() instanceof ItemInfinityFluidCell);
    }

    @Override
    public IMEInventoryHandler getCellInventory(ItemStack stack, ISaveProvider saver, StorageChannel channel) {
        if (stack == null) return null;
        if (channel == StorageChannel.ITEMS && stack.getItem() instanceof ItemInfinityCell) {
            return new InfinityItemInventory(stack, saver);
        }
        if (channel == StorageChannel.FLUIDS && stack.getItem() instanceof ItemInfinityFluidCell) {
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
