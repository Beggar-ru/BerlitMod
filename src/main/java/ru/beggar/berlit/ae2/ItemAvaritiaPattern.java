package ru.beggar.berlit.ae2;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.crafting.ICraftingPatternDetails;
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
}
