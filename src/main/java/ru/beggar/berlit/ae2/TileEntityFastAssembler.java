package ru.beggar.berlit.ae2;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;

import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import ru.beggar.berlit.Berlit;

/** Fast 3x3 molecular assembler: standard AE2 patterns, instant crafts. */
public class TileEntityFastAssembler extends TileEntityAvaritiaAssembler {

    @Override
    protected ItemStack machineStack() {
        return new ItemStack(Berlit.fastAssembler);
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return stack != null && stack.getItem() instanceof ICraftingPatternItem;
    }

    @Override
    public boolean pushPattern(ICraftingPatternDetails details, InventoryCrafting crafting) {
        if (worldObj == null || worldObj.isRemote) return false;

        ItemStack result = details.getOutput(crafting, worldObj);
        if (result == null) return false;

        queueOutput(result);
        return true;
    }
}
