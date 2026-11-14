package ru.beggar.berlit.machine;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ru.beggar.berlit.Berlit;

public class BlockNeutronCompressor extends BlockContainer {

    @SideOnly(Side.CLIENT)
    private IIcon iconSide;
    @SideOnly(Side.CLIENT)
    private IIcon iconFront;
    @SideOnly(Side.CLIENT)
    private IIcon iconTopBottom;

    public BlockNeutronCompressor() {
        super(Material.iron);
        setHardness(5.0F);
        setResistance(15.0F);
        setStepSound(soundTypeMetal);
        setBlockName("berlit.neutron_compressor");
        setCreativeTab(Berlit.TAB);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityNeutronCompressor();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        iconSide = register.registerIcon("berlit:avaritia_assembler_side");
        iconFront = register.registerIcon("berlit:avaritia_frontside_neutroncompressor");
        iconTopBottom = register.registerIcon("berlit:avaritia_top_and_bottom_assembler");
        blockIcon = iconFront;
    }

    /**
     * The neutron compressor shares the Avaritia casing but has its own front face. Metadata
     * stores which side faces the player.
     */
    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        if (side == 0 || side == 1) return iconTopBottom;
        // Old worlds without stored facing default to the north front.
        if (meta < 2) return side == 2 ? iconFront : iconSide;
        return side == meta ? iconFront : iconSide;
    }

    @Override
    public int damageDropped(int meta) {
        // Facing lives in metadata; the dropped item must not inherit it.
        return 0;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        int yaw = MathHelper.floor_double(placer.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
        // yaw 0 south, 1 west, 2 north, 3 east -> the opposite side faces the player.
        int[] front = { 2, 5, 3, 4 };
        world.setBlockMetadataWithNotify(x, y, z, front[yaw], 2);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
        float hitY, float hitZ) {
        if (!world.isRemote) {
            player.openGui(Berlit.instance, 7, world, x, y, z);
        }
        return true;
    }
}
