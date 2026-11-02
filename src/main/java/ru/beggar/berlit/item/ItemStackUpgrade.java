package ru.beggar.berlit.item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ru.beggar.berlit.Berlit;

/** Processes a whole stack in one operation when installed into a Berlit machine. */
public class ItemStackUpgrade extends Item {

    public ItemStackUpgrade() {
        setUnlocalizedName("berlit.stack_upgrade");
        setTextureName("berlit:stack_upgrade");
        setMaxStackSize(16);
        setCreativeTab(Berlit.TAB);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return StatCollector.translateToLocal("item.berlit.stack_upgrade.name");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
        list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("item.berlit.stack_upgrade.tooltip"));
    }
}
