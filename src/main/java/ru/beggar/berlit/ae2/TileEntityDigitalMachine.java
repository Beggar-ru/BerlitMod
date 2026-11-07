package ru.beggar.berlit.ae2;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
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
import appeng.api.networking.events.MENetworkCraftingPatternChange;
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
import ru.beggar.berlit.Berlit;
import ru.beggar.berlit.machine.MachineType;
import ru.beggar.berlit.network.ModNetwork;
import ru.beggar.berlit.network.PacketDigitalSync;
import ru.beggar.berlit.upgrade.UpgradeHelper;
import ru.beggar.berlit.util.InventoryUtils;

/**
 * Digital version of a Berlit processing machine.
 *
 * <p>
 * When it joins an ME network it publishes every recipe of its {@link MachineType} as an AE2
 * crafting pattern, so no encoded pattern items or pattern slots are needed. The ME crafting CPU
 * pushes a pattern in, the machine processes it using EU (or ME power as a fallback) and injects
 * the result back into the network. The four upgrade slots work exactly like the other machines.
 */
public class TileEntityDigitalMachine extends TileEntity
    implements IGridHost, IActionHost, ICraftingProvider, IEnergySink, IInventory {

    public static final int UPGRADE_SLOTS = 4;

    public static final int ENERGY_CAPACITY = 50000;

    /** How many pushed patterns can wait in the machine queue. */
    public static final int MAX_QUEUE = 64;

    /** Base processing time at speed 1; overclockers and accelerators reduce it. */
    public static final int BASE_TICKS = 40;

    /** AE extracted per tick when no EU is available. */
    public static final double AE_PER_TICK = 1.0D;

    public static final int STATUS_OFFLINE = 0;
    public static final int STATUS_READY = 1;
    public static final int STATUS_WORKING = 2;
    public static final int STATUS_NO_ENERGY = 3;
    public static final int STATUS_NO_RECIPES = 4;
    public static final int STATUS_QUEUE_FULL = 5;

    private MachineType type;
    private final ItemStack[] upgrades = new ItemStack[UPGRADE_SLOTS];
    private final Deque<Job> queue = new ArrayDeque<Job>();
    private final List<ItemStack> pendingOutput = new ArrayList<ItemStack>();

    private Job currentJob;
    private int energy;
    private int lastInput;
    private int inputThisTick;
    private int syncCounter;
    private int recipeCount;
    private int recipeRefreshCounter;
    private int lastFilterRevision = -1;
    private int status = STATUS_OFFLINE;
    private boolean active;
    private boolean addedToEnergyNet;
    private IGridNode node;

    // Client-side mirrors of PacketDigitalSync.
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

    public TileEntityDigitalMachine() {
        this(MachineType.MACERATOR);
    }

    public TileEntityDigitalMachine(MachineType type) {
        this.type = type == null ? MachineType.MACERATOR : type;
    }

    public MachineType getMachineType() {
        return type;
    }

    public int getMode() {
        return type.getDefaultMode();
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
        if (worldObj.isRemote) return;

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

        if (recipeRefreshCounter == 0 && recipeCount <= 0) {
            refreshRecipeCount();
        }
        if (++recipeRefreshCounter >= 100) {
            recipeRefreshCounter = 0;
            refreshRecipeCount();
        }

        int filterRevision = DigitalRecipeFilter.getRevision();
        if (filterRevision != lastFilterRevision) {
            IGrid grid = networkGrid();
            IGridNode node = getNode();
            if (grid != null && node != null) {
                lastFilterRevision = filterRevision;
                grid.postEvent(new MENetworkCraftingPatternChange(this, node));
            }
        }

        status = computeStatus();
        active = active || currentJob != null;

        if (++syncCounter >= 10) {
            syncCounter = 0;
            syncToClients();
        }
    }

    private void startNextJob(UpgradeHelper.Stats stats) {
        currentJob = queue.poll();
        if (currentJob == null) return;

        double speed = Math.max(0.01D, stats.speed);
        currentJob.requiredTicks = Math.max(1, (int) Math.round(BASE_TICKS / speed));

        long totalEu = Math.max(1L, (long) Math.ceil(currentJob.details.getEnergy() * stats.energyCost));
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

        for (ItemStack output : currentJob.outputs) {
            if (output != null) pendingOutput.add(output.copy());
        }
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
        if (pendingOutput.isEmpty()) return;

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

        // No ME storage: do not void the result, push it into a neighbouring inventory if possible.
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
        if (recipeCount <= 0) return STATUS_NO_RECIPES;

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
        for (ItemStack upgrade : upgrades) {
            stacks.add(upgrade);
        }
        return UpgradeHelper.combine(stacks);
    }

    /**
     * Registers every enabled recipe of this machine, including every concrete ore-dictionary
     * variant. The global filter lets the player disable duplicated/unwanted patterns.
     */
    private int buildPatterns(ICraftingProviderHelper helper) {
        int count = 0;

        for (DigitalRecipeFilter.Entry entry : DigitalRecipeFilter.getEntries(type, getMode())) {
            if (!DigitalRecipeFilter.isEnabled(worldObj, type, entry)) continue;

            if (helper == null) {
                count++;
                continue;
            }

            DigitalPatternDetails details = new DigitalPatternDetails(
                type,
                entry.getInput(),
                entry.getOutputs(),
                entry.getEnergy());
            if (!details.isValid()) continue;

            helper.addCraftingOption(this, details);
            count++;
        }

        recipeCount = count;
        return count;
    }

    @Override
    public void provideCrafting(ICraftingProviderHelper helper) {
        if (worldObj != null && worldObj.isRemote) return;
        buildPatterns(helper);
    }

    private void refreshRecipeCount() {
        buildPatterns(null);
    }

    @Override
    public boolean pushPattern(ICraftingPatternDetails details, net.minecraft.inventory.InventoryCrafting crafting) {
        if (!(details instanceof DigitalPatternDetails)) return false;

        DigitalPatternDetails pattern = (DigitalPatternDetails) details;
        if (!pattern.isValid() || pattern.getMachineType() != type) return false;

        if (queue.size() >= MAX_QUEUE) {
            status = STATUS_QUEUE_FULL;
            return false;
        }
        if (networkGrid() == null) return false;

        queue.add(new Job(pattern));
        markDirty();
        return true;
    }

    /** Test hook: queue a pattern without a live ME grid. */
    boolean queuePatternForTest(DigitalPatternDetails pattern) {
        if (pattern == null || !pattern.isValid() || pattern.getMachineType() != type) return false;
        queue.add(new Job(pattern));
        return true;
    }

    /** Test hook: output that has been processed but not yet injected into an ME network. */
    List<ItemStack> getPendingOutputForTest() {
        return pendingOutput;
    }

    @Override
    public boolean isBusy() {
        return queue.size() >= MAX_QUEUE;
    }

    @Override
    public void validate() {
        super.validate();
        if (worldObj != null && !worldObj.isRemote) {
            getNode().updateState();
        }
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

    // ----- IC2 energy sink -----

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

    // ----- inventory (upgrade slots only) -----

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
        return type.getDigitalContainerKey();
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

    // ----- public state for the GUI -----

    public int getEnergy() {
        return energy;
    }

    public int getMaxEnergy() {
        if (worldObj != null && worldObj.isRemote) return clientMaxEnergy;
        return ENERGY_CAPACITY + getUpgradeStats().storage;
    }

    public int getRecipeCount() {
        return worldObj != null && worldObj.isRemote ? clientRecipeCount : recipeCount;
    }

    public int getQueueSize() {
        return worldObj != null && worldObj.isRemote ? clientQueueSize : queue.size() + (currentJob == null ? 0 : 1);
    }

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

    public int getRequiredEnergy() {
        return worldObj != null && worldObj.isRemote ? clientRequiredEnergy
            : currentJob == null ? 0 : currentJob.requiredEnergy;
    }

    public int getProgressScaled(int scale) {
        int required = getRequiredTicks();
        if (required <= 0) return 0;
        return Math.min(scale, getProgressTicks() * scale / required);
    }

    public int getLastInput() {
        return worldObj != null && worldObj.isRemote ? clientLastInput : lastInput;
    }

    public boolean isActive() {
        return worldObj != null && worldObj.isRemote ? clientActive : active;
    }

    public ItemStack getCurrentOutput() {
        if (worldObj != null && worldObj.isRemote) {
            return clientOutput == null ? null : clientOutput.copy();
        }
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
                        recipeCount,
                        status,
                        lastInput,
                        active,
                        getCurrentOutput()),
                    player);
            }
        }
    }

    // ----- NBT -----

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        MachineType loaded = MachineType.byId(nbt.getString("MachineType"));
        if (loaded != null) type = loaded;

        energy = nbt.getInteger("Energy");
        NBTTagList itemList = nbt.getTagList("Items", 10);
        for (int i = 0; i < itemList.tagCount(); i++) {
            NBTTagCompound tag = itemList.getCompoundTagAt(i);
            int slot = tag.getByte("Slot") & 255;
            if (slot >= 0 && slot < upgrades.length) {
                upgrades[slot] = ItemStack.loadItemStackFromNBT(tag);
            }
        }

        queue.clear();
        NBTTagList queueList = nbt.getTagList("Queue", 10);
        for (int i = 0; i < queueList.tagCount(); i++) {
            ItemStack pattern = ItemStack.loadItemStackFromNBT(queueList.getCompoundTagAt(i));
            addJobFromPattern(pattern);
        }

        currentJob = null;
        NBTTagCompound currentTag = nbt.getCompoundTag("Current");
        if (currentTag != null && !currentTag.hasNoTags()) {
            ItemStack pattern = ItemStack.loadItemStackFromNBT(currentTag.getCompoundTag("Pattern"));
            Job job = jobFromPattern(pattern);
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

        recipeCount = 0;
        recipeRefreshCounter = 99;
        status = STATUS_OFFLINE;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setString("MachineType", type.getId());
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

    private void addJobFromPattern(ItemStack pattern) {
        Job job = jobFromPattern(pattern);
        if (job != null) queue.add(job);
    }

    private Job jobFromPattern(ItemStack pattern) {
        if (pattern == null) return null;
        try {
            DigitalPatternDetails details = new DigitalPatternDetails(pattern);
            return details.isValid() && details.getMachineType() == type ? new Job(details) : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static final class Job {

        private final DigitalPatternDetails details;
        private final List<ItemStack> outputs;
        private int progressTicks;
        private int requiredTicks;
        private int euPerTick;
        private int requiredEnergy;

        private Job(DigitalPatternDetails details) {
            this.details = details;
            this.outputs = details.getItemOutputs();
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
            return new DimensionalCoord(TileEntityDigitalMachine.this);
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
            return TileEntityDigitalMachine.this;
        }

        @Override
        public void gridChanged() {}

        @Override
        public ItemStack getMachineRepresentation() {
            return new ItemStack(Berlit.digitalMachineBlocks.get(type));
        }
    }
}
