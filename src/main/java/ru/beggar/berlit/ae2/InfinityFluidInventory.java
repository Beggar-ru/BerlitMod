package ru.beggar.berlit.ae2;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;

/** Infinite fluid storage: up to 5 fluid types, unlimited amount. */
public class InfinityFluidInventory implements IMEInventoryHandler<IAEFluidStack> {

    private final ItemStack cell;
    private final ISaveProvider saver;
    private final NBTTagList types;

    public InfinityFluidInventory(ItemStack cell, ISaveProvider saver) {
        this.cell = cell;
        this.saver = saver;
        NBTTagCompound tag = cell.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            cell.setTagCompound(tag);
        }
        this.types = tag.getTagList("berlitFluidTypes", 10);
    }

    private void save() {
        if (saver != null) saver.saveChanges(this);
    }

    private boolean has(IAEFluidStack input) {
        for (int i = 0; i < types.tagCount(); i++) {
            FluidStack stack = FluidStack.loadFluidStackFromNBT(types.getCompoundTagAt(i));
            if (stack != null && input.getFluidStack() != null
                && input.getFluidStack()
                    .isFluidEqual(stack))
                return true;
        }
        return false;
    }

    @Override
    public IAEFluidStack injectItems(IAEFluidStack input, Actionable mode, BaseActionSource src) {
        if (input == null) return null;
        if (has(input)) return null;
        if (types.tagCount() >= 5) return input;

        if (mode == Actionable.MODULATE) {
            NBTTagCompound entry = new NBTTagCompound();
            FluidStack stack = input.getFluidStack()
                .copy();
            stack.amount = 1;
            stack.writeToNBT(entry);
            types.appendTag(entry);
            save();
        }
        return null;
    }

    @Override
    public IAEFluidStack extractItems(IAEFluidStack request, Actionable mode, BaseActionSource src) {
        if (request == null || !has(request)) return null;
        IAEFluidStack result = request.copy();
        result.setStackSize(request.getStackSize());
        return result;
    }

    @Override
    public IItemList<IAEFluidStack> getAvailableItems(IItemList<IAEFluidStack> out) {
        for (int i = 0; i < types.tagCount(); i++) {
            FluidStack stack = FluidStack.loadFluidStackFromNBT(types.getCompoundTagAt(i));
            if (stack == null) continue;
            IAEFluidStack ae = AEApi.instance()
                .storage()
                .createFluidStack(stack);
            ae.setStackSize(Long.MAX_VALUE);
            out.add(ae);
        }
        return out;
    }

    @Override
    public StorageChannel getChannel() {
        return StorageChannel.FLUIDS;
    }

    @Override
    public appeng.api.config.AccessRestriction getAccess() {
        return appeng.api.config.AccessRestriction.READ_WRITE;
    }

    @Override
    public boolean isPrioritized(IAEFluidStack input) {
        return false;
    }

    @Override
    public boolean canAccept(IAEFluidStack input) {
        return input != null && (has(input) || types.tagCount() < 5);
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public int getSlot() {
        return 0;
    }

    @Override
    public boolean validForPass(int i) {
        return true;
    }
}
