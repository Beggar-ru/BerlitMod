package ru.beggar.berlit.ae2;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import appeng.block.AEBaseTileBlock;
import appeng.core.sync.GuiBridge;
import appeng.tile.crafting.TileCraftingTile;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ru.beggar.berlit.Berlit;

/** Common block shell for custom AE2 crafting CPU parts. */
public abstract class BlockCraftingCpuBlock extends AEBaseTileBlock {

    private final String id;

    @SideOnly(Side.CLIENT)
    private IIcon iconTop;
    @SideOnly(Side.CLIENT)
    private IIcon iconBottom;
    @SideOnly(Side.CLIENT)
    private IIcon iconSide;

    protected BlockCraftingCpuBlock(String id, Class<? extends TileCraftingTile> tileClass) {
        super(Material.iron);
        this.id = id;
        setBlockName("berlit." + id);
        setCreativeTab(Berlit.TAB);
        hasSubtypes = false;
        setTileEntity(tileClass);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        iconSide = register.registerIcon("berlit:" + id + "_side");
        iconTop = register.registerIcon("berlit:" + id + "_top");
        iconBottom = register.registerIcon("berlit:" + id + "_bottom");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        if (side == 0) return iconBottom;
        if (side == 1) return iconTop;
        return iconSide;
    }

    @Override
    public boolean onActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY,
        float hitZ) {
        TileCraftingTile tile = getTileEntity(world, x, y, z);
        if (tile != null && tile.isFormed() && tile.isActive()) {
            if (!world.isRemote) {
                Platform.openGUI(
                    player,
                    tile,
                    net.minecraftforge.common.util.ForgeDirection.getOrientation(side),
                    GuiBridge.GUI_CRAFTING_CPU);
            }
            return true;
        }
        return false;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        TileCraftingTile tile = getTileEntity(world, x, y, z);
        if (tile != null) tile.breakCluster();
        super.breakBlock(world, x, y, z, block, meta);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        TileCraftingTile tile = getTileEntity(world, x, y, z);
        if (tile != null) tile.updateMultiBlock();
        super.onNeighborBlockChange(world, x, y, z, block);
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
