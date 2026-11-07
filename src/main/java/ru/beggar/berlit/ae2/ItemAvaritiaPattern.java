package ru.beggar.berlit.ae2;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ru.beggar.berlit.Berlit;

/** AE2-compatible pattern for the 9x9 Avaritia table. */
public class ItemAvaritiaPattern extends Item implements ICraftingPatternItem {

    public static final int GRID = 9;
    public static final int SLOTS = GRID * GRID;

    public ItemAvaritiaPattern() {
        setUnlocalizedName("berlit.avaritia_pattern");
        setTextureName("berlit:avaritia_pattern");
        setMaxStackSize(1);
        setCreativeTab(Berlit.TAB);
    }

    public static ItemStack create(ItemStack[] grid, ItemStack output) {
        ItemStack pattern = new ItemStack(Berlit.avaritiaPattern);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean("crafting", false);
        tag.setBoolean("substitute", false);

        NBTTagList in = new NBTTagList();
        for (int i = 0; i < SLOTS; i++) {
            NBTTagCompound entry = new NBTTagCompound();
            if (i < grid.length && grid[i] != null) {
                grid[i].writeToNBT(entry);
            }
            in.appendTag(entry);
        }
        tag.setTag("in", in);

        NBTTagList out = new NBTTagList();
        NBTTagCompound outTag = new NBTTagCompound();
        output.writeToNBT(outTag);
        out.appendTag(outTag);
        tag.setTag("out", out);

        pattern.setTagCompound(tag);
        return pattern;
    }

    @Override
    public ICraftingPatternDetails getPatternForItem(ItemStack stack, World world) {
        if (stack == null || !stack.hasTagCompound()) return null;
        return new AvaritiaPatternDetails(stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
        if (stack == null || !stack.hasTagCompound()) return;

        NBTTagCompound tag = stack.getTagCompound();
        NBTTagList outputList = tag.getTagList("out", 10);
        if (outputList.tagCount() > 0) {
            ItemStack output = ItemStack.loadItemStackFromNBT(outputList.getCompoundTagAt(0));
            if (output != null) {
                list.add(
                    EnumChatFormatting.GOLD + StatCollector
                        .translateToLocalFormatted("item.berlit.pattern.result", output.getDisplayName()));
            }
        }

        NBTTagList inputs = tag.getTagList("in", 10);
        Map<String, Integer> counts = new LinkedHashMap<String, Integer>();
        for (int i = 0; i < inputs.tagCount(); i++) {
            NBTTagCompound entry = inputs.getCompoundTagAt(i);
            if (entry.hasNoTags()) continue;

            ItemStack input = ItemStack.loadItemStackFromNBT(entry);
            if (input == null) continue;

            String name = input.getDisplayName();
            Integer count = counts.get(name);
            counts.put(name, (count == null ? 0 : count) + Math.max(1, input.stackSize));
        }

        if (!counts.isEmpty()) {
            list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("item.berlit.pattern.inputs"));
            int shown = 0;
            for (Map.Entry<String, Integer> entry : counts.entrySet()) {
                if (shown++ >= 6) {
                    list.add(
                        EnumChatFormatting.DARK_GRAY
                            + StatCollector.translateToLocalFormatted("item.berlit.pattern.more", counts.size() - 6));
                    break;
                }
                list.add(EnumChatFormatting.DARK_GRAY + " " + entry.getValue() + "x " + entry.getKey());
            }
        }
    }
}
