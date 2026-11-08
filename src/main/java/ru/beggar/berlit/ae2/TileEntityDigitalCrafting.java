package ru.beggar.berlit.ae2;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;
import cpw.mods.fml.common.Loader;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySink;
import ru.beggar.berlit.network.ModNetwork;
import ru.beggar.berlit.network.PacketDigitalSync;
import ru.beggar.berlit.upgrade.UpgradeHelper;
import ru.beggar.berlit.util.InventoryUtils;

/**
 * Shared digital-machine implementation for custom recipe sets (Avaritia 9x9 and the neutron
 * compressor). It publishes every recipe it knows as an AE2 pattern, processes queued patterns
 * with EU or ME power, supports the usual four upgrades and injects results into ME storage.
 */
public abstract class TileEntityDigitalCrafting extends TileEntity
    implements DigitalMachineView, IGridHost, IActionHost, ICraftingProvider, IEnergySink {

    public static final int UPGRADE_SLOTS = 4;
    public static final int MAX_QUEUE = 64;
    public static final int ENERGY_CAPACITY = 50000;

    public static final int STATUS_OFFLINE = 0;
    public static final int STATUS_READY = 1;
    public static final int STATUS_WORKING = 2;
    public static final int STATUS_NO_ENERGY = 3;
    public static final int STATUS_NO_RECIPES = 4;
    public static final int STATUS_QUEUE_FULL = 5;

    private static final double AE_PER_TICK = 1.0D;

    private final ItemStack[] upgrades = new ItemStack[UPGRADE_SLOTS];
    private final Deque<Job> queue = new ArrayDeque<Job>();
    private final List<ItemStack> pendingOutput = new ArrayList<ItemStack>();
    private final List<ICraftingPatternDetails> patterns = new ArrayList<ICraftingPatternDetails>();

    private Job currentJob;
    private int energy;
    private int lastInput;
    private int inputThisTick;
    private int syncCounter;
    private int recipeRefreshCounter;
    private int lastFilterRevision = -1;
    private int status = STATUS_OFFLINE;
    private boolean active;
    private boolean addedToEnergyNet;
    private IGridNode node;

    private int clientMaxEnergy = ENERGY_CAPACITY;
    private int clientRecipeCount;
    private int clientQueueSize;
    private int clientStatus;
    private int clientProgressTicks;
    private int clientRequiredTicks;
    private int clientRequiredEnergy;
    private int clientLastInput;
    private boolean clientActive;
    private ItemStack clientOutput;

    protected abstract List<ICraftingPatternDetails> createPatterns();

    protected abstract boolean acceptPattern(ICraftingPatternDetails details);

    protected abstract ICraftingPatternDetails parsePattern(ItemStack pattern);

    protected abstract int getBaseTicks(ICraftingPatternDetails details);

    protected abstract int getEnergyPerOperation(ICraftingPatternDetails details);

    protected abstract ItemStack getMachineStack();

    public abstract String getInventoryKey();

    @Override
    public abstract String getGuiTitleKey();

    @Override
    public ru.beggar.berlit.machine.MachineType getFilterMachineType() {
        return null;
    }

    private IGridNode getNode() {
        if (worldObj != null && worldObj.isRemote) return null;
        if (node == null) {
            node = AEApi.instance()
                .createGridNode(new GridBlock());
        }
        return node;
    }

    @Override
    public IGridNode getGridNode(ForgeDirection dir) {
        return getNode();
    }

    @Override
    public IGridNode getActionableNode() {
        return getNode();
    }

    @Override
    public AECableType getCableConnectionType(ForgeDirection dir) {
        return AECableType.SMART;
    }

    @Override
    public void securityBreak() {}

    @Override
    public void updateEntity() {
        super.updateEntity();
        if (worldObj == null || worldObj.isRemote) return;

        if (!addedToEnergyNet && Loader.isModLoaded("IC2")) {
            MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
            addedToEnergyNet = true;
        }

        lastInput = inputThisTick;
        inputThisTick = 0;
        UpgradeHelper.Stats stats = getUpgradeStats();
        active = false;

        if (currentJob == null && !queue.isEmpty()) {
            startNextJob(stats);
        }
        if (currentJob != null) {
            processJob();
        }
        flushOutput();

        if (++recipeRefreshCounter >= 100 || patterns.isEmpty()) {
            recipeRefreshCounter = 0;
            refreshPatterns();
        }

        int filterRevision = DigitalRecipeFilter.getRevision();
        if (filterRevision != lastFilterRevision) {
            IGrid grid = networkGrid();
            IGridNode node = getNode();
            if (grid != null && node != null) {
                lastFilterRevision = filterRevision;
                grid.postEvent(new appeng.api.networking.events.MENetworkCraftingPatternChange(this, node));
            }
        }

        status = computeStatus();
        active = active || currentJob != null;

        if (++syncCounter >= 10) {
            syncCounter = 0;
            syncToClients();
        }
    }

    private void refreshPatterns() {
        patterns.clear();
        try {
            patterns.addAll(createPatterns());
        } catch (Throwable ignored) {
            // Recipe managers can be partially initialised; retry on the next refresh.
        }
    }

    @Override
    public void provideCrafting(ICraftingProviderHelper helper) {
        if (worldObj != null && worldObj.isRemote) return;
        if (patterns.isEmpty()) refreshPatterns();
        for (ICraftingPatternDetails details : patterns) {
            if (acceptPattern(details)) helper.addCraftingOption(this, details);
        }
    }

    @Override
    public boolean pushPattern(ICraftingPatternDetails details, net.minecraft.inventory.InventoryCrafting crafting) {
        if (!acceptPattern(details)) return false;
        if (queue.size() >= MAX_QUEUE) {
            status = STATUS_QUEUE_FULL;
            return false;
        }
        if (networkGrid() == null) return false;

        queue.add(new Job(details));
        markDirty();
        return true;
    }

    @Override
    public boolean isBusy() {
        return queue.size() >= MAX_QUEUE;
    }

    private void startNextJob(UpgradeHelper.Stats stats) {
        currentJob = queue.poll();
        if (currentJob == null) return;

        double speed = Math.max(0.01D, stats.speed);
        currentJob.requiredTicks = Math.max(1, (int) Math.round(getBaseTicks(currentJob.details) / speed));
        long totalEu = Math.max(1L, (long) Math.ceil(getEnergyPerOperation(currentJob.details) * stats.energyCost));
        currentJob.euPerTick = (int) Math.max(1L, (totalEu + currentJob.requiredTicks - 1) / currentJob.requiredTicks);
        currentJob.requiredEnergy = currentJob.euPerTick * currentJob.requiredTicks;
        markDirty();
    }

    private void processJob() {
        Job job = currentJob;
        if (job == null) return;

        if (job.progressTicks >= job.requiredTicks) {
            finishJob();
            return;
        }

        if (energy >= job.euPerTick) {
            energy -= job.euPerTick;
            job.progressTicks++;
            active = true;
            status = STATUS_WORKING;
        } else if (extractAePower()) {
            job.progressTicks++;
            active = true;
            status = STATUS_WORKING;
        } else {
            status = STATUS_NO_ENERGY;
            return;
        }

        if (job.progressTicks >= job.requiredTicks) {
            finishJob();
        }
    }

    private void finishJob() {
        if (currentJob == null) return;
        pendingOutput.addAll(currentJob.outputs);
        currentJob = null;
        active = true;
        markDirty();
    }

    private boolean extractAePower() {
        IGrid grid = networkGrid();
        if (grid == null) return false;
        IEnergyGrid energyGrid = grid.getCache(IEnergyGrid.class);
        if (energyGrid == null) return false;
        if (energyGrid.extractAEPower(AE_PER_TICK, Actionable.SIMULATE, PowerMultiplier.CONFIG) < AE_PER_TICK) {
            return false;
        }
        return energyGrid.extractAEPower(AE_PER_TICK, Actionable.MODULATE, PowerMultiplier.CONFIG) >= AE_PER_TICK;
    }

    private void flushOutput() {
        if (pendingOutput.isEmpty() || worldObj == null || worldObj.isRemote) return;

        IGrid grid = networkGrid();
        if (grid != null) {
            IStorageGrid storage = grid.getCache(IStorageGrid.class);
            if (storage != null) {
                IMEMonitor<IAEItemStack> items = storage.getItemInventory();
                MachineSource source = new MachineSource(this);
                Iterator<ItemStack> iterator = pendingOutput.iterator();
                while (iterator.hasNext()) {
                    ItemStack stack = iterator.next();
                    IAEItemStack leftover = items.injectItems(
                        AEApi.instance()
                            .storage()
                            .createItemStack(stack),
                        Actionable.MODULATE,
                        source);
                    if (leftover == null || leftover.getStackSize() <= 0) {
                        iterator.remove();
                    } else {
                        stack.stackSize = (int) Math.min(Integer.MAX_VALUE, leftover.getStackSize());
                    }
                }
                markDirty();
                return;
            }
        }

        Iterator<ItemStack> iterator = pendingOutput.iterator();
        while (iterator.hasNext()) {
            ItemStack stack = iterator.next();
            ItemStack remaining = InventoryUtils.insertIntoNeighbours(worldObj, this, stack.copy());
            if (remaining == null || remaining.stackSize <= 0) {
                iterator.remove();
            } else {
                stack.stackSize = remaining.stackSize;
            }
        }
        markDirty();
    }

    private int computeStatus() {
        if (worldObj == null || worldObj.isRemote) return status;
        if (networkGrid() == null) return STATUS_OFFLINE;
        if (patterns.isEmpty()) return STATUS_NO_RECIPES;
        if (currentJob != null) {
            if (energy >= currentJob.euPerTick) return STATUS_WORKING;
            return aePowerAvailable() ? STATUS_WORKING : STATUS_NO_ENERGY;
        }
        if (!queue.isEmpty()) return STATUS_WORKING;
        return STATUS_READY;
    }

    private boolean aePowerAvailable() {
        IGrid grid = networkGrid();
        if (grid == null) return false;
        IEnergyGrid energyGrid = grid.getCache(IEnergyGrid.class);
        return energyGrid != null && energyGrid.isNetworkPowered();
    }

    private UpgradeHelper.Stats getUpgradeStats() {
        List<ItemStack> stacks = new ArrayList<ItemStack>();
        for (ItemStack upgrade : upgrades) stacks.add(upgrade);
        return UpgradeHelper.combine(stacks);
    }

    private IGrid networkGrid() {
        IGridNode own = getNode();
        IGrid grid = own == null ? null : own.getGrid();
        if (grid != null && grid.getCache(IStorageGrid.class) != null) return grid;

        if (worldObj == null) return grid;
        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            TileEntity adjacent = worldObj
                .getTileEntity(xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ);
            if (!(adjacent instanceof IGridHost)) continue;
            IGridNode other = ((IGridHost) adjacent).getGridNode(dir.getOpposite());
            if (other != null && other.getGrid() != null
                && other.getGrid()
                    .getCache(IStorageGrid.class) != null) {
                return other.getGrid();
            }
        }
        return grid;
    }

    @Override
    public void validate() {
        super.validate();
        if (worldObj != null && !worldObj.isRemote) getNode().updateState();
    }

    @Override
    public void invalidate() {
        if (!worldObj.isRemote && addedToEnergyNet) {
            MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
            addedToEnergyNet = false;
        }
        if (node != null) {
            node.destroy();
            node = null;
        }
        super.invalidate();
    }

    @Override
    public void onChunkUnload() {
        if (!worldObj.isRemote && addedToEnergyNet) {
            MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
            addedToEnergyNet = false;
        }
        if (node != null) {
            node.destroy();
            node = null;
        }
        super.onChunkUnload();
    }

    @Override
    public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction) {
        return true;
    }

    @Override
    public double getDemandedEnergy() {
        if (currentJob == null && queue.isEmpty()) return 0;
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
    public int getSizeInventory() {
        return UPGRADE_SLOTS;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return upgrades[slot];
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        if (upgrades[slot] == null) return null;
        ItemStack result;
        if (upgrades[slot].stackSize <= amount) {
            result = upgrades[slot];
            upgrades[slot] = null;
        } else {
            result = upgrades[slot].splitStack(amount);
            if (upgrades[slot].stackSize <= 0) upgrades[slot] = null;
        }
        markDirty();
        return result;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        ItemStack stack = upgrades[slot];
        upgrades[slot] = null;
        return stack;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        upgrades[slot] = stack;
        if (stack != null && stack.stackSize > getInventoryStackLimit()) stack.stackSize = getInventoryStackLimit();
        markDirty();
    }

    @Override
    public String getInventoryName() {
        return getInventoryKey();
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
        return stack != null && UpgradeHelper.isMachineUpgrade(stack);
    }

    @Override
    public int getEnergy() {
        return energy;
    }

    @Override
    public int getMaxEnergy() {
        if (worldObj != null && worldObj.isRemote) return clientMaxEnergy;
        return ENERGY_CAPACITY + getUpgradeStats().storage;
    }

    @Override
    public int getRecipeCount() {
        return worldObj != null && worldObj.isRemote ? clientRecipeCount : patterns.size();
    }

    @Override
    public int getQueueSize() {
        return worldObj != null && worldObj.isRemote ? clientQueueSize : queue.size() + (currentJob == null ? 0 : 1);
    }

    @Override
    public int getStatus() {
        return worldObj != null && worldObj.isRemote ? clientStatus : status;
    }

    public int getProgressTicks() {
        return worldObj != null && worldObj.isRemote ? clientProgressTicks
            : currentJob == null ? 0 : currentJob.progressTicks;
    }

    public int getRequiredTicks() {
        return worldObj != null && worldObj.isRemote ? clientRequiredTicks
            : currentJob == null ? 0 : currentJob.requiredTicks;
    }

    @Override
    public int getRequiredEnergy() {
        return worldObj != null && worldObj.isRemote ? clientRequiredEnergy
            : currentJob == null ? 0 : currentJob.requiredEnergy;
    }

    @Override
    public int getProgressScaled(int scale) {
        int required = getRequiredTicks();
        if (required <= 0) return 0;
        return Math.min(scale, getProgressTicks() * scale / required);
    }

    @Override
    public int getLastInput() {
        return worldObj != null && worldObj.isRemote ? clientLastInput : lastInput;
    }

    @Override
    public boolean isActive() {
        return worldObj != null && worldObj.isRemote ? clientActive : active;
    }

    @Override
    public ItemStack getCurrentOutput() {
        if (worldObj != null && worldObj.isRemote) return clientOutput == null ? null : clientOutput.copy();
        if (currentJob == null || currentJob.outputs.isEmpty()) return null;
        return currentJob.outputs.get(0)
            .copy();
    }

    public void setClientData(int energy, int maxEnergy, int progressTicks, int requiredTicks, int requiredEnergy,
        int queueSize, int recipeCount, int status, int lastInput, boolean active, ItemStack output) {
        this.energy = energy;
        this.clientMaxEnergy = maxEnergy;
        this.clientProgressTicks = progressTicks;
        this.clientRequiredTicks = requiredTicks;
        this.clientRequiredEnergy = requiredEnergy;
        this.clientQueueSize = queueSize;
        this.clientRecipeCount = recipeCount;
        this.clientStatus = status;
        this.clientLastInput = lastInput;
        this.clientActive = active;
        this.clientOutput = output == null ? null : output.copy();
    }

    private void syncToClients() {
        if (worldObj == null || worldObj.isRemote) return;
        for (Object object : worldObj.playerEntities) {
            EntityPlayerMP player = (EntityPlayerMP) object;
            if (player.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 4096.0D) {
                ModNetwork.INSTANCE.sendTo(
                    new PacketDigitalSync(
                        xCoord,
                        yCoord,
                        zCoord,
                        energy,
                        getMaxEnergy(),
                        getProgressTicks(),
                        getRequiredTicks(),
                        getRequiredEnergy(),
                        getQueueSize(),
                        getRecipeCount(),
                        status,
                        lastInput,
                        active,
                        getCurrentOutput()),
                    player);
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        energy = nbt.getInteger("Energy");

        NBTTagList itemList = nbt.getTagList("Items", 10);
        for (int i = 0; i < itemList.tagCount(); i++) {
            NBTTagCompound tag = itemList.getCompoundTagAt(i);
            int slot = tag.getByte("Slot") & 255;
            if (slot >= 0 && slot < upgrades.length) upgrades[slot] = ItemStack.loadItemStackFromNBT(tag);
        }

        queue.clear();
        NBTTagList queueList = nbt.getTagList("Queue", 10);
        for (int i = 0; i < queueList.tagCount(); i++) {
            Job job = jobFromPattern(ItemStack.loadItemStackFromNBT(queueList.getCompoundTagAt(i)));
            if (job != null) queue.add(job);
        }

        currentJob = null;
        NBTTagCompound currentTag = nbt.getCompoundTag("Current");
        if (currentTag != null && !currentTag.hasNoTags()) {
            Job job = jobFromPattern(ItemStack.loadItemStackFromNBT(currentTag.getCompoundTag("Pattern")));
            if (job != null) {
                job.progressTicks = currentTag.getInteger("Progress");
                job.requiredTicks = Math.max(1, currentTag.getInteger("Ticks"));
                job.euPerTick = Math.max(1, currentTag.getInteger("EuPerTick"));
                job.requiredEnergy = currentTag.getInteger("RequiredEnergy");
                currentJob = job;
            }
        }

        pendingOutput.clear();
        NBTTagList pendingList = nbt.getTagList("Pending", 10);
        for (int i = 0; i < pendingList.tagCount(); i++) {
            ItemStack stack = ItemStack.loadItemStackFromNBT(pendingList.getCompoundTagAt(i));
            if (stack != null) pendingOutput.add(stack);
        }

        patterns.clear();
        recipeRefreshCounter = 99;
        status = STATUS_OFFLINE;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("Energy", energy);

        NBTTagList itemList = new NBTTagList();
        for (int i = 0; i < upgrades.length; i++) {
            if (upgrades[i] != null) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setByte("Slot", (byte) i);
                upgrades[i].writeToNBT(tag);
                itemList.appendTag(tag);
            }
        }
        nbt.setTag("Items", itemList);

        NBTTagList queueList = new NBTTagList();
        for (Job job : queue) {
            NBTTagCompound tag = new NBTTagCompound();
            if (job.details.getPattern() != null) job.details.getPattern()
                .writeToNBT(tag);
            queueList.appendTag(tag);
        }
        nbt.setTag("Queue", queueList);

        if (currentJob != null) {
            NBTTagCompound currentTag = new NBTTagCompound();
            NBTTagCompound patternTag = new NBTTagCompound();
            if (currentJob.details.getPattern() != null) currentJob.details.getPattern()
                .writeToNBT(patternTag);
            currentTag.setTag("Pattern", patternTag);
            currentTag.setInteger("Progress", currentJob.progressTicks);
            currentTag.setInteger("Ticks", currentJob.requiredTicks);
            currentTag.setInteger("EuPerTick", currentJob.euPerTick);
            currentTag.setInteger("RequiredEnergy", currentJob.requiredEnergy);
            nbt.setTag("Current", currentTag);
        }

        NBTTagList pendingList = new NBTTagList();
        for (ItemStack stack : pendingOutput) {
            NBTTagCompound tag = new NBTTagCompound();
            stack.writeToNBT(tag);
            pendingList.appendTag(tag);
        }
        nbt.setTag("Pending", pendingList);
    }

    private Job jobFromPattern(ItemStack pattern) {
        if (pattern == null) return null;
        try {
            ICraftingPatternDetails details = parsePattern(pattern);
            return details != null && acceptPattern(details) ? new Job(details) : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    public List<ICraftingPatternDetails> getPatternsForTest() {
        if (patterns.isEmpty()) refreshPatterns();
        return new ArrayList<ICraftingPatternDetails>(patterns);
    }

    public boolean queuePatternForTest(ICraftingPatternDetails details) {
        if (!acceptPattern(details)) return false;
        queue.add(new Job(details));
        return true;
    }

    public List<ItemStack> getPendingOutputForTest() {
        return pendingOutput;
    }

    private final class Job {

        private final ICraftingPatternDetails details;
        private final List<ItemStack> outputs;
        private int progressTicks;
        private int requiredTicks;
        private int euPerTick;
        private int requiredEnergy;

        private Job(ICraftingPatternDetails details) {
            this.details = details;
            this.outputs = new ArrayList<ItemStack>();
            for (IAEItemStack output : details.getOutputs()) {
                if (output == null) continue;
                ItemStack stack = output.getItemStack();
                if (stack != null) this.outputs.add(stack.copy());
            }
        }
    }

    private final class GridBlock implements IGridBlock {

        @Override
        public double getIdlePowerUsage() {
            return 2.0D;
        }

        @Override
        public java.util.EnumSet<GridFlags> getFlags() {
            return java.util.EnumSet.of(GridFlags.REQUIRE_CHANNEL);
        }

        @Override
        public boolean isWorldAccessible() {
            return true;
        }

        @Override
        public DimensionalCoord getLocation() {
            return new DimensionalCoord(TileEntityDigitalCrafting.this);
        }

        @Override
        public AEColor getGridColor() {
            return AEColor.Transparent;
        }

        @Override
        public void onGridNotification(appeng.api.networking.GridNotification notification) {}

        @Override
        public void setNetworkStatus(IGrid grid, int channels) {}

        @Override
        public java.util.EnumSet<ForgeDirection> getConnectableSides() {
            return java.util.EnumSet.allOf(ForgeDirection.class);
        }

        @Override
        public IGridHost getMachine() {
            return TileEntityDigitalCrafting.this;
        }

        @Override
        public void gridChanged() {}

        @Override
        public ItemStack getMachineRepresentation() {
            return getMachineStack();
        }
    }
}
