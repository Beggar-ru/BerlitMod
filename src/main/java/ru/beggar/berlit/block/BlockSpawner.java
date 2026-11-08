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
import ru.beggar.berlit.Config;
import ru.beggar.berlit.tileentity.TileEntitySpawner;

public class BlockSpawner extends BlockContainer {

    @SideOnly(Side.CLIENT)
    private IIcon iconTop;
    @SideOnly(Side.CLIENT)
    private IIcon iconBottom;
    @SideOnly(Side.CLIENT)
    private IIcon iconSide;

    public BlockSpawner() {
        super(Material.iron);
        setHardness(5.0F);
        setResistance(12.0F);
        setStepSound(soundTypeMetal);
        setBlockName("berlit.spawner");
        setCreativeTab(Berlit.TAB);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntitySpawner();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        iconSide = register.registerIcon("berlit:spawner_side");
        iconTop = register.registerIcon("berlit:spawner_top");
        iconBottom = iconTop;
    }

    @Override
    public boolean isOpaqueCube() {
        return !Config.transparentSpawners;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return !Config.transparentSpawners;
    }

    @Override
    public int getRenderBlockPass() {
        return Config.transparentSpawners ? 1 : 0;
    }

    @Override
    public boolean canRenderInPass(int pass) {
        return Config.transparentSpawners ? pass == 1 : pass == 0;
    }

    @Override
    public int getLightOpacity() {
        return Config.transparentSpawners ? 0 : 255;
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
            player.openGui(Berlit.instance, 1, world, x, y, z);
        }
        return true;
    }
}
