package ru.beggar.berlit.ae2;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import appeng.block.crafting.BlockCraftingUnit;
import appeng.client.render.BlockRenderInfo;
import appeng.client.texture.FlippableIcon;
import appeng.tile.crafting.TileCraftingTile;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ru.beggar.berlit.Berlit;

/** Common block shell for custom AE2 crafting CPU parts. */
public abstract class BlockCraftingCpuBlock extends BlockCraftingUnit {

    private final String id;

    @SideOnly(Side.CLIENT)
    private IIcon iconTop;
    @SideOnly(Side.CLIENT)
    private IIcon iconBottom;
    @SideOnly(Side.CLIENT)
    private IIcon iconSide;
    @SideOnly(Side.CLIENT)
    private IIcon iconRing;
    @SideOnly(Side.CLIENT)
    private IIcon iconRingLong;
    @SideOnly(Side.CLIENT)
    private IIcon iconRingLongRotated;
    @SideOnly(Side.CLIENT)
    private IIcon iconFitSolid;
    @SideOnly(Side.CLIENT)
    private IIcon iconFit;

    /** Set only while an item renderer draws the block in a GUI; makes RenderBlocks use a cube. */
    public static boolean renderingItem;

    protected BlockCraftingCpuBlock(String id, Class<? extends TileCraftingTile> tileClass) {
        super();
        this.id = id;
        setBlockName("berlit." + id);
        setCreativeTab(Berlit.TAB);
        setHardness(5.0F);
        setResistance(15.0F);
        hasSubtypes = false;
        setTileEntity(tileClass);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        iconSide = register.registerIcon("berlit:" + id + "_side");
        iconTop = register.registerIcon("berlit:" + id + "_top");
        iconBottom = register.registerIcon("berlit:" + id + "_bottom");
        // AE2's crafting CPU renderer uses these four extra textures to draw the multiblock
        // shell: corner rings, the two long edge strips and the solid inset faces.
        iconRing = register.registerIcon("berlit:crafting_unit_ring");
        iconRingLong = register.registerIcon("berlit:crafting_unit_ring_long");
        iconRingLongRotated = register.registerIcon("berlit:crafting_unit_ring_long_rotated");
        iconFitSolid = register.registerIcon("berlit:crafting_fit_solid");
        iconFit = register.registerIcon("berlit:" + id + "_fit");

        // Keep AE2's block render info valid; it is used for the item form and for the unformed cube.
        BlockRenderInfo info = getRendererInstance();
        info.updateIcons(
            new FlippableIcon(iconBottom),
            new FlippableIcon(iconTop),
            new FlippableIcon(iconSide),
            new FlippableIcon(iconSide),
            new FlippableIcon(iconSide),
            new FlippableIcon(iconSide));
    }

    /**
     * AE2's CPU renderer asks for metadata | 8 once the multiblock is formed and uses that icon as
     * the inset face colour. Unformed blocks keep the normal side/top/bottom cube look.
     */
    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        if ((meta & 8) != 0) return iconFit;
        if (side == 0) return iconBottom;
        if (side == 1) return iconTop;
        return iconSide;
    }

    @Override
    public int getRenderType() {
        // The item renderer flips this while drawing the inventory cube; normally the block belongs
        // to AE2's WorldRender, which has no plain ItemBlock inventory renderer.
        return renderingItem ? 0 : super.getRenderType();
    }

    @SideOnly(Side.CLIENT)
    public IIcon getRingIcon() {
        return iconRing;
    }

    @SideOnly(Side.CLIENT)
    public IIcon getRingLongIcon() {
        return iconRingLong;
    }

    @SideOnly(Side.CLIENT)
    public IIcon getRingLongRotatedIcon() {
        return iconRingLongRotated;
    }

    @SideOnly(Side.CLIENT)
    public IIcon getFitSolidIcon() {
        return iconFitSolid;
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
