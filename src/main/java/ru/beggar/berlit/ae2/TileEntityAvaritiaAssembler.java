package ru.beggar.berlit.ae2;

import java.util.EnumSet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.implementations.tiles.ICraftingMachine;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;
import appeng.container.ContainerNull;
import fox.spiteful.avaritia.crafting.ExtremeCraftingManager;
import ru.beggar.berlit.Berlit;

/** ME machine that executes 9x9 Avaritia patterns. */
public class TileEntityAvaritiaAssembler extends TileEntity
    implements IGridHost, IActionHost, ICraftingProvider, ICraftingMachine, IInventory {

    public static final int PATTERNS = 9;

    private IGridNode node;
    private final ItemStack[] patterns = new ItemStack[PATTERNS];
    private final java.util.List<ItemStack> pendingOutput = new java.util.ArrayList<ItemStack>();

    /**
     * AE2 grid features are server side only - calling createGridNode on the client throws
     * "Grid features ... are server side only", which used to crash the game as soon as the GUI synced
     * its inventory. Callers on the client get null and must tolerate it.
     */
    private IGridNode getNode() {
        if (worldObj != null && worldObj.isRemote) {
            return null;
        }
        if (node == null) {
            node = AEApi.instance()
                .createGridNode(new Block());
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
        flushOutput();
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
        if (node != null) {
            node.destroy();
            node = null;
        }
        super.invalidate();
    }

    @Override
    public void onChunkUnload() {
        if (node != null) {
            node.destroy();
            node = null;
        }
        super.onChunkUnload();
    }

    protected void queueOutput(ItemStack stack) {
        queueOutput(stack, 1);
    }

    /**
     * Queues {@code copies} crafts of {@code stack}, split into normal stack-sized entries so the
     * pending list stays small even when a whole crafting job is produced at once.
     */
    protected void queueOutput(ItemStack stack, long copies) {
        if (stack == null || stack.stackSize <= 0 || copies <= 0) return;

        long remaining = (long) stack.stackSize * copies;
        int max = Math.max(1, stack.getMaxStackSize());
        while (remaining > 0) {
            int size = (int) Math.min(max, remaining);
            ItemStack copy = stack.copy();
            copy.stackSize = size;
            pendingOutput.add(copy);
            remaining -= size;
        }
        markDirty();
    }

    protected void flushOutput() {
        if (pendingOutput.isEmpty() || worldObj == null || worldObj.isRemote) return;
        IGrid grid = networkGrid();
        if (grid == null) return;
        IStorageGrid storage = grid.getCache(IStorageGrid.class);
        if (storage == null) return;
        IMEMonitor<IAEItemStack> items = storage.getItemInventory();
        MachineSource source = new MachineSource(this);
        java.util.Iterator<ItemStack> iterator = pendingOutput.iterator();
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
                // Keep what the network could not accept; dropping it would hang the CPU job.
                stack.stackSize = (int) Math.min(Integer.MAX_VALUE, leftover.getStackSize());
            }
        }
        markDirty();
    }

    private void notifyPatternChange() {
        IGridNode node = getNode();
        if (node != null && node.getGrid() != null) {
            node.getGrid()
                .postEvent(new appeng.api.networking.events.MENetworkCraftingPatternChange(this, node));
        }
    }

    protected ItemStack machineStack() {
        return new ItemStack(Berlit.avaritiaAssembler);
    }

    @Override
    public void provideCrafting(ICraftingProviderHelper helper) {
        for (ItemStack pattern : patterns) {
            if (pattern == null || !(pattern.getItem() instanceof ICraftingPatternItem)) continue;
            ICraftingPatternDetails details = ((ICraftingPatternItem) pattern.getItem())
                .getPatternForItem(pattern, worldObj);
            if (details != null) {
                helper.addCraftingOption(this, details);
            }
        }
    }

    @Override
    public boolean pushPattern(ICraftingPatternDetails details, InventoryCrafting crafting) {
        if (!(details instanceof AvaritiaPatternDetails) || worldObj == null || worldObj.isRemote) return false;

        InventoryCrafting grid = new InventoryCrafting(new ContainerNull(), 9, 9);
        AvaritiaPatternDetails pattern = (AvaritiaPatternDetails) details;
        IAEItemStack[] inputs = pattern.getGrid();
        for (int i = 0; i < 81; i++) {
            grid.setInventorySlotContents(i, inputs[i] == null ? null : inputs[i].getItemStack());
        }

        ItemStack result = ExtremeCraftingManager.getInstance()
            .findMatchingRecipe(grid, worldObj);
        if (result == null) return false;

        IGrid g = networkGrid();
        if (g != null && g.getCache(IStorageGrid.class) != null) {
            queueOutput(result);
            return true;
        }

        if (worldObj.getTileEntity(xCoord, yCoord + 1, zCoord) instanceof IInventory) {
            IInventory up = (IInventory) worldObj.getTileEntity(xCoord, yCoord + 1, zCoord);
            for (int i = 0; i < up.getSizeInventory(); i++) {
                if (up.getStackInSlot(i) == null) {
                    up.setInventorySlotContents(i, result);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean pushPattern(ICraftingPatternDetails details, InventoryCrafting crafting, ForgeDirection side) {
        return pushPattern(details, crafting);
    }

    @Override
    public boolean acceptsPlans() {
        return true;
    }

    /** Own grid, or the grid of an adjacent AE2 block (e.g. ME Interface) if no cable. */
    protected IGrid networkGrid() {
        IGridNode own = getNode();
        IGrid grid = own == null ? null : own.getGrid();
        if (grid != null && grid.getCache(IStorageGrid.class) != null) return grid;

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

    /** Pulls one recipe worth of ingredients from the ME network and crafts it. */
    public boolean craftOnce() {
        if (worldObj == null || worldObj.isRemote) return false;

        AvaritiaPatternDetails pattern = null;
        for (ItemStack stack : patterns) {
            if (stack != null && stack.getItem() instanceof ICraftingPatternItem) {
                ICraftingPatternDetails details = ((ICraftingPatternItem) stack.getItem())
                    .getPatternForItem(stack, worldObj);
                if (details instanceof AvaritiaPatternDetails) {
                    pattern = (AvaritiaPatternDetails) details;
                    break;
                }
            }
        }
        if (pattern == null) return false;

        IGrid grid = networkGrid();
        if (grid == null) return false;
        IStorageGrid storage = grid.getCache(IStorageGrid.class);
        if (storage == null) return false;
        IMEMonitor<IAEItemStack> items = storage.getItemInventory();
        MachineSource source = new MachineSource(this);

        IAEItemStack[] required = pattern.getInputs();
        for (IAEItemStack input : required) {
            if (input == null) continue;
            IAEItemStack found = items.extractItems(input.copy(), Actionable.SIMULATE, source);
            if (found == null || found.getStackSize() < input.getStackSize()) return false;
        }

        for (IAEItemStack input : required) {
            if (input == null) continue;
            items.extractItems(input.copy(), Actionable.MODULATE, source);
        }

        InventoryCrafting gridInv = new InventoryCrafting(new ContainerNull(), 9, 9);
        IAEItemStack[] cells = pattern.getGrid();
        for (int i = 0; i < 81; i++) {
            gridInv.setInventorySlotContents(i, cells[i] == null ? null : cells[i].getItemStack());
        }

        ItemStack result = ExtremeCraftingManager.getInstance()
            .findMatchingRecipe(gridInv, worldObj);
        if (result == null) return false;

        queueOutput(result);
        return true;
    }

    @Override
    public boolean isBusy() {
        return false;
    }

    @Override
    public int getSizeInventory() {
        return PATTERNS;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return patterns[slot];
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        if (patterns[slot] == null) return null;
        ItemStack result = patterns[slot];
        patterns[slot] = null;
        markDirty();
        return result;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        patterns[slot] = stack;
        markDirty();
        // The client gets here while syncing the window contents; the grid event is server side.
        if (slot >= 0 && slot < PATTERNS && worldObj != null && !worldObj.isRemote) {
            notifyPatternChange();
        }
    }

    @Override
    public String getInventoryName() {
        return "tile.berlit.avaritia_assembler.name";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
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
        return stack != null && stack.getItem() == Berlit.avaritiaPattern;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        NBTTagList list = nbt.getTagList("Patterns", 10);
        for (int i = 0; i < list.tagCount() && i < PATTERNS; i++) {
            patterns[i] = ItemStack.loadItemStackFromNBT(list.getCompoundTagAt(i));
        }

        pendingOutput.clear();
        NBTTagList pending = nbt.getTagList("Pending", 10);
        for (int i = 0; i < pending.tagCount(); i++) {
            ItemStack stack = ItemStack.loadItemStackFromNBT(pending.getCompoundTagAt(i));
            if (stack != null) pendingOutput.add(stack);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        NBTTagList list = new NBTTagList();
        for (ItemStack pattern : patterns) {
            NBTTagCompound tag = new NBTTagCompound();
            if (pattern != null) pattern.writeToNBT(tag);
            list.appendTag(tag);
        }
        nbt.setTag("Patterns", list);

        NBTTagList pending = new NBTTagList();
        for (ItemStack stack : pendingOutput) {
            NBTTagCompound tag = new NBTTagCompound();
            stack.writeToNBT(tag);
            pending.appendTag(tag);
        }
        nbt.setTag("Pending", pending);
    }

    private final class Block implements IGridBlock {

        @Override
        public double getIdlePowerUsage() {
            return 1.0;
        }

        @Override
        public EnumSet<GridFlags> getFlags() {
            return EnumSet.of(GridFlags.REQUIRE_CHANNEL);
        }

        @Override
        public boolean isWorldAccessible() {
            return true;
        }

        @Override
        public DimensionalCoord getLocation() {
            return new DimensionalCoord(TileEntityAvaritiaAssembler.this);
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
        public EnumSet<ForgeDirection> getConnectableSides() {
            return EnumSet.allOf(ForgeDirection.class);
        }

        @Override
        public IGridHost getMachine() {
            return TileEntityAvaritiaAssembler.this;
        }

        @Override
        public void gridChanged() {}

        @Override
        public ItemStack getMachineRepresentation() {
            return machineStack();
        }
    }
}
