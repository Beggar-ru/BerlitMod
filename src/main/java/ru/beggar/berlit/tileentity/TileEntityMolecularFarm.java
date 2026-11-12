package ru.beggar.berlit.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.common.Loader;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySink;
import ru.beggar.berlit.farm.FarmRecipe;
import ru.beggar.berlit.farm.FarmRecipes;

/**
 * Molecular farm: one seed slot and six output slots. Seeds are never consumed; each harvest
 * multiplies the recipe output by the number of seeds in the input stack.
 *
 * <p>
 * A harvest costs {@link #ENERGY_PER_ITEM} EU per item it produces, so the machine only works
 * while it is fed. The progress bar shows how much of the next harvest is already paid for.
 */
public class TileEntityMolecularFarm extends TileEntity implements IEnergySink, IInventory {

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT_START = 1;
    public static final int SLOT_OUTPUT_COUNT = 6;
    public static final int SLOT_COUNT = SLOT_OUTPUT_START + SLOT_OUTPUT_COUNT;

    /** EU per produced item: a full 64-seed wheat harvest costs 64 wheat + 64 seeds worth. */
    public static final int ENERGY_PER_ITEM = 1024;

    /** Buffer floor; it grows to one harvest when that needs more. */
    public static final int ENERGY_CAPACITY = 10000;

    private final ItemStack[] inventory = new ItemStack[SLOT_COUNT];
    private int energy;
    private int requiredEnergy;
    private int progress;
    private boolean addedToEnergyNet;

    @Override
    public void updateEntity() {
        super.updateEntity();
        if (worldObj == null || worldObj.isRemote) return;

        loadEnergyNet();

        ItemStack seeds = inventory[SLOT_INPUT];
        FarmRecipe recipe = FarmRecipes.find(seeds);
        if (recipe == null || seeds == null || seeds.stackSize <= 0) {
            progress = 0;
            requiredEnergy = 0;
            return;
        }

        requiredEnergy = ENERGY_PER_ITEM * itemsPerHarvest(recipe, seeds.stackSize);
        if (!canFit(recipe, seeds.stackSize)) {
            progress = 0;
            return;
        }

        if (energy >= requiredEnergy) {
            produce(recipe, seeds.stackSize);
            energy -= requiredEnergy;
            progress = 0;
        } else {
            progress = requiredEnergy <= 0 ? 0 : energy * 100 / requiredEnergy;
        }
    }

    /** Total number of items one harvest would put into the output slots. */
    private static int itemsPerHarvest(FarmRecipe recipe, int seedCount) {
        int items = 0;
        for (ItemStack output : recipe.getOutputsView()) {
            items += output.stackSize * seedCount;
        }
        return items;
    }

    private void loadEnergyNet() {
        if (addedToEnergyNet || !Loader.isModLoaded("IC2")) return;
        // The smoke test drives a detached tile; only real, placed blocks join the energy net.
        if (worldObj.getTileEntity(xCoord, yCoord, zCoord) != this) return;

        MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
        addedToEnergyNet = true;
    }

    // ----- IC2 energy sink -----

