package ru.beggar.berlit.tileentity;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import cpw.mods.fml.common.Loader;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySink;
import ru.beggar.berlit.network.ModNetwork;
import ru.beggar.berlit.network.PacketReplicatorSync;
import ru.beggar.berlit.recipe.ReplicatorPattern;
import ru.beggar.berlit.recipe.ReplicatorRecipes;
import ru.beggar.berlit.upgrade.UpgradeHelper;
import ru.beggar.berlit.util.InventoryUtils;
import ru.beggar.berlit.util.ReplicatorFluid;

/**
 * IC2-inspired replicator with an instant scanner instead of crystal memories.
 *
 * <p>
 * The scan slot is checked every tick. As soon as an item is placed there its UU value is read
 * from {@link ReplicatorRecipes} (IC2's UU graph by default) and the item is remembered. Selecting
 * a remembered pattern and supplying liquid UU matter makes the machine build one whole stack per
 * operation; the matter cost of the operation is the per-item cost multiplied by the stack size.
 */
public class TileEntityReplicator extends TileEntity implements IEnergySink, IInventory, IFluidHandler {

    public static final int SLOT_SCAN = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int SLOT_FLUID_INPUT = 2;
    public static final int SLOT_EMPTY_OUTPUT = 3;
    public static final int SLOT_UPGRADE_START = 4;
    public static final int SLOT_UPGRADE_COUNT = 4;
    public static final int SLOT_COUNT = SLOT_UPGRADE_START + SLOT_UPGRADE_COUNT;

    /** 16 cells of UU matter, like the IC2 replicator's internal tank. */
    public static final int TANK_CAPACITY = 16000;

    public static final int ENERGY_CAPACITY = 100000;

    /** EU accepted per tick at full speed (upgrades modify this). */
    public static final double EU_PER_TICK = 32.0D;

    /** Millibuckets of UU matter consumed per tick at full speed. */
    public static final double MATTER_PER_TICK = 1.0D;

    public static final int MAX_PATTERNS = 1024;

    public static final int STATUS_NO_PATTERN = 0;
    public static final int STATUS_READY = 1;
    public static final int STATUS_NO_VALUE = 2;
    public static final int STATUS_OUTPUT_FULL = 3;
    public static final int STATUS_NO_MATTER = 4;
    public static final int STATUS_NO_ENERGY = 5;
    public static final int STATUS_PATTERN_LIMIT = 6;

    private final ItemStack[] inventory = new ItemStack[SLOT_COUNT];
    private final List<ReplicatorPattern> patterns = new ArrayList<ReplicatorPattern>();
    private final FluidTank tank = new FluidTank(TANK_CAPACITY);

    private int energy;
    private double progress;
    private double matterBuffer;

    private int patternIndex = -1;
    private int patternCount;
    private int matterPerItem;
    private int stackSize;
    private int requiredMatter;
    private int status = STATUS_NO_PATTERN;
    private ItemStack currentPattern;
    private String lastScanKey;
    private int scanStatus = STATUS_NO_PATTERN;

    private boolean active;
    private boolean addedToEnergyNet;
    private int syncCounter;
    private int lastInput;
    private int inputThisTick;

    // Client-side mirrors of the values sent by {@link PacketReplicatorSync}.
    private int clientFluid;
    private int clientFluidCapacity = TANK_CAPACITY;
    private int clientMaxEnergy = ENERGY_CAPACITY;
    private ItemStack clientPattern;

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

        drainFluidContainer();
        scanSlot();
        refreshCurrentPattern();

        active = false;

        if (currentPattern == null) {
            progress = 0;
            matterBuffer = 0;
            status = patterns.isEmpty() && scanStatus == STATUS_NO_VALUE ? STATUS_NO_VALUE : STATUS_NO_PATTERN;
        } else if (progress >= requiredMatter) {
            finishOperation();
        } else if (tank.getFluidAmount() <= 0) {
            status = STATUS_NO_MATTER;
        } else {
            process(stats);
        }

