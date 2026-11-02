package ru.beggar.berlit.item;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ru.beggar.berlit.Berlit;

public class ItemAccelerator extends Item {

    public static final int TIER_2 = 2;
    public static final int TIER_3 = 3;

    private final int tier;

    public ItemAccelerator(int tier) {
        this.tier = tier;
        setUnlocalizedName("berlit.accelerator_t" + tier);
        setTextureName("berlit:accelerator_t" + tier);
        setMaxStackSize(16);
        setCreativeTab(Berlit.TAB);
    }

    public int getTier() {
        return tier;
    }

    /** Speed multiplier applied to machine progress. */
    public float getSpeedMultiplier() {
        return tier == TIER_3 ? 3.0F : 2.0F;
    }

    /** Energy cost multiplier applied to the recipe energy. */
    public float getEnergyMultiplier() {
        return tier == TIER_3 ? 0.4F : 0.6F;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return StatCollector.translateToLocal("item.berlit.accelerator_t" + tier + ".name");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
        list.add(
            EnumChatFormatting.GRAY + StatCollector
                .translateToLocalFormatted("item.berlit.accelerator.speed", (int) (getSpeedMultiplier() * 100)));
        list.add(
            EnumChatFormatting.GRAY + StatCollector
                .translateToLocalFormatted("item.berlit.accelerator.energy", (int) (getEnergyMultiplier() * 100)));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs tab, List list) {
        list.add(new ItemStack(item));
    }
}
