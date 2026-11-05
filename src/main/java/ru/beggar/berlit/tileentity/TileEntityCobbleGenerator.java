package ru.beggar.berlit.tileentity;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
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
import ru.beggar.berlit.network.ModNetwork;
import ru.beggar.berlit.network.PacketMachineSync;
import ru.beggar.berlit.upgrade.UpgradeHelper;
import ru.beggar.berlit.util.InventoryUtils;

/**
 * Cobblestone generator: turns EU into cobblestone. Three tiers differ in speed, energy use and buffer size; the usual
 * IC2 upgrades (overclocker, transformer, energy storage) and the Berlit accelerators scale it further. The output is
 * pushed into any adjacent inventory.
 */
public class TileEntityCobbleGenerator extends TileEntity implements IEnergySink, IInventory {

    public static final int TIER_COUNT = 3;

    /** Ticks per cobblestone for each tier. */
    private static final int[] BASE_TICKS = { 40, 20, 10 };
    /** EU consumed per tick for each tier. */
    private static final int[] EU_PER_TICK = { 8, 16, 32 };
    private static final int[] ENERGY_CAPACITY = { 8000, 8000, 8000 };
    private static final int[] SINK_TIER = { 4, 4, 4 };

    public static final int SLOT_OUTPUT = 0;
    public static final int SLOT_UPGRADE_START = 1;
    public static final int SLOT_UPGRADE_COUNT = 4;
    public static final int SLOT_COUNT = 1 + SLOT_UPGRADE_COUNT;

    private static final String[] NAME_KEYS = { "tile.berlit.cobble_generator.name",
        "tile.berlit.cobble_generator_mk2.name", "tile.berlit.cobble_generator_mk3.name" };

    private final ItemStack[] inventory = new ItemStack[SLOT_COUNT];

    private final int tier;

    private int energy;
    private double progress;
    private boolean active;
    private boolean addedToEnergyNet;
    private int syncCounter;
    private int lastInput;
    private int inputThisTick;

    public TileEntityCobbleGenerator() {
        this(0);
    }

    public TileEntityCobbleGenerator(int tier) {
        this.tier = Math.max(0, Math.min(TIER_COUNT - 1, tier));
    }

    public int getTier() {
        return tier;
    }

    private int baseTicks() {
        return BASE_TICKS[tier];
    }

    private int euPerTick() {
        return EU_PER_TICK[tier];
    }

    // ---------------------------------------------------------------------
    // Machine logic
    // ---------------------------------------------------------------------

    @Override
    public void updateEntity() {
        super.updateEntity();
        if (worldObj.isRemote) return;

        if (!addedToEnergyNet && Loader.isModLoaded("IC2")) {
            MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
            addedToEnergyNet = true;
        }

        lastInput = inputThisTick;
        inputThisTick = 0;

        UpgradeHelper.Stats stats = getUpgradeStats();
        active = false;

        if (progress >= baseTicks()) {
            if (canAcceptOutput()) {
                produce();
                progress = 0.0D;
            }
        } else if (canAcceptOutput()) {
            double needed = euPerTick() * stats.energyCost;
            if (energy >= needed) {
                energy -= needed;
                progress = Math.min(baseTicks(), progress + stats.speed);
                active = true;
            }
        }

        if (inventory[SLOT_OUTPUT] != null) {
            ejectOutput();
        }

        if (++syncCounter >= 10) {
            syncCounter = 0;
            syncToClients();
        }
    }

    private UpgradeHelper.Stats getUpgradeStats() {
        List<ItemStack> stacks = new ArrayList<ItemStack>();
        for (int i = SLOT_UPGRADE_START; i < SLOT_COUNT; i++) {
            stacks.add(inventory[i]);
        }
        return UpgradeHelper.combine(stacks);
    }

    private boolean canAcceptOutput() {
        ItemStack slot = inventory[SLOT_OUTPUT];
        if (slot == null) return true;
        return slot.getItem() == Item.getItemFromBlock(Blocks.cobblestone) && slot.getItemDamage() == 0
            && slot.stackSize < getInventoryStackLimit();
    }

