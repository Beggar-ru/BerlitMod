package ru.beggar.berlit.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import ru.beggar.berlit.farm.FarmRecipes;
import ru.beggar.berlit.tileentity.TileEntityMolecularFarm;

public class ContainerMolecularFarm extends Container {

    public static final int INPUT_X = 32;
    public static final int INPUT_Y = 34;
    public static final int OUTPUT_X = 92;
    public static final int OUTPUT_Y = 26;

    private static final int SLOT_SIZE = MachineLayout.SLOT;

    private final TileEntityMolecularFarm tile;
    private int clientProgress;
    private int lastProgress = -1;

    public ContainerMolecularFarm(InventoryPlayer playerInventory, TileEntityMolecularFarm tile) {
        this.tile = tile;

        addSlotToContainer(new Slot(tile, TileEntityMolecularFarm.SLOT_INPUT, INPUT_X, INPUT_Y) {

            @Override
            public boolean isItemValid(ItemStack stack) {
                return FarmRecipes.find(stack) != null;
            }
        });

        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 3; col++) {
                int index = col + row * 3;
                addSlotToContainer(
                    new Slot(
                        tile,
                        TileEntityMolecularFarm.SLOT_OUTPUT_START + index,
                        OUTPUT_X + col * SLOT_SIZE,
                        OUTPUT_Y + row * SLOT_SIZE) {

                        @Override
                        public boolean isItemValid(ItemStack stack) {
                            return false;
                        }
                    });
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(
                    new Slot(
                        playerInventory,
                        col + row * 9 + 9,
                        MachineLayout.PLAYER_X + col * SLOT_SIZE,
                        MachineLayout.PLAYER_TOP + row * SLOT_SIZE));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(
                new Slot(playerInventory, col, MachineLayout.PLAYER_X + col * SLOT_SIZE, MachineLayout.HOTBAR_Y));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return tile.isUseableByPlayer(player);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        int progress = tile.getProgress();
        if (progress == lastProgress) return;
        if (progress != 0 && progress != TileEntityMolecularFarm.TICKS_PER_HARVEST
            && Math.abs(progress - lastProgress) < 5) return;

        lastProgress = progress;
        for (Object crafter : crafters) {
            ((ICrafting) crafter).sendProgressBarUpdate(this, 0, progress);
        }
    }

    @Override
    public void updateProgressBar(int id, int data) {
        if (id == 0) clientProgress = data;
    }

    public int getClientProgress() {
        return clientProgress;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack result = null;
        Slot slot = (Slot) inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            result = stack.copy();

            int machineSlots = TileEntityMolecularFarm.SLOT_COUNT;
            if (index < machineSlots) {
                if (!mergeItemStack(stack, machineSlots, inventorySlots.size(), true)) return null;
            } else if (FarmRecipes.find(stack) != null) {
                if (!mergeItemStack(stack, 0, 1, false)) return null;
            } else {
                return null;
            }

            if (stack.stackSize == 0) {
                slot.putStack(null);
            } else {
                slot.onSlotChanged();
            }
        }

        return result;
    }
}
