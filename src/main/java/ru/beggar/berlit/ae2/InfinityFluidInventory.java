package ru.beggar.berlit.ae2;

import java.util.ArrayList;
import java.util.List;

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

/** Infinite fluid storage: up to 5 types, unlimited bytes but real counts. */
public class InfinityFluidInventory implements IMEInventoryHandler<IAEFluidStack> {

    private static final int MAX_TYPES = 5;

    private static final class Entry {

        FluidStack stack;
        long count;
    }

    private final ItemStack cell;
    private final ISaveProvider saver;
    private final List<Entry> entries = new ArrayList<Entry>();

    public InfinityFluidInventory(ItemStack cell, ISaveProvider saver) {
        this.cell = cell;
        this.saver = saver;

        NBTTagCompound tag = cell.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            cell.setTagCompound(tag);
        }
        NBTTagList list = tag.getTagList("berlitFluidTypes", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound entryTag = list.getCompoundTagAt(i);
            FluidStack stack = FluidStack.loadFluidStackFromNBT(entryTag.getCompoundTag("Fluid"));
            if (stack == null) continue;
            Entry entry = new Entry();
            entry.stack = stack;
            entry.count = entryTag.getLong("Count");
            entries.add(entry);
        }
    }

    private void save() {
        NBTTagCompound tag = cell.getTagCompound();
        NBTTagList list = new NBTTagList();
        for (Entry entry : entries) {
            NBTTagCompound entryTag = new NBTTagCompound();
            FluidStack stack = entry.stack.copy();
            stack.amount = 1;
            stack.writeToNBT(entryTag);
            entryTag.setLong("Count", entry.count);
            list.appendTag(entryTag);
        }
        tag.setTag("berlitFluidTypes", list);
        if (saver != null) saver.saveChanges(this);
    }

    private Entry find(IAEFluidStack input) {
        FluidStack fluid = input.getFluidStack();
        if (fluid == null) return null;
        for (Entry entry : entries) {
            if (fluid.isFluidEqual(entry.stack)) return entry;
        }
        return null;
    }

    @Override
    public IAEFluidStack injectItems(IAEFluidStack input, Actionable mode, BaseActionSource src) {
        if (input == null || input.getStackSize() <= 0) return null;

        Entry entry = find(input);
        if (entry == null) {
            if (entries.size() >= MAX_TYPES) return input;
            if (mode == Actionable.MODULATE) {
                entry = new Entry();
                entry.stack = input.getFluidStack()
                    .copy();
                entry.stack.amount = 1;
                entry.count = input.getStackSize();
                entries.add(entry);
                save();
            }
            return null;
        }

        if (mode == Actionable.MODULATE) {
            if (Long.MAX_VALUE - entry.count < input.getStackSize()) {
                entry.count = Long.MAX_VALUE;
            } else {
                entry.count += input.getStackSize();
            }
            save();
        }
        return null;
    }

    @Override
    public IAEFluidStack extractItems(IAEFluidStack request, Actionable mode, BaseActionSource src) {
        if (request == null) return null;
        Entry entry = find(request);
        if (entry == null || entry.count <= 0) return null;

        long take = Math.min(entry.count, request.getStackSize());
        if (take <= 0) return null;

        if (mode == Actionable.MODULATE) {
            entry.count -= take;
            if (entry.count <= 0) entries.remove(entry);
            save();
        }

        IAEFluidStack result = request.copy();
        result.setStackSize(take);
        return result;
    }

    @Override
    public IItemList<IAEFluidStack> getAvailableItems(IItemList<IAEFluidStack> out) {
        for (Entry entry : entries) {
            if (entry.count <= 0) continue;
            IAEFluidStack ae = AEApi.instance()
                .storage()
                .createFluidStack(entry.stack);
            ae.setStackSize(entry.count);
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
        return input != null && (find(input) != null || entries.size() < MAX_TYPES);
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
