package ru.beggar.berlit.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

import ru.beggar.berlit.Berlit;

/** Decorative block of nether stars. */
public class BlockNetherStar extends Block {

    public BlockNetherStar() {
        super(Material.iron);
        setHardness(5.0F);
        setResistance(20.0F);
        setStepSound(soundTypeMetal);
        setBlockName("berlit.nether_star_block");
        setBlockTextureName("berlit:nether_star_block");
        setCreativeTab(Berlit.TAB);
    }
}
