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
import ru.beggar.berlit.spawner.UpgradeRegistry;
import ru.beggar.berlit.spawner.UpgradeStats;

/** Berlit spawner core: increases cycle speed and drop amount. */
public class ItemSpawnerCore extends Item implements UpgradeRegistry.ISpawnerUpgrade {

    private final int tier;

    public ItemSpawnerCore(int tier) {
        this.tier = tier;
        setUnlocalizedName("berlit.spawner_core_t" + tier);
        setTextureName("berlit:spawner_core_t" + tier);
        setMaxStackSize(16);
        setCreativeTab(Berlit.TAB);
    }

    @Override
    public UpgradeStats getSpawnerUpgrade(ItemStack stack) {
        switch (tier) {
            case 3:
                return new UpgradeStats(2.0F, 6);
            case 2:
                return new UpgradeStats(1.0F, 4);
            default:
                return new UpgradeStats(0.5F, 2);
        }
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return StatCollector.translateToLocal("item.berlit.spawner_core_t" + tier + ".name");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
        UpgradeStats stats = getSpawnerUpgrade(stack);
        list.add(
            EnumChatFormatting.GRAY + StatCollector
                .translateToLocalFormatted("item.berlit.spawner_core.speed", (int) (stats.getSpeedBonus() * 100)));
        list.add(
            EnumChatFormatting.GRAY
                + StatCollector.translateToLocalFormatted("item.berlit.spawner_core.amount", stats.getAmountBonus()));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs tab, List list) {
        list.add(new ItemStack(item));
    }
}
