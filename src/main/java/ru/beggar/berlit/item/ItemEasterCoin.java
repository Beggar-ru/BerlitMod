package ru.beggar.berlit.item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import ru.beggar.berlit.Berlit;

/** Easter egg: a coin with a negative value. */
public class ItemEasterCoin extends Item {

    public ItemEasterCoin() {
        setUnlocalizedName("berlit.easter_coin");
        setTextureName("berlit:easter_coin");
        setMaxStackSize(1);
        setCreativeTab(Berlit.TAB);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
        list.add(EnumChatFormatting.RED + StatCollector.translateToLocal("tooltip.berlit.easter_coin"));
    }
}
