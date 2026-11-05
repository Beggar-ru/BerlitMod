package ru.beggar.berlit.ae2;

import java.util.ArrayList;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;

import appeng.api.config.FuzzyMode;
import extracells.api.IFluidStorageCell;
import ru.beggar.berlit.Berlit;

/** Infinity fluid cell: standard ExtraCells fluid cell with 5 types and a huge capacity. */
public class ItemInfinityFluidCell extends Item implements IFluidStorageCell {

    private static final IInventory EMPTY = new InventoryBasic("berlit_fluid_cell", false, 1);

    public ItemInfinityFluidCell() {
        setUnlocalizedName("berlit.infinity_fluid_cell");
        setTextureName("berlit:infinity_fluid_cell");
        setMaxStackSize(1);
        setCreativeTab(Berlit.TAB);
    }

    @Override
    public ArrayList<Fluid> getFilter(ItemStack cell) {
        return new ArrayList<Fluid>();
    }

    @Override
    public int getMaxBytes(ItemStack cell) {
        return 100000000;
    }

    @Override
    public int getMaxTypes(ItemStack cell) {
        return 5;
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
