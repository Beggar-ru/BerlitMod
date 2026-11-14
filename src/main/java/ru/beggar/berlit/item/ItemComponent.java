package ru.beggar.berlit.item;

import net.minecraft.item.Item;

import ru.beggar.berlit.Berlit;

/**
 * Simple crafting component: one item id, one texture and one localisation key. Used for the
 * Quantum Core T1/T2 and the Reinforced Plate.
 */
public class ItemComponent extends Item {

    public ItemComponent(String name) {
        setUnlocalizedName("berlit." + name);
        setTextureName("berlit:" + name);
        setCreativeTab(Berlit.TAB);
    }
}
