package ru.beggar.berlit.block;

import net.minecraft.block.Block;
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
import ru.beggar.berlit.matter.MatterType;
import ru.beggar.berlit.tileentity.TileEntityMatterGenerator;

/**
 * Solid matter generator. Eight blocks, one per {@link MatterType}; each turns the previous kind into its own.
 */
public class BlockMatterGenerator extends BlockContainer {

    private final MatterType type;

    @SideOnly(Side.CLIENT)
    private IIcon iconSide;
    @SideOnly(Side.CLIENT)
    private IIcon iconUpBottom;

    public BlockMatterGenerator(MatterType type) {
        super(Material.iron);
        this.type = type;
        setHardness(5.0F);
        setResistance(10.0F);
        setStepSound(soundTypeMetal);
        setBlockName("berlit.matter_generator_" + type.getId());
        setCreativeTab(Berlit.TAB);
    }

    public MatterType getMatterType() {
        return type;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityMatterGenerator(type);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        iconSide = register.registerIcon("berlit:matter_generator_" + type.getId());
        iconUpBottom = register.registerIcon("berlit:matter_generator_" + type.getId() + "_up_botton");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        // top and bottom share one lattice texture, the four sides show the machine face
        return side == 0 || side == 1 ? iconUpBottom : iconSide;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
        float hitY, float hitZ) {
        if (!world.isRemote) {
            player.openGui(Berlit.instance, 0, world, x, y, z);
        }
        return true;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileEntityMatterGenerator) {
            ((TileEntityMatterGenerator) tile).dropContents();
        }
        super.breakBlock(world, x, y, z, block, meta);
    }
}