        if (stats.ejector) {
            ejectOutputs();
        }

        if (++syncCounter >= 10) {
            syncCounter = 0;
            syncToClients();
        }
    }

    private void process(UpgradeHelper.Stats stats) {
        double demand = EU_PER_TICK * stats.energyCost;
        if (demand <= 0.0D) demand = 1.0D;

        int euUsed = (int) Math.min(energy, demand);
        if (euUsed <= 0) {
            status = STATUS_NO_ENERGY;
            return;
        }

        energy -= euUsed;
        double factor = euUsed / demand;

        // Keep the fractional remainder so even a trickle of EU slowly fills the operation.
        matterBuffer += MATTER_PER_TICK * stats.speed * factor;

        int whole = (int) Math.floor(matterBuffer + 1.0E-9D);
        if (whole <= 0) return;

        int remaining = requiredMatter - (int) Math.floor(progress);
        int amount = Math.min(whole, Math.min(remaining, tank.getFluidAmount()));
        if (amount <= 0) {
            matterBuffer = Math.min(matterBuffer, 1.0D);
            return;
        }

        FluidStack drained = tank.drain(amount, true);
        if (drained == null || drained.amount <= 0) {
            status = STATUS_NO_MATTER;
            return;
        }

        progress += drained.amount;
        matterBuffer -= drained.amount;
        if (matterBuffer < 0.0D) matterBuffer = 0.0D;

        active = true;
        status = STATUS_READY;

        if (progress >= requiredMatter) {
            finishOperation();
        }
    }

    private void finishOperation() {
        if (!canOutput()) {
            status = STATUS_OUTPUT_FULL;
            return;
        }

        outputStack();
        progress = 0.0D;
        matterBuffer = 0.0D;
        active = true;
        status = STATUS_READY;
    }

    private void refreshCurrentPattern() {
        patternCount = patterns.size();

        if (patterns.isEmpty()) {
            patternIndex = -1;
            currentPattern = null;
            matterPerItem = 0;
            stackSize = 0;
            requiredMatter = 0;
            return;
        }

        if (patternIndex < 0 || patternIndex >= patterns.size()) {
            patternIndex = 0;
            progress = 0.0D;
            matterBuffer = 0.0D;
        }

        ReplicatorPattern pattern = patterns.get(patternIndex);
        currentPattern = pattern.getStack();
        matterPerItem = pattern.getMatterPerItem();
        stackSize = pattern.getStackSize();

        long required = (long) matterPerItem * stackSize;
        requiredMatter = required > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) required;

        if (progress > requiredMatter + 1.0D) {
            progress = 0.0D;
            matterBuffer = 0.0D;
        }
    }

    private void scanSlot() {
        ItemStack scan = inventory[SLOT_SCAN];
        String key = ReplicatorPattern.key(scan);

        if (key == null) {
            lastScanKey = null;
            return;
        }
        if (key.equals(lastScanKey)) return;

        lastScanKey = key;

        ReplicatorPattern pattern = ReplicatorRecipes.createPattern(scan);
        if (pattern == null) {
            scanStatus = STATUS_NO_VALUE;
            return;
        }

        int existing = findPattern(pattern.getStack());
        if (existing >= 0) {
            ReplicatorPattern old = patterns.get(existing);
            boolean costChanged = old.getMatterPerItem() != pattern.getMatterPerItem();
            old.setMatterPerItem(pattern.getMatterPerItem());

            if (existing != patternIndex) {
                setPatternIndex(existing);
            } else if (costChanged) {
                progress = 0.0D;
                matterBuffer = 0.0D;
            }
        } else {
            if (patterns.size() >= MAX_PATTERNS) {
                scanStatus = STATUS_PATTERN_LIMIT;
                return;
            }
            patterns.add(pattern);
            setPatternIndex(patterns.size() - 1);
        }

        scanStatus = STATUS_READY;
        markDirty();
        refreshCurrentPattern();
        syncToClients();
    }

    private int findPattern(ItemStack stack) {
        for (int i = 0; i < patterns.size(); i++) {
            if (ReplicatorPattern.sameItem(
                patterns.get(i)
                    .getStack(),
                stack)) return i;
        }
        return -1;
    }

    private void setPatternIndex(int index) {
        if (index < 0 || index >= patterns.size()) {
            index = patterns.isEmpty() ? -1 : 0;
        }
        if (index == patternIndex) return;

        patternIndex = index;
        progress = 0.0D;
        matterBuffer = 0.0D;
        markDirty();
    }

    /** Called from the GUI buttons; action 0 is previous, anything else is next. */
    public void handleAction(int action) {
        if (patterns.isEmpty()) return;

        int delta = action == 0 ? -1 : 1;
        int next = patternIndex + delta;
        next %= patterns.size();
        if (next < 0) next += patterns.size();

        setPatternIndex(next);
        refreshCurrentPattern();
        syncToClients();
    }

    private void drainFluidContainer() {
        ItemStack input = inventory[SLOT_FLUID_INPUT];
        if (input == null) return;

        FluidStack fluid = ReplicatorFluid.getContainerFluid(input);
        if (fluid == null || fluid.amount <= 0) return;
        if (tank.fill(fluid.copy(), false) < fluid.amount) return;

        ItemStack empty = ReplicatorFluid.drainContainer(input);
        if (empty == null) return;

        if (input.stackSize > 1 && !canAddToSlot(SLOT_EMPTY_OUTPUT, empty)) return;

        int filled = tank.fill(fluid.copy(), true);
        if (filled <= 0) return;

        if (input.stackSize <= 1) {
            inventory[SLOT_FLUID_INPUT] = empty.copy();
        } else {
            input.stackSize--;
            addToSlot(SLOT_EMPTY_OUTPUT, empty);
        }
        markDirty();
    }

    private boolean canAddToSlot(int slot, ItemStack stack) {
        ItemStack existing = inventory[slot];
        if (existing == null) return true;
        if (!ReplicatorPattern.sameItem(existing, stack)) return false;

        int limit = Math.min(getInventoryStackLimit(), existing.getMaxStackSize());
        return existing.stackSize < limit;
    }

    private void addToSlot(int slot, ItemStack stack) {
        ItemStack existing = inventory[slot];
        if (existing == null) {
            inventory[slot] = stack.copy();
            inventory[slot].stackSize = 1;
        } else if (ReplicatorPattern.sameItem(existing, stack)
            && existing.stackSize < Math.min(getInventoryStackLimit(), existing.getMaxStackSize())) {
                existing.stackSize++;
            }
    }

    private boolean canOutput() {
        if (currentPattern == null || stackSize <= 0) return false;

        ItemStack output = inventory[SLOT_OUTPUT];
        if (output == null) return stackSize <= getInventoryStackLimit();

        if (!ReplicatorPattern.sameItem(output, currentPattern)) return false;

        int limit = Math.min(getInventoryStackLimit(), output.getMaxStackSize());
        return output.stackSize + stackSize <= limit;
    }

    private void outputStack() {
        ItemStack output = inventory[SLOT_OUTPUT];
        if (output == null) {
            ItemStack result = currentPattern.copy();
            result.stackSize = stackSize;
            inventory[SLOT_OUTPUT] = result;
        } else {
            output.stackSize += stackSize;
        }
        markDirty();
    }

    private void ejectOutputs() {
        int[] slots = { SLOT_OUTPUT, SLOT_EMPTY_OUTPUT };
        for (int slot : slots) {
            ItemStack stack = inventory[slot];
            if (stack == null) continue;

            ItemStack remaining = InventoryUtils.insertIntoNeighbours(worldObj, this, stack.copy());
            inventory[slot] = remaining == null || remaining.stackSize <= 0 ? null : remaining;
            markDirty();
        }
    }

    private UpgradeHelper.Stats getUpgradeStats() {
        List<ItemStack> stacks = new ArrayList<ItemStack>();
        for (int i = SLOT_UPGRADE_START; i < SLOT_COUNT; i++) {
            stacks.add(inventory[i]);
        }
        return UpgradeHelper.combine(stacks);
    }

    private void syncToClients() {
        if (worldObj == null || worldObj.isRemote) return;

        for (Object object : worldObj.playerEntities) {
            EntityPlayerMP player = (EntityPlayerMP) object;
            if (player.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 4096.0D) {
                ModNetwork.INSTANCE.sendTo(
                    new PacketReplicatorSync(
                        xCoord,
                        yCoord,
                        zCoord,
                        energy,
                        getMaxEnergy(),
                        getFluidAmount(),
                        TANK_CAPACITY,
                        (int) progress,
                        requiredMatter,
                        matterPerItem,
                        stackSize,
                        patternIndex,
                        patternCount,
                        status,
                        lastInput,
                        active,
                        currentPattern),
                    player);
            }
        }
    }

    /** Applies the data received from the server (client side). */
    public void setClientData(int energy, int maxEnergy, int fluid, int fluidCapacity, int progress, int requiredMatter,
        int matterPerItem, int stackSize, int patternIndex, int patternCount, int status, int lastInput, boolean active,
        ItemStack pattern) {
        this.energy = energy;
        this.clientMaxEnergy = maxEnergy;
        this.clientFluid = fluid;
        this.clientFluidCapacity = fluidCapacity;
        this.progress = progress;
        this.requiredMatter = requiredMatter;
        this.matterPerItem = matterPerItem;
        this.stackSize = stackSize;
        this.patternIndex = patternIndex;
        this.patternCount = patternCount;
        this.status = status;
        this.lastInput = lastInput;
        this.active = active;
        this.clientPattern = pattern == null ? null : pattern.copy();
    }

    public int getEnergy() {
        return energy;
    }

    public int getMaxEnergy() {
        if (worldObj != null && worldObj.isRemote) return clientMaxEnergy;
        return ENERGY_CAPACITY + getUpgradeStats().storage;
    }

    public int getFluidAmount() {
        if (worldObj != null && worldObj.isRemote) return clientFluid;
        return tank.getFluidAmount();
    }

    public int getFluidCapacity() {
        if (worldObj != null && worldObj.isRemote) return clientFluidCapacity;
        return TANK_CAPACITY;
    }

    public int getProgress() {
        return (int) progress;
    }

    public int getProgressScaled(int scale) {
        if (requiredMatter <= 0) return 0;
        int scaled = (int) (progress * scale / requiredMatter);
        return Math.max(0, Math.min(scale, scaled));
    }

    public int getRequiredMatter() {
        return requiredMatter;
    }

    public int getMatterPerItem() {
        return matterPerItem;
    }

    public int getMatterPerStack() {
        long amount = (long) matterPerItem * stackSize;
        return amount > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) amount;
    }

    public int getStackSize() {
        return stackSize;
    }

    public int getPatternIndex() {
        return patternIndex;
    }

    public int getPatternCount() {
        return patternCount;
    }

    public int getStatus() {
        return status;
    }

    public int getLastInput() {
        return lastInput;
    }

    public boolean isActive() {
        return active;
    }

    public ItemStack getCurrentPattern() {
        ItemStack pattern = worldObj != null && worldObj.isRemote ? clientPattern : currentPattern;
        return pattern == null ? null : pattern.copy();
    }

    // ----- IC2 energy sink -----

    @Override
    public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction) {
        return true;
    }

    @Override
    public double getDemandedEnergy() {
        if (currentPattern == null || requiredMatter <= (int) progress) return 0;
        if (tank.getFluidAmount() <= 0) return 0;
        return Math.max(0, getMaxEnergy() - energy);
    }

    @Override
    public int getSinkTier() {
        return Math.min(5, 4 + getUpgradeStats().tierBonus);
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

    // ----- fluid handler -----

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        if (!ReplicatorFluid.isUuMatter(resource)) return 0;
        return tank.fill(resource, doFill);
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        return null;
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        return null;
    }

    @Override
    public boolean canFill(ForgeDirection from, net.minecraftforge.fluids.Fluid fluid) {
        return ReplicatorFluid.isUuMatter(fluid);
    }

    @Override
    public boolean canDrain(ForgeDirection from, net.minecraftforge.fluids.Fluid fluid) {
        return false;
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        return new FluidTankInfo[] { tank.getInfo() };
    }

    // ----- inventory -----

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
            if (inventory[slot].stackSize <= 0) {
                inventory[slot] = null;
            }
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
        if (stack != null && stack.stackSize > getInventoryStackLimit()) {
            stack.stackSize = getInventoryStackLimit();
        }
        markDirty();
    }

    @Override
    public String getInventoryName() {
        return "container.berlit.replicator";
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

        if (slot == SLOT_SCAN) return true;
        if (slot == SLOT_FLUID_INPUT) return ReplicatorFluid.isUuMatterContainer(stack);
        if (slot >= SLOT_UPGRADE_START && slot < SLOT_COUNT) return UpgradeHelper.isMachineUpgrade(stack);

        return false;
    }

    // ----- NBT -----

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        energy = nbt.getInteger("Energy");
        progress = nbt.getDouble("Progress");
        matterBuffer = nbt.getDouble("MatterBuffer");
        patternIndex = nbt.getInteger("PatternIndex");
        scanStatus = STATUS_NO_PATTERN;
        lastScanKey = null;

        patterns.clear();
        NBTTagList patternList = nbt.getTagList("Patterns", 10);
        for (int i = 0; i < patternList.tagCount(); i++) {
            NBTTagCompound entry = patternList.getCompoundTagAt(i);
            ItemStack stack = ItemStack.loadItemStackFromNBT(entry.getCompoundTag("Item"));
            int matter = entry.getInteger("Matter");
            if (stack != null && matter > 0) {
                patterns.add(new ReplicatorPattern(stack, matter));
            }
        }

        NBTTagCompound tankTag = nbt.getCompoundTag("Tank");
        if (tankTag != null) {
            tank.readFromNBT(tankTag);
        }

        NBTTagList itemList = nbt.getTagList("Items", 10);
        for (int i = 0; i < itemList.tagCount(); i++) {
            NBTTagCompound tag = itemList.getCompoundTagAt(i);
            int slot = tag.getByte("Slot") & 255;
            if (slot >= 0 && slot < inventory.length) {
                inventory[slot] = ItemStack.loadItemStackFromNBT(tag);
            }
        }

        refreshCurrentPattern();
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        nbt.setInteger("Energy", energy);
        nbt.setDouble("Progress", progress);
        nbt.setDouble("MatterBuffer", matterBuffer);
        nbt.setInteger("PatternIndex", patternIndex);

        NBTTagList patternList = new NBTTagList();
        for (ReplicatorPattern pattern : patterns) {
            NBTTagCompound entry = new NBTTagCompound();
            NBTTagCompound itemTag = new NBTTagCompound();
            pattern.getStack()
                .writeToNBT(itemTag);
            entry.setTag("Item", itemTag);
            entry.setInteger("Matter", pattern.getMatterPerItem());
            patternList.appendTag(entry);
        }
        nbt.setTag("Patterns", patternList);

        NBTTagCompound tankTag = new NBTTagCompound();
        tank.writeToNBT(tankTag);
        nbt.setTag("Tank", tankTag);

        NBTTagList itemList = new NBTTagList();
        for (int i = 0; i < inventory.length; i++) {
            if (inventory[i] != null) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setByte("Slot", (byte) i);
                inventory[i].writeToNBT(tag);
                itemList.appendTag(tag);
            }
        }
        nbt.setTag("Items", itemList);
    }
}
