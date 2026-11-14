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
import ru.beggar.berlit.tileentity.TileEntityCobbleGenerator;

/** Single-tier cobblestone generator with a 27-slot chest inventory. */
public class BlockCobbleGenerator extends BlockContainer {

    private final String textureName;

    @SideOnly(Side.CLIENT)
    private IIcon iconTop;
    @SideOnly(Side.CLIENT)
    private IIcon iconBottom;
    @SideOnly(Side.CLIENT)
    private IIcon iconSide;
    @SideOnly(Side.CLIENT)
    private IIcon iconFront;

    public BlockCobbleGenerator() {
        this(0);
    }

    /** Tier 0 is the only obtainable block. Tiers 1/2 remain hidden legacy IDs for old worlds. */
    public BlockCobbleGenerator(int legacyTier) {
        super(Material.iron);
        this.textureName = legacyTier == 0 ? "cobble_generator" : "cobble_generator_mk" + (legacyTier + 1);
        setHardness(5.0F);
        setResistance(10.0F);
        setStepSound(soundTypeMetal);
        setBlockName("berlit." + textureName);
        if (legacyTier == 0) setCreativeTab(Berlit.TAB);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityCobbleGenerator();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        iconSide = register.registerIcon("berlit:" + textureName + "_side");
        iconTop = register.registerIcon("berlit:" + textureName + "_top");
        iconBottom = register.registerIcon("berlit:" + textureName + "_bottom");
        // Only the obtainable tier has a dedicated front texture; legacy IDs keep the plain side.
        iconFront = textureName.equals("cobble_generator") ? register.registerIcon("berlit:cobble_generator_front")
            : iconSide;
        blockIcon = iconFront;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        if (side == 0) return iconBottom;
        if (side == 1) return iconTop;
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
            player.openGui(Berlit.instance, 4, world, x, y, z);
        }
        return true;
    }
}
