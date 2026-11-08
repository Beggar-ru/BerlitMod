package ru.beggar.berlit.ae2.iface;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import appeng.block.misc.BlockInterface;
import appeng.tile.misc.TileInterface;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ru.beggar.berlit.Berlit;

/** ME Interface block with an expanded pattern inventory and a custom Berlit GUI. */
public class BlockBigInterface extends BlockInterface {

    private final String id;
    private final String texture;
    @SideOnly(Side.CLIENT)
    private IIcon icon;

    public BlockBigInterface(String id, String texture, Class<? extends TileInterface> tileClass) {
        super();
        this.id = id;
        this.texture = texture;
        setBlockName("berlit." + id);
        setCreativeTab(Berlit.TAB);
        hasSubtypes = false;
        setTileEntity(tileClass);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        icon = register.registerIcon("berlit:" + texture);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return icon;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(net.minecraft.world.IBlockAccess world, int x, int y, int z, int side) {
        return icon;
    }

    /**
     * AE2 renders its own interface through {@code RenderBlockInterface}, which expects the icons AE2
     * registers in {@code BlockInterface.registerBlockIcons} - a method this class replaces with its own
     * texture. The renderer then draws nothing and the block looks untextured. Returning the plain cube
     * render id hands the block to the vanilla renderer, which uses {@link #getIcon(int, int)}.
     */
    @Override
    public int getRenderType() {
        return 0;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return true;
    }

    @Override
    public boolean onActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY,
        float hitZ) {
        if (player.isSneaking()) return false;
        if (!world.isRemote) player.openGui(Berlit.instance, 7, world, x, y, z);
        return true;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return getUnlocalizedName();
    }

    @Override
    public void getCheckedSubBlocks(Item item, CreativeTabs tab, List list) {
        list.add(new ItemStack(item));
    }
}
