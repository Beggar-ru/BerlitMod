package ru.beggar.berlit.block;

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
import ru.beggar.berlit.machine.MachineType;
import ru.beggar.berlit.tileentity.TileEntityProcessingMachine;

public class BlockProcessingMachine extends BlockContainer {

    private final MachineType type;

    @SideOnly(Side.CLIENT)
    private IIcon iconTop;
    @SideOnly(Side.CLIENT)
    private IIcon iconBottom;
    @SideOnly(Side.CLIENT)
    private IIcon iconSide;
    @SideOnly(Side.CLIENT)
    private IIcon iconFront;

    public BlockProcessingMachine(MachineType type) {
        super(Material.iron);
        this.type = type;
        setHardness(5.0F);
        setResistance(10.0F);
        setStepSound(soundTypeMetal);
        setBlockName("berlit.machine_" + type.getId());
        setCreativeTab(Berlit.TAB);
    }

    public MachineType getMachineType() {
        return type;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityProcessingMachine(type);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        iconSide = register.registerIcon(type.getTextureName() + "_side");
        iconFront = register.registerIcon(type.getTextureName() + "_front");
        iconTop = register.registerIcon(type.getTextureName() + "_top");
        iconBottom = register.registerIcon(type.getTextureName() + "_bottom");
    }

    /**
     * The front face shows the machine-specific working face; every other side uses the common
     * mechanism casing. Top and bottom always use the plain top/bottom texture.
     */
    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        if (side == 1) return iconTop;
        if (side == 0) return iconBottom;
        // Item form and old worlds without stored facing show the front on all four sides.
        if (meta < 2) return iconFront;
        return side == meta ? iconFront : iconSide;
    }

    @Override
    public int damageDropped(int meta) {
        // The facing lives in metadata; the dropped item must not inherit it.
        return 0;
    }

    /** Stores the front side in metadata, so the machine faces the player who placed it. */
    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        int yaw = MathHelper.floor_double(placer.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
        // yaw 0 south, 1 west, 2 north, 3 east -> the opposite side faces the player
        int[] front = { 2, 5, 3, 4 };
        world.setBlockMetadataWithNotify(x, y, z, front[yaw], 2);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
        float hitY, float hitZ) {
        // Old worlds briefly saved these blocks without a registered tile entity. Recreate a
        // correctly typed tile when the player opens such a machine.
        TileEntity tile = world.getTileEntity(x, y, z);
        if (!(tile instanceof TileEntityProcessingMachine)
            || ((TileEntityProcessingMachine) tile).getMachineType() != type) {
            tile = new TileEntityProcessingMachine(type);
            world.setTileEntity(x, y, z, tile);
        }

        if (!world.isRemote) {
            player.openGui(Berlit.instance, 3, world, x, y, z);
        }
        return true;
    }
}
