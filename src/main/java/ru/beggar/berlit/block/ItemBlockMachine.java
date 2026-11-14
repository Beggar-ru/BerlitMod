package ru.beggar.berlit.block;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.IIcon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Item form for the directional Avaritia machines. The world block shows the front according to
 * its metadata, while the inventory icon should always show that front face instead of the top.
 */
public class ItemBlockMachine extends ItemBlock {

    private final Block machine;

    public ItemBlockMachine(Block block) {
        super(block);
        this.machine = block;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int meta) {
        // Side 2 is the default north-facing front for an unplaced machine.
        return machine.getIcon(2, meta);
    }
}
