package ru.beggar.berlit.ae2;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ru.beggar.berlit.Berlit;

/** AE2 pattern for the neutron compressor: cost items -> singularity. */
public class ItemSingularityPattern extends Item implements ICraftingPatternItem {

    public ItemSingularityPattern() {
        setUnlocalizedName("berlit.singularity_pattern");
        setTextureName("berlit:singularity_pattern");
        setMaxStackSize(1);
        setCreativeTab(Berlit.TAB);
    }

    public static ItemStack create(ItemStack input, int cost, ItemStack output) {
        ItemStack pattern = new ItemStack(Berlit.singularityPattern);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean("crafting", false);
        tag.setBoolean("substitute", false);
        tag.setInteger("cost", cost);

        NBTTagCompound in = new NBTTagCompound();
        input.writeToNBT(in);
        in.setInteger("Count", 1);
        tag.setTag("input", in);

        NBTTagCompound out = new NBTTagCompound();
        output.writeToNBT(out);
        tag.setTag("out", out);

        pattern.setTagCompound(tag);
        return pattern;
    }

    @Override
    public ICraftingPatternDetails getPatternForItem(ItemStack stack, World world) {
        if (stack == null || !stack.hasTagCompound()) return null;
        return new SingularityPatternDetails(stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
        if (stack == null || !stack.hasTagCompound()) return;

        NBTTagCompound tag = stack.getTagCompound();
        ItemStack input = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("input"));
        ItemStack output = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("out"));
        int cost = tag.getInteger("cost");

        if (output != null) {
            list.add(
                EnumChatFormatting.GOLD
                    + StatCollector.translateToLocalFormatted("item.berlit.pattern.result", output.getDisplayName()));
        }
        if (input != null) {
            list.add(
                EnumChatFormatting.GRAY + StatCollector.translateToLocalFormatted(
                    "item.berlit.singularity_pattern.input",
                    Math.max(1, cost),
                    input.getDisplayName()));
        }
        list.add(
            EnumChatFormatting.DARK_GRAY
                + StatCollector.translateToLocalFormatted("item.berlit.singularity_pattern.cost", cost));
    }
}
