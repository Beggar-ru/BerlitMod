package ru.beggar.berlit.ae2;

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

/** Digital ME version of the Avaritia assembler or neutron compressor. */
public class BlockDigitalCraftingMachine extends BlockContainer {

    public enum Kind {
        AVARITIA,
        COMPRESSOR
    }

    private final Kind kind;
    private final String id;

    @SideOnly(Side.CLIENT)
    private IIcon iconTop;
    @SideOnly(Side.CLIENT)
    private IIcon iconBottom;
    @SideOnly(Side.CLIENT)
    private IIcon iconSide;

    public BlockDigitalCraftingMachine(Kind kind, String id) {
        super(Material.iron);
        this.kind = kind;
        this.id = id;
        setHardness(5.0F);
        setResistance(15.0F);
        setStepSound(soundTypeMetal);
        setBlockName("berlit.digital_" + id);
        setCreativeTab(Berlit.TAB);
    }

    public Kind getKind() {
        return kind;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return kind == Kind.AVARITIA ? new TileEntityDigitalAvaritiaAssembler()
            : new TileEntityDigitalNeutronCompressor();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        String texture = "berlit:digital_" + id;
        iconSide = register.registerIcon(texture + "_side");
        iconTop = register.registerIcon(texture + "_top");
        iconBottom = register.registerIcon(texture + "_bottom");
    }

    /**
     * The digital machines share one casing, and only the front carries the machine's own face:
     * metadata stores which side the player placed it against, everything else shows the casing.
     */
    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        if (side == 1) return iconTop;
        if (meta < 2) return side == 0 ? iconBottom : iconSide;
        return side == meta ? iconSide : iconBottom;
    }

    @Override
    public int damageDropped(int meta) {
        return 0;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        int yaw = MathHelper.floor_double(placer.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
        int[] front = { 2, 5, 3, 4 };
        world.setBlockMetadataWithNotify(x, y, z, front[yaw], 2);
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