    private void produce() {
        ItemStack slot = inventory[SLOT_OUTPUT];
        if (slot == null) {
            inventory[SLOT_OUTPUT] = new ItemStack(Blocks.cobblestone);
        } else {
            slot.stackSize++;
        }
        markDirty();
    }

    private void ejectOutput() {
        ItemStack stack = inventory[SLOT_OUTPUT];
        if (stack == null) return;
        ItemStack remaining = InventoryUtils.insertIntoNeighbours(worldObj, this, stack.copy());
        inventory[SLOT_OUTPUT] = remaining == null || remaining.stackSize <= 0 ? null : remaining;
    }

    private void syncToClients() {
        for (Object object : worldObj.playerEntities) {
            EntityPlayerMP player = (EntityPlayerMP) object;
            if (player.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 4096.0D) {
                ModNetwork.INSTANCE.sendTo(
                    new PacketMachineSync(
                        xCoord,
                        yCoord,
                        zCoord,
                        energy,
                        (int) progress,
                        baseTicks(),
                        lastInput,
                        0,
                        active),
                    player);
            }
        }
    }

    // ---------------------------------------------------------------------
    // GUI data
    // ---------------------------------------------------------------------

    public int getEnergy() {
        return energy;
    }

    public int getMaxEnergy() {
        return ENERGY_CAPACITY[tier] + getUpgradeStats().storage;
    }

    public int getProgress() {
        return (int) progress;
    }

    public int getProgressScaled(int scale) {
        int base = baseTicks();
        if (base <= 0) return 0;
        return Math.min(scale, (int) (progress * scale / base));
    }

    public int getLastInput() {
        return lastInput;
    }

    public boolean isActive() {
        return active;
    }

    public void setClientData(int energy, int progress, int recipeEnergy, int lastInput, boolean active) {
        this.energy = energy;
        this.progress = progress;
        this.lastInput = lastInput;
        this.active = active;
    }

    // ---------------------------------------------------------------------
    // IC2 energy
    // ---------------------------------------------------------------------

    @Override
    public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction) {
        return true;
    }

    @Override
    public double getDemandedEnergy() {
        if (!canAcceptOutput()) return 0;
        return Math.max(0, getMaxEnergy() - energy);
    }

    @Override
    public int getSinkTier() {
        return Math.min(5, SINK_TIER[tier] + getUpgradeStats().tierBonus);
    }

    @Override
    public double injectEnergy(ForgeDirection directionFrom, double amount, double voltage) {
        double accepted = Math.min(amount, getDemandedEnergy());
        energy += (int) accepted;
        inputThisTick += (int) accepted;
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

    // ---------------------------------------------------------------------
    // Inventory
    // ---------------------------------------------------------------------

    @Override
    public int getSizeInventory() {
        return SLOT_COUNT;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return inventory[slot];
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        if (inventory[slot] == null) return null;
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
        ItemStack stack = inventory[slot];
        inventory[slot] = null;
        return stack;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        inventory[slot] = stack;
        if (stack != null && stack.stackSize > getInventoryStackLimit()) stack.stackSize = getInventoryStackLimit();
        markDirty();
    }

    @Override
    public String getInventoryName() {
        return NAME_KEYS[tier];
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
        if (stack == null) return false;
        if (slot == SLOT_OUTPUT) return false;
        return slot >= SLOT_UPGRADE_START && slot < SLOT_COUNT && UpgradeHelper.isMachineUpgrade(stack);
    }

    // ---------------------------------------------------------------------
    // NBT
    // ---------------------------------------------------------------------

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        energy = nbt.getInteger("Energy");
        progress = nbt.getDouble("Progress");
        NBTTagList list = nbt.getTagList("Items", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            int slot = tag.getByte("Slot") & 255;
            if (slot >= 0 && slot < inventory.length) inventory[slot] = ItemStack.loadItemStackFromNBT(tag);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("Energy", energy);
        nbt.setDouble("Progress", progress);
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < inventory.length; i++) {
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
