package ru.beggar.berlit.ae2;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import ru.beggar.berlit.Berlit;

/** Infinity fluid cell: 5 fluid types, infinite amount. */
public class ItemInfinityFluidCell extends Item {

    public ItemInfinityFluidCell() {
        setUnlocalizedName("berlit.infinity_fluid_cell");
        setTextureName("berlit:infinity_fluid_cell");
        setMaxStackSize(1);
        setCreativeTab(Berlit.TAB);
    }

    public static ItemStack typesTag(ItemStack cell) {
        return cell;
    }
}
