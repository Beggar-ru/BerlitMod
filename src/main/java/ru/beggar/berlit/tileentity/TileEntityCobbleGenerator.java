package ru.beggar.berlit.tileentity;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
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

/**
 * Single-tier cobblestone generator. It exposes 27 chest-like output slots and keeps every slot
 * filled with a full stack of 64 cobblestone while it is powered.
 */
public class TileEntityCobbleGenerator extends TileEntity implements IEnergySink, IInventory {

    public static final int SLOT_COUNT = 27;

    /** Constant draw while the generator keeps the slots topped up. */
    public static final int ENERGY_PER_TICK = 10000;

    /** Two ticks of buffer, so a single missed packet does not stop the refill. */
    public static final int ENERGY_CAPACITY = ENERGY_PER_TICK * 2;

    private final ItemStack[] inventory = new ItemStack[SLOT_COUNT];
    private int energy;
    private boolean active;
    private boolean addedToEnergyNet;

    public TileEntityCobbleGenerator() {}

    @Override
    public void updateEntity() {
        super.updateEntity();
        if (worldObj == null || worldObj.isRemote) return;

        loadEnergyNet();

        // Nothing is generated for free: without a full tick of energy the slots stay as they are.
        if (energy < ENERGY_PER_TICK) {
            active = false;
            return;
        }
        energy -= ENERGY_PER_TICK;
        active = true;

        boolean changed = false;
        for (int i = 0; i < SLOT_COUNT; i++) {
            ItemStack current = inventory[i];
            if (current == null) {
                inventory[i] = new ItemStack(Blocks.cobblestone, 64);
                changed = true;
                continue;
            }

            if (current.getItem() != Item.getItemFromBlock(Blocks.cobblestone) || current.getItemDamage() != 0) {
                // Remove leftovers of the old tier/upgrade layout instead of silently deleting them.
                worldObj.spawnEntityInWorld(
                    new EntityItem(worldObj, xCoord + 0.5D, yCoord + 0.8D, zCoord + 0.5D, current.copy()));
                inventory[i] = new ItemStack(Blocks.cobblestone, 64);
                changed = true;
            } else if (current.stackSize < 64) {
                inventory[i] = new ItemStack(Blocks.cobblestone, 64);
                changed = true;
            }
        }

        if (changed) markDirty();
    }

    private void loadEnergyNet() {
        if (addedToEnergyNet || !Loader.isModLoaded("IC2")) return;
        // The smoke test drives a detached tile; only real, placed blocks join the energy net.
        if (worldObj.getTileEntity(xCoord, yCoord, zCoord) != this) return;

        MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
        addedToEnergyNet = true;
    }

    /** Energy held in the buffer. */
    public int getEnergy() {
        return energy;
    }

    /** True when the generator got its energy this tick and topped the slots up. */
    public boolean isPowered() {
        return active;
    }

    // ----- IC2 energy sink -----

    @Override
    public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction) {
        return true;
    }

    @Override
    public double getDemandedEnergy() {
        return Math.max(0, Math.min(ENERGY_PER_TICK, ENERGY_CAPACITY - energy));
    }

    @Override
    public int getSinkTier() {
        return 5;
    }

    @Override
    public double injectEnergy(ForgeDirection directionFrom, double amount, double voltage) {
        double accepted = Math.min(amount, Math.max(0, ENERGY_CAPACITY - energy));
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
        if (stack != null && stack.stackSize > getInventoryStackLimit()) {
            stack.stackSize = getInventoryStackLimit();
        }
        markDirty();
    }

    @Override
    public String getInventoryName() {
        return "tile.berlit.cobble_generator.name";
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
        return false;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
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
