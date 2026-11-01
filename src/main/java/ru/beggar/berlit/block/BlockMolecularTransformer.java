package ru.beggar.berlit.block;

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
import ru.beggar.berlit.tileentity.TileEntityMolecularTransformer;

public class BlockMolecularTransformer extends BlockContainer {

    @SideOnly(Side.CLIENT)
    private IIcon iconTop;
    @SideOnly(Side.CLIENT)
    private IIcon iconBottom;
    @SideOnly(Side.CLIENT)
    private IIcon iconSide;

    public BlockMolecularTransformer() {
        super(Material.iron);
        setHardness(5.0F);
        setResistance(10.0F);
        setStepSound(soundTypeMetal);
        setBlockName("berlit.molecular_transformer");
        setCreativeTab(Berlit.TAB);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityMolecularTransformer();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        iconSide = register.registerIcon("berlit:molecular_transformer_side");
        iconTop = register.registerIcon("berlit:molecular_transformer_top");
        iconBottom = register.registerIcon("berlit:molecular_transformer_bottom");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        if (side == 0) return iconBottom;
        if (side == 1) return iconTop;
        return iconSide;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
        float hitY, float hitZ) {
        if (!world.isRemote) {
            player.openGui(Berlit.instance, 0, world, x, y, z);
        }
        return true;
    }
}
