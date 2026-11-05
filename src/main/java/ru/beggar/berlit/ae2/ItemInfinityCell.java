package ru.beggar.berlit.ae2;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import appeng.api.config.FuzzyMode;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.storage.data.IAEItemStack;
import ru.beggar.berlit.Berlit;

/** Infinity item cell: standard AE2 cell with 63 types and a very large byte capacity. */
public class ItemInfinityCell extends Item implements IStorageCell {

    private static final IInventory EMPTY = new InventoryBasic("berlit_cell", false, 1);

    public ItemInfinityCell() {
        setUnlocalizedName("berlit.infinity_cell");
        setTextureName("berlit:infinity_cell");
        setMaxStackSize(1);
        setCreativeTab(Berlit.TAB);
    }

    @Override
    public int getBytes(ItemStack cell) {
        return Integer.MAX_VALUE;
    }

    @Override
    public int BytePerType(ItemStack cell) {
        return 1;
    }

    @Override
    public int getBytesPerType(ItemStack cell) {
        return 1;
    }

    @Override
    public int getTotalTypes(ItemStack cell) {
        return 63;
    }

    @Override
    public boolean isBlackListed(ItemStack cell, IAEItemStack requestedAddition) {
        return false;
    }

    @Override
    public boolean storableInStorageCell() {
        return false;
    }

    @Override
    public boolean isStorageCell(ItemStack cell) {
        return true;
    }

    @Override
    public double getIdleDrain() {
        return 0;
    }

    @Override
    public boolean isEditable(ItemStack cell) {
        return false;
    }

    @Override
    public IInventory getUpgradesInventory(ItemStack cell) {
        return EMPTY;
    }

    @Override
    public IInventory getConfigInventory(ItemStack cell) {
        return EMPTY;
    }

    @Override
    public FuzzyMode getFuzzyMode(ItemStack cell) {
        return FuzzyMode.IGNORE_ALL;
    }

    @Override
    public void setFuzzyMode(ItemStack cell, FuzzyMode mode) {}
}
