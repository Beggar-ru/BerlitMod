package ru.beggar.berlit.block;

import java.util.Random;

import net.minecraft.block.BlockCrops;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import ru.beggar.berlit.Berlit;

public class BlockGarlicCrop extends BlockCrops {

    public BlockGarlicCrop() {
        setBlockName("berlit.garlic_crop");
        setBlockTextureName("berlit:garlic_crop");
        setCreativeTab(null);
    }

    @Override
    protected Item func_149865_P() {
        return Berlit.garlic;
    }

    @Override
    public Item getItemDropped(int meta, Random random, int fortune) {
        return Berlit.garlic;
    }

    @Override
    public int quantityDropped(Random random) {
        return 1 + random.nextInt(2);
    }

    @Override
    public void dropBlockAsItemWithChance(World world, int x, int y, int z, int meta, float chance, int fortune) {
        super.dropBlockAsItemWithChance(world, x, y, z, meta, chance, fortune);
        if (!world.isRemote && meta >= 7) {
            dropBlockAsItem(world, x, y, z, new ItemStack(Berlit.garlic, 1 + world.rand.nextInt(2)));
        }
    }
}
