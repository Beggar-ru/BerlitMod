package ru.beggar.berlit.ae2;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import ru.beggar.berlit.Berlit;

/** Infinity storage cell: 63 item types, infinite amount. */
public class ItemInfinityCell extends Item {

    public ItemInfinityCell() {
        setUnlocalizedName("berlit.infinity_cell");
        setTextureName("berlit:infinity_cell");
        setMaxStackSize(1);
        setCreativeTab(Berlit.TAB);
    }

    public static ItemStack typesTag(ItemStack cell) {
        return cell;
    }
}
