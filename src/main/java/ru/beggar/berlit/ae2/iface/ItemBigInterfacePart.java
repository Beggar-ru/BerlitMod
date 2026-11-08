package ru.beggar.berlit.ae2.iface;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import appeng.api.AEApi;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ru.beggar.berlit.Berlit;

/** Part item for the three cable-attached ME Interface sizes. */
public class ItemBigInterfacePart extends Item implements IPartItem {

    @SideOnly(Side.CLIENT)
    private IIcon[] icons;

    public ItemBigInterfacePart() {
        setUnlocalizedName("berlit.big_interface_part");
        setMaxStackSize(64);
        setHasSubtypes(true);
        setCreativeTab(Berlit.TAB);
    }

    @Override
    public IPart createPartFromItemStack(ItemStack stack) {
        switch (stack.getItemDamage()) {
            case 0:
                return new PartBigInterface18(stack);
            case 1:
                return new PartBigInterface27(stack);
            case 2:
                return new PartBigInterface36(stack);
            default:
                return null;
        }
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
        float hitX, float hitY, float hitZ) {
        return AEApi.instance()
            .partHelper()
            .placeBus(stack, x, y, z, side, player, world);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        icons = new IIcon[3];
        icons[0] = register.registerIcon("berlit:big_interface_part_18");
        icons[1] = register.registerIcon("berlit:big_interface_part_27");
        icons[2] = register.registerIcon("berlit:big_interface_part_36");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int damage) {
        return icons == null ? null : icons[Math.max(0, Math.min(2, damage))];
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return "item.berlit.big_interface_part_" + (stack.getItemDamage() + 1);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return StatCollector.translateToLocal(getUnlocalizedName(stack) + ".name");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs tab, List list) {
        list.add(new ItemStack(item, 1, 0));
        list.add(new ItemStack(item, 1, 1));
        list.add(new ItemStack(item, 1, 2));
    }
}
