package ru.beggar.berlit.ae2;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import ru.beggar.berlit.machine.MachineType;

/** State exposed by every digital ME machine to the shared status/config GUI. */
public interface DigitalMachineView extends IInventory {

    String getGuiTitleKey();

    /** Machine type whose recipes can be filtered, or null for custom recipe sets. */
    MachineType getFilterMachineType();

    int getStatus();

    int getRecipeCount();

    int getQueueSize();

    int getProgressScaled(int scale);

    int getLastInput();

    int getEnergy();

    int getMaxEnergy();

    int getRequiredEnergy();

    ItemStack getCurrentOutput();

    boolean isActive();

    boolean isUseableByPlayer(EntityPlayer player);
}
