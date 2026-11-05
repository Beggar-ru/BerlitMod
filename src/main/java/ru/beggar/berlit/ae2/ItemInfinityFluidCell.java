package ru.beggar.berlit.ae2;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import ru.beggar.berlit.Berlit;

/** Infinity fluid cell: 5 fluid types, unlimited bytes. */
public class ItemInfinityFluidCell extends Item {

    public ItemInfinityFluidCell() {
        setUnlocalizedName("berlit.infinity_fluid_cell");
        setTextureName("berlit:infinity_fluid_cell");
        setMaxStackSize(1);
        setCreativeTab(Berlit.TAB);
    }

    @Override
    public void addInformation(ItemStack cell, EntityPlayer player, List list, boolean advanced) {
        int types = cell.hasTagCompound() ? cell.getTagCompound()
            .getTagList("berlitFluidTypes", 10)
            .tagCount() : 0;
        list.add("Types: " + types + " / 5");
        list.add("Bytes: infinity");
    }
}
