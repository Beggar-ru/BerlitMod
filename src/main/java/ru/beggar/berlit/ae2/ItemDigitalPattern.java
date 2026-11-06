package ru.beggar.berlit.ae2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import ru.beggar.berlit.Berlit;
import ru.beggar.berlit.machine.MachineRecipe;
import ru.beggar.berlit.machine.MachineType;

/**
 * Hidden item that carries one auto-generated digital machine recipe. Players never obtain it:
 * it is only used by AE2 to display and persist a crafting task for a digital machine.
 */
public class ItemDigitalPattern extends Item implements ICraftingPatternItem {

    public ItemDigitalPattern() {
        setUnlocalizedName("berlit.digital_pattern");
        setTextureName("berlit:digital_pattern");
        setMaxStackSize(1);
        setCreativeTab(null);
    }

    public static ItemStack create(MachineType type, ItemStack input, List<ItemStack> outputs, int energy) {
        if (Berlit.digitalPattern == null || type == null || input == null || outputs == null || outputs.isEmpty()) {
            return null;
        }

        ItemStack pattern = new ItemStack(Berlit.digitalPattern);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("machine", type.getId());

        NBTTagCompound in = new NBTTagCompound();
        input.writeToNBT(in);
        tag.setTag("in", in);

        NBTTagList out = new NBTTagList();
        for (ItemStack output : outputs) {
            if (output == null) continue;
            NBTTagCompound entry = new NBTTagCompound();
            output.writeToNBT(entry);
            out.appendTag(entry);
        }
        tag.setTag("out", out);
        tag.setInteger("energy", energy);

        pattern.setTagCompound(tag);
        return pattern;
    }

    public static ItemStack create(MachineType type, MachineRecipe recipe) {
        if (recipe == null || recipe.getInputs()
            .isEmpty()) return null;
        return create(
            type,
            recipe.getInputs()
                .get(0),
            recipe.getOutputs(),
            recipe.getEnergy());
    }

    @Override
    public ICraftingPatternDetails getPatternForItem(ItemStack stack, World world) {
        if (stack == null || stack.getItem() != this || !stack.hasTagCompound()) return null;
        try {
            DigitalPatternDetails details = new DigitalPatternDetails(stack);
            return details.isValid() ? details : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static List<ItemStack> copyOutputs(List<ItemStack> outputs) {
        if (outputs == null) return Collections.emptyList();
        List<ItemStack> result = new ArrayList<ItemStack>(outputs.size());
        for (ItemStack output : outputs) {
            if (output != null) result.add(output.copy());
        }
        return result;
    }
}
