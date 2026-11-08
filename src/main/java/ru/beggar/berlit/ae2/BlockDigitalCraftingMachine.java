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
            player.openGui(Berlit.instance, 6, world, x, y, z);
        }
        return true;
    }
}
