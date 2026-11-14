package ru.beggar.berlit.ae2;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ru.beggar.berlit.Berlit;

public class BlockPatternEncoder extends BlockContainer {

    @SideOnly(Side.CLIENT)
    private IIcon iconSide;
    @SideOnly(Side.CLIENT)
    private IIcon iconTop;
    @SideOnly(Side.CLIENT)
    private IIcon iconBottom;

    public BlockPatternEncoder() {
        super(Material.iron);
        setHardness(4.0F);
        setResistance(12.0F);
        setStepSound(soundTypeMetal);
        setBlockName("berlit.pattern_encoder");
        setCreativeTab(Berlit.TAB);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityPatternEncoder();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        iconSide = register.registerIcon("berlit:avaritia_assembler_side");
        iconTop = register.registerIcon("berlit:pattern_encoder_top");
        iconBottom = register.registerIcon("berlit:avaritia_top_and_bottom_assembler");
        blockIcon = iconSide;
    }

    /** The 9x9 pattern encoder shares the Avaritia casing but has its own top layout. */
    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        if (side == 1) return iconTop;
        if (side == 0) return iconBottom;
        return iconSide;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
        float hitY, float hitZ) {
        if (!world.isRemote) {
            player.openGui(Berlit.instance, 6, world, x, y, z);
        }
        return true;
    }
}
