package ru.beggar.berlit.machine;

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
import ru.beggar.berlit.tileentity.TileEntityCobbleGenerator;

public class BlockCobbleGenerator extends BlockContainer {

    private final int tier;
    private final String textureName;

    @SideOnly(Side.CLIENT)
    private IIcon iconTop;
    @SideOnly(Side.CLIENT)
    private IIcon iconBottom;
    @SideOnly(Side.CLIENT)
    private IIcon iconSide;

    public BlockCobbleGenerator(int tier) {
        super(Material.iron);
        this.tier = Math.max(0, Math.min(TileEntityCobbleGenerator.TIER_COUNT - 1, tier));
        this.textureName = this.tier == 0 ? "cobble_generator" : "cobble_generator_mk" + (this.tier + 1);

        setHardness(5.0F);
        setResistance(10.0F);
        setStepSound(soundTypeMetal);
        setBlockName("berlit." + textureName);
        setCreativeTab(Berlit.TAB);
    }

    public int getTier() {
        return tier;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityCobbleGenerator(tier);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        iconSide = register.registerIcon("berlit:" + textureName + "_side");
        iconTop = register.registerIcon("berlit:" + textureName + "_top");
        iconBottom = register.registerIcon("berlit:" + textureName + "_bottom");
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
            player.openGui(Berlit.instance, 4, world, x, y, z);
        }
        return true;
    }
}
