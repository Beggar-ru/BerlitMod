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
        iconTop = register.registerIcon(type.getTextureName() + "_top");
        iconBottom = register.registerIcon(type.getTextureName() + "_bottom");
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
        if (player.isSneaking() && type == MachineType.METAL_FORMER) {
            if (!world.isRemote) {
                TileEntity tile = world.getTileEntity(x, y, z);
                if (tile instanceof TileEntityProcessingMachine) {
                    ((TileEntityProcessingMachine) tile).cycleMode();
                }
            }
            return true;
        }

        if (!world.isRemote) {
            player.openGui(Berlit.instance, 3, world, x, y, z);
        }
        return true;
    }
}
