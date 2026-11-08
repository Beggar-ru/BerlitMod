package ru.beggar.berlit.tileentity;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;

import cpw.mods.fml.common.Loader;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.item.IC2Items;
import ru.beggar.berlit.Berlit;
import ru.beggar.berlit.block.BlockMatterGenerator;
import ru.beggar.berlit.matter.MatterType;
import ru.beggar.berlit.network.ModNetwork;
import ru.beggar.berlit.network.PacketMachineSync;

/**
 * Converts matter of one kind into the next kind: scrap into {@link MatterType#STANDARD} for the first generator,
 * then every generator consumes the previous kind.
 */
public class TileEntityMatterGenerator extends TileEntity implements IEnergySink, IInventory {

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int SLOT_COUNT = 2;

    private static final int SYNC_INTERVAL = 10;

    private MatterType type;
    private final ItemStack[] inventory = new ItemStack[SLOT_COUNT];
    private int energy;
    private int progress;
    private boolean addedToEnergyNet;
    private int syncTimer;

    // client mirrors, filled by {@link PacketMachineSync}
    private int clientEnergy;
    private int clientProgress;
    private int clientCost;
    private boolean clientActive;

    public TileEntityMatterGenerator() {}

    public TileEntityMatterGenerator(MatterType type) {
        this.type = type;
    }

    /** Falls back to the block when the tile was recreated by the chunk loader. */
    public MatterType getMatterType() {
        if (type == null) {
            Block block = getBlockType();
            type = block instanceof BlockMatterGenerator ? ((BlockMatterGenerator) block).getMatterType()
                : MatterType.STANDARD;
        }
        return type;
    }

    public int getEnergy() {
        return worldObj != null && worldObj.isRemote ? clientEnergy : energy;
    }

    public int getProgress() {
        return worldObj != null && worldObj.isRemote ? clientProgress : progress;
    }

    public int getEnergyPerOperation() {
        return worldObj != null && worldObj.isRemote ? clientCost : getMatterType().getEnergyPerOperation();
    }

    public boolean isActive() {
        return worldObj != null && worldObj.isRemote ? clientActive : progress > 0;
    }

    public int getProgressPercent() {
        int ticks = getMatterType().getTicksPerOperation();
        int value = getProgress();
        return ticks <= 0 ? 0 : Math.min(100, value * 100 / ticks);
    }

    // ------------------------------------------------------------------ ticking

    @Override
    public void updateEntity() {
        super.updateEntity();
        if (worldObj == null || worldObj.isRemote) return;

        loadEnergyNet();

        MatterType current = getMatterType();
        ItemStack expected = getInputFor(current);
        ItemStack result = getOutputFor(current);
        int ticks = current.getTicksPerOperation();
        int perTick = Math.max(1, current.getEnergyPerOperation() / ticks);

        boolean canWork = expected != null && inventory[SLOT_INPUT] != null
            && inventory[SLOT_INPUT].isItemEqual(expected)
            && canStore(result);

        if (!canWork) {
            if (progress != 0) {
                progress = 0;
                markDirty();
            }
        } else if (energy >= perTick) {
            energy -= perTick;
            progress++;
            if (progress >= ticks) {
                finishOperation(result);
            }
        }

        if (++syncTimer >= SYNC_INTERVAL) {
            syncTimer = 0;
            syncToPlayers();
        }
    }

    private void finishOperation(ItemStack result) {
        inventory[SLOT_INPUT].stackSize--;
        if (inventory[SLOT_INPUT].stackSize <= 0) {
            inventory[SLOT_INPUT] = null;
        }

        if (inventory[SLOT_OUTPUT] == null) {
            inventory[SLOT_OUTPUT] = result.copy();
        } else {
            inventory[SLOT_OUTPUT].stackSize += result.stackSize;
        }

        progress = 0;
        markDirty();
    }

    private boolean canStore(ItemStack result) {
        if (result == null) return false;
        ItemStack slot = inventory[SLOT_OUTPUT];
        if (slot == null) return true;
        if (!slot.isItemEqual(result)) return false;
        return slot.stackSize + result.stackSize <= slot.getMaxStackSize();
    }

    /** Scrap for the first generator, the previous matter kind for the rest. */
    public ItemStack getInputFor(MatterType current) {
        if (current.isFirst()) {
            return Loader.isModLoaded("IC2") ? IC2Items.getItem("scrap") : null;
        }
        return new ItemStack(
            Berlit.matterItems[current.getPrevious()
                .ordinal()]);
    }

    public ItemStack getOutputFor(MatterType current) {
        return new ItemStack(Berlit.matterItems[current.ordinal()]);
    }

