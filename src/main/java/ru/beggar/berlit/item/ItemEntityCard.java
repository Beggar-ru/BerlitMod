package ru.beggar.berlit.item;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ru.beggar.berlit.Berlit;
import ru.beggar.berlit.spawner.EntityDefinition;
import ru.beggar.berlit.spawner.EntityDrop;
import ru.beggar.berlit.spawner.EntityRegistry;

public class ItemEntityCard extends Item {

    private static final String NBT_ENTITY = "EntityId";

    public ItemEntityCard() {
        setUnlocalizedName("berlit.entity_card");
        setTextureName("berlit:entity_card");
        setMaxStackSize(10);
        setCreativeTab(Berlit.TAB);
    }

    public static ItemStack create(String entityId) {
        ItemStack stack = new ItemStack(Berlit.entityCard);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString(NBT_ENTITY, entityId);
        stack.setTagCompound(tag);
        return stack;
    }

    public static String getEntityId(ItemStack stack) {
        if (stack == null || stack.getTagCompound() == null) return null;
        if (!stack.getTagCompound()
            .hasKey(NBT_ENTITY)) return null;
        return stack.getTagCompound()
            .getString(NBT_ENTITY);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        EntityDefinition definition = EntityRegistry.get(getEntityId(stack));
        if (definition == null) {
            return StatCollector.translateToLocal("item.berlit.entity_card.empty");
        }
        return StatCollector.translateToLocalFormatted("item.berlit.entity_card.name", definition.getDisplayName());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
        EntityDefinition definition = EntityRegistry.get(getEntityId(stack));
        if (definition == null) {
            list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("item.berlit.entity_card.unknown"));
            return;
        }

        list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("item.berlit.entity_card.drops"));
        for (EntityDrop drop : definition.getDrops()) {
            String name = drop.getStack()
                .getDisplayName();
            String chance = String.format("%.0f%%", drop.getChance() * 100.0F);
            list.add(
                EnumChatFormatting.DARK_GRAY + " - "
                    + name
                    + " "
                    + chance
                    + " ("
                    + drop.getMin()
                    + "-"
                    + drop.getMax()
                    + ")");
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs tab, List list) {
        for (EntityDefinition definition : EntityRegistry.all()) {
            list.add(create(definition.getId()));
        }
    }
}
