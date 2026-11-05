package ru.beggar.berlit.ae2;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;

/** Infinite item storage: up to 63 types, unlimited count per type. */
public class InfinityItemInventory implements IMEInventoryHandler<IAEItemStack> {

    private final ItemStack cell;
    private final ISaveProvider saver;
    private final NBTTagList types;

    public InfinityItemInventory(ItemStack cell, ISaveProvider saver) {
        this.cell = cell;
        this.saver = saver;
        NBTTagCompound tag = cell.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            cell.setTagCompound(tag);
        }
        this.types = tag.getTagList("berlitTypes", 10);
    }

    private void save() {
        if (saver != null) saver.saveChanges(this);
    }

    private boolean has(IAEItemStack input) {
        for (int i = 0; i < types.tagCount(); i++) {
            ItemStack stack = ItemStack.loadItemStackFromNBT(types.getCompoundTagAt(i));
            if (stack != null && input.isSameType(stack)) return true;
        }
        return false;
    }

    @Override
    public IAEItemStack injectItems(IAEItemStack input, Actionable mode, BaseActionSource src) {
        if (input == null) return null;
        if (has(input)) return null;
        if (types.tagCount() >= 63) return input;

        if (mode == Actionable.MODULATE) {
            NBTTagCompound entry = new NBTTagCompound();
            ItemStack stored = input.getItemStack()
                .copy();
            stored.stackSize = 1;
            stored.writeToNBT(entry);
            types.appendTag(entry);
            save();
        }
        return null;
    }

    @Override
    public IAEItemStack extractItems(IAEItemStack request, Actionable mode, BaseActionSource src) {
        if (request == null || !has(request)) return null;
        IAEItemStack result = request.copy();
        result.setStackSize(request.getStackSize());
        return result;
    }

    @Override
    public IItemList<IAEItemStack> getAvailableItems(IItemList<IAEItemStack> out) {
        for (int i = 0; i < types.tagCount(); i++) {
            ItemStack stack = ItemStack.loadItemStackFromNBT(types.getCompoundTagAt(i));
            if (stack == null) continue;
            IAEItemStack ae = AEApi.instance()
                .storage()
                .createItemStack(stack);
            ae.setStackSize(Long.MAX_VALUE);
            out.add(ae);
        }
        return out;
    }

    @Override
    public StorageChannel getChannel() {
        return StorageChannel.ITEMS;
    }

    @Override
    public appeng.api.config.AccessRestriction getAccess() {
        return appeng.api.config.AccessRestriction.READ_WRITE;
    }

    @Override
    public boolean isPrioritized(IAEItemStack input) {
        return false;
    }

    @Override
    public boolean canAccept(IAEItemStack input) {
        return input != null && (has(input) || types.tagCount() < 63);
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
