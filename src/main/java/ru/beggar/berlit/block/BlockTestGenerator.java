package ru.beggar.berlit.block;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ru.beggar.berlit.Berlit;
import ru.beggar.berlit.tileentity.TileEntityTestGenerator;

public class BlockTestGenerator extends BlockContainer {

    @SideOnly(Side.CLIENT)
    private IIcon iconTop;
    @SideOnly(Side.CLIENT)
    private IIcon iconBottom;
    @SideOnly(Side.CLIENT)
    private IIcon iconSide;

    public BlockTestGenerator() {
        super(Material.iron);
        setHardness(4.0F);
        setResistance(8.0F);
        setStepSound(soundTypeMetal);
        setBlockName("berlit.test_generator");
        setCreativeTab(Berlit.TAB);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityTestGenerator();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        iconSide = register.registerIcon("berlit:test_generator_side");
        iconTop = register.registerIcon("berlit:test_generator_top");
        iconBottom = register.registerIcon("berlit:test_generator_bottom");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        if (side == 0) return iconBottom;
        if (side == 1) return iconTop;
        return iconSide;
    }
}