    @Override
    public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction) {
        return true;
    }

    @Override
    public double getDemandedEnergy() {
        return Math.max(0, getMaxEnergy() - energy);
    }

    @Override
    public int getSinkTier() {
        return 4;
    }

    @Override
    public double injectEnergy(ForgeDirection directionFrom, double amount, double voltage) {
        double accepted = Math.min(amount, Math.max(0, getMaxEnergy() - energy));
        energy += (int) accepted;
        return amount - accepted;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (!worldObj.isRemote && addedToEnergyNet) {
            MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
            addedToEnergyNet = false;
        }
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        if (!worldObj.isRemote && addedToEnergyNet) {
            MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
            addedToEnergyNet = false;
        }
    }

    private boolean canFit(FarmRecipe recipe, int seedCount) {
        for (ItemStack output : recipe.getOutputsView()) {
            if (capacity(output) < output.stackSize * seedCount) return false;
        }
        return true;
    }

    private int capacity(ItemStack output) {
        int capacity = 0;
        for (int i = SLOT_OUTPUT_START; i < SLOT_COUNT; i++) {
            ItemStack slot = inventory[i];
            if (slot == null) {
                capacity += 64;
            } else if (sameItem(slot, output) && slot.stackSize < 64) {
                capacity += 64 - slot.stackSize;
            }
        }
        return capacity;
    }

    private void produce(FarmRecipe recipe, int seedCount) {
        for (ItemStack output : recipe.getOutputsView()) {
            int remaining = output.stackSize * seedCount;
            for (int i = SLOT_OUTPUT_START; i < SLOT_COUNT && remaining > 0; i++) {
                ItemStack slot = inventory[i];
                if (slot == null) {
                    ItemStack copy = output.copy();
                    copy.stackSize = Math.min(64, remaining);
                    inventory[i] = copy;
                    remaining -= copy.stackSize;
                } else if (sameItem(slot, output) && slot.stackSize < 64) {
                    int moved = Math.min(64 - slot.stackSize, remaining);
                    slot.stackSize += moved;
                    remaining -= moved;
                }
            }
        }
        markDirty();
    }

    private static boolean sameItem(ItemStack a, ItemStack b) {
        return a.getItem() == b.getItem() && a.getItemDamage() == b.getItemDamage()
            && ItemStack.areItemStackTagsEqual(a, b);
    }

    /** Progress towards the next harvest, in percent. */
    public int getProgress() {
        return progress;
    }

    public int getProgressScaled(int scale) {
        return Math.min(scale, progress * scale / 100);
    }

    public int getEnergy() {
        return energy;
    }

    /** At least one harvest is buffered, however many items that batch produces. */
    public int getMaxEnergy() {
        return Math.max(ENERGY_CAPACITY, requiredEnergy);
    }

    public int getRequiredEnergy() {
        return requiredEnergy;
    }

    public int getSeedCount() {
        return inventory[SLOT_INPUT] == null ? 0 : inventory[SLOT_INPUT].stackSize;
    }

    @Override
    public int getSizeInventory() {
        return SLOT_COUNT;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return slot >= 0 && slot < SLOT_COUNT ? inventory[slot] : null;
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        if (slot < 0 || slot >= SLOT_COUNT || inventory[slot] == null) return null;

        ItemStack result;
        if (inventory[slot].stackSize <= amount) {
            result = inventory[slot];
            inventory[slot] = null;
        } else {
            result = inventory[slot].splitStack(amount);
            if (inventory[slot].stackSize <= 0) inventory[slot] = null;
        }
        if (slot == SLOT_INPUT) progress = 0;
        markDirty();
        return result;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        if (slot < 0 || slot >= SLOT_COUNT) return null;
        ItemStack stack = inventory[slot];
        inventory[slot] = null;
        return stack;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        if (slot < 0 || slot >= SLOT_COUNT) return;
        inventory[slot] = stack;
        if (stack != null && stack.stackSize > getInventoryStackLimit()) stack.stackSize = getInventoryStackLimit();
        if (slot == SLOT_INPUT) progress = 0;
        markDirty();
    }

    @Override
    public String getInventoryName() {
        return "tile.berlit.molecular_farm.name";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this
            && player.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 64.0D;
    }

    @Override
    public void openInventory() {}

    @Override
    public void closeInventory() {}

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (slot != SLOT_INPUT || stack == null) return false;
        return FarmRecipes.find(stack) != null;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        progress = nbt.getInteger("Progress");
        energy = nbt.getInteger("Energy");
        NBTTagList list = nbt.getTagList("Items", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            int slot = tag.getByte("Slot") & 255;
            if (slot >= 0 && slot < SLOT_COUNT) inventory[slot] = ItemStack.loadItemStackFromNBT(tag);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("Progress", progress);
        nbt.setInteger("Energy", energy);
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (inventory[i] != null) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setByte("Slot", (byte) i);
                inventory[i].writeToNBT(tag);
                list.appendTag(tag);
            }
        }
        nbt.setTag("Items", list);
    }
}