    private void loadEnergyNet() {
        if (addedToEnergyNet || !Loader.isModLoaded("IC2")) return;
        MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
        addedToEnergyNet = true;
    }

    private void syncToPlayers() {
        for (Object object : worldObj.playerEntities) {
            if (!(object instanceof EntityPlayerMP)) continue;
            EntityPlayerMP player = (EntityPlayerMP) object;
            if (player.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) > 4096.0D) continue;
            ModNetwork.INSTANCE.sendTo(
                new PacketMachineSync(
                    xCoord,
                    yCoord,
                    zCoord,
                    energy,
                    progress,
                    getMatterType().getEnergyPerOperation(),
                    0,
                    getMatterType().ordinal(),
                    progress > 0),
                player);
        }
    }

    public void setClientData(int energy, int progress, int cost, boolean active) {
        this.clientEnergy = energy;
        this.clientProgress = progress;
        this.clientCost = cost;
        this.clientActive = active;
    }

    /** Called when the block is broken, so the contents are not lost. */
    public void dropContents() {
        if (worldObj == null || worldObj.isRemote) return;
        for (ItemStack stack : inventory) {
            if (stack == null) continue;
            EntityItem entity = new EntityItem(worldObj, xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D, stack.copy());
            worldObj.spawnEntityInWorld(entity);
        }
    }

    // ----------------------------------------------------------------- energy

    @Override
    public boolean acceptsEnergyFrom(TileEntity emitter, net.minecraftforge.common.util.ForgeDirection direction) {
        return true;
    }

    @Override
    public double getDemandedEnergy() {
        return Math.max(0, getMatterType().getEnergyCapacity() - energy);
    }

    @Override
    public int getSinkTier() {
        return 4;
    }

    @Override
    public double injectEnergy(net.minecraftforge.common.util.ForgeDirection direction, double amount, double voltage) {
        int capacity = getMatterType().getEnergyCapacity();
        int accepted = (int) Math.min(amount, capacity - energy);
        energy += accepted;
        return amount - accepted;
    }

    @Override
    public void invalidate() {
        if (addedToEnergyNet && Loader.isModLoaded("IC2")) {
            MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
            addedToEnergyNet = false;
        }
        super.invalidate();
    }

    @Override
    public void onChunkUnload() {
        if (addedToEnergyNet && Loader.isModLoaded("IC2")) {
            MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
            addedToEnergyNet = false;
        }
        super.onChunkUnload();
    }

    // --------------------------------------------------------------- inventory

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
        ItemStack stack = inventory[slot];
        if (stack == null) return null;
        if (stack.stackSize <= amount) {
            inventory[slot] = null;
            markDirty();
            return stack;
        }
        ItemStack split = stack.splitStack(amount);
        if (stack.stackSize <= 0) inventory[slot] = null;
        markDirty();
        return split;
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
        if (stack != null && stack.stackSize > getInventoryStackLimit()) {
            stack.stackSize = getInventoryStackLimit();
        }
        markDirty();
    }

    @Override
    public String getInventoryName() {
        return "container.berlit.matter_generator";
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
        return worldObj != null && worldObj.getTileEntity(xCoord, yCoord, zCoord) == this
            && player.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 64.0D;
    }

    @Override
    public void openInventory() {}

    @Override
    public void closeInventory() {}

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (slot == SLOT_OUTPUT) return false;
        ItemStack expected = getInputFor(getMatterType());
        return expected != null && stack != null && stack.isItemEqual(expected);
    }

    // -------------------------------------------------------------------- nbt

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("energy", energy);
        tag.setInteger("progress", progress);
        tag.setInteger("matter", getMatterType().ordinal());

        NBTTagList items = new NBTTagList();
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (inventory[i] == null) continue;
            NBTTagCompound entry = new NBTTagCompound();
            entry.setByte("slot", (byte) i);
            inventory[i].writeToNBT(entry);
            items.appendTag(entry);
        }
        tag.setTag("items", items);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        energy = tag.getInteger("energy");
        progress = tag.getInteger("progress");
        type = MatterType.byIndex(tag.getInteger("matter"));

        java.util.Arrays.fill(inventory, null);
        NBTTagList items = tag.getTagList("items", 10);
        for (int i = 0; i < items.tagCount(); i++) {
            NBTTagCompound entry = items.getCompoundTagAt(i);
            int slot = entry.getByte("slot") & 0xFF;
            if (slot < SLOT_COUNT) {
                inventory[slot] = ItemStack.loadItemStackFromNBT(entry);
            }
        }
    }
}
