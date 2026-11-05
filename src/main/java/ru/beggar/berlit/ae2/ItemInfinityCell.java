package ru.beggar.berlit.ae2;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import ru.beggar.berlit.Berlit;

/** Infinity storage cell: 63 item types, unlimited bytes. */
public class ItemInfinityCell extends Item {

    public ItemInfinityCell() {
        setUnlocalizedName("berlit.infinity_cell");
        setTextureName("berlit:infinity_cell");
        setMaxStackSize(1);
        setCreativeTab(Berlit.TAB);
    }

    @Override
    public void addInformation(ItemStack cell, EntityPlayer player, List list, boolean advanced) {
        int types = cell.hasTagCompound() ? cell.getTagCompound()
            .getTagList("berlitTypes", 10)
            .tagCount() : 0;
        list.add("Types: " + types + " / 63");
        list.add("Bytes: infinity");
    }
}
