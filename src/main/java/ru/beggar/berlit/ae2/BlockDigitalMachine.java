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
import ru.beggar.berlit.machine.MachineType;

/**
 * Digital version of a Berlit processing machine. It has no encoded-pattern slots: when it joins
 * an ME network it exports every recipe of its machine type as an AE2 crafting pattern.
 */
public class BlockDigitalMachine extends BlockContainer {

    private final MachineType machineType;

    @SideOnly(Side.CLIENT)
    private IIcon iconTop;
    @SideOnly(Side.CLIENT)
    private IIcon iconBottom;
    @SideOnly(Side.CLIENT)
    private IIcon iconSide;

    public BlockDigitalMachine(MachineType machineType) {
        super(Material.iron);
        this.machineType = machineType;
        setHardness(5.0F);
        setResistance(15.0F);
        setStepSound(soundTypeMetal);
        setBlockName("berlit." + machineType.getDigitalId());
        setCreativeTab(Berlit.TAB);
    }

    public MachineType getMachineType() {
        return machineType;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityDigitalMachine(machineType);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        String texture = machineType.getDigitalTextureName();
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
