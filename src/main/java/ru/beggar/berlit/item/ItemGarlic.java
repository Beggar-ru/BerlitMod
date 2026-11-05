package ru.beggar.berlit.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import ru.beggar.berlit.Berlit;

/** Garlic: edible and plantable on farmland. */
public class ItemGarlic extends ItemFood {

    public ItemGarlic() {
        super(2, 0.3F, false);
        setUnlocalizedName("berlit.garlic");
        setTextureName("berlit:garlic");
        setCreativeTab(Berlit.TAB);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
        float hitX, float hitY, float hitZ) {
        if (world.getBlock(x, y, z) != Blocks.farmland) return false;
        if (!world.isAirBlock(x, y + 1, z)) return false;

        if (!world.isRemote) {
            world.setBlock(x, y + 1, z, Berlit.garlicCrop);
            stack.stackSize--;
        }
        return true;
    }
}
