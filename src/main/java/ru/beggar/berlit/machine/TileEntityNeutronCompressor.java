package ru.beggar.berlit.machine;

import java.util.EnumSet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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
import fox.spiteful.avaritia.crafting.CompressorManager;
import ru.beggar.berlit.Berlit;
import ru.beggar.berlit.ae2.SingularityPatternDetails;

/** Compresses items into an Avaritia singularity, one item at a time. */
public class TileEntityNeutronCompressor extends TileEntity
    implements IInventory, IGridHost, IActionHost, ICraftingProvider, ICraftingMachine {

    public static final int INPUT = 0;
    public static final int OUTPUT = 1;
    public static final int PATTERN = 2;
    public static final int SIZE = 3;

    private final ItemStack[] inventory = new ItemStack[SIZE];
    private int progress;
    private int cost;
    private ItemStack pendingOutput;
    private IGridNode node;

    private IGridNode getNode() {
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

    private SingularityPatternDetails getPattern() {
        ItemStack stack = inventory[PATTERN];
        if (stack == null || !(stack.getItem() instanceof ICraftingPatternItem)) return null;
        ICraftingPatternDetails details = ((ICraftingPatternItem) stack.getItem()).getPatternForItem(stack, worldObj);
        return details instanceof SingularityPatternDetails ? (SingularityPatternDetails) details : null;
    }

    @Override
    public void provideCrafting(ICraftingProviderHelper helper) {
        SingularityPatternDetails pattern = getPattern();
        if (pattern != null) helper.addCraftingOption(this, pattern);
    }

    @Override
    public boolean pushPattern(ICraftingPatternDetails details, InventoryCrafting crafting) {
        if (!(details instanceof SingularityPatternDetails) || worldObj == null || worldObj.isRemote) return false;

        ItemStack result = details.getOutput(crafting, worldObj);
        if (result == null) return false;

        IGrid grid = networkGrid();
        if (grid != null) {
            IStorageGrid storage = grid.getCache(IStorageGrid.class);
            if (storage != null) {
                IMEMonitor<IAEItemStack> items = storage.getItemInventory();
                items.injectItems(
                    AEApi.instance()
                        .storage()
                        .createItemStack(result),
                    Actionable.MODULATE,
                    new MachineSource(this));
                return true;
            }
        }

        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            TileEntity adjacent = worldObj
                .getTileEntity(xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ);
            if (adjacent instanceof IInventory) {
                IInventory inv = (IInventory) adjacent;
                for (int i = 0; i < inv.getSizeInventory(); i++) {
                    if (inv.getStackInSlot(i) == null) {
                        inv.setInventorySlotContents(i, result);
                        return true;
                    }
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
    private IGrid networkGrid() {
        IGrid grid = getNode().getGrid();
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

    /** Pulls one singularity worth of items from the ME network. */
    public boolean craftOnce() {
        if (worldObj == null || worldObj.isRemote) return false;
        SingularityPatternDetails pattern = getPattern();
        if (pattern == null) return false;

        IGrid grid = networkGrid();
        if (grid == null) return false;
        IStorageGrid storage = grid.getCache(IStorageGrid.class);
        if (storage == null) return false;
        IMEMonitor<IAEItemStack> items = storage.getItemInventory();
        MachineSource source = new MachineSource(this);

        IAEItemStack input = pattern.getInputs()[0];
        if (input == null) return false;
        IAEItemStack found = items.extractItems(input.copy(), Actionable.SIMULATE, source);
        if (found == null || found.getStackSize() < input.getStackSize()) return false;
        items.extractItems(input.copy(), Actionable.MODULATE, source);

        ItemStack result = pattern.getOutput(null, worldObj);
        if (result == null) return false;
        items.injectItems(
            AEApi.instance()
                .storage()
                .createItemStack(result),
            Actionable.MODULATE,
            source);
        return true;
    }

    @Override
    public boolean isBusy() {
        return false;
    }

    @Override
    public void updateEntity() {
        if (worldObj == null || worldObj.isRemote) return;
        if (inventory[OUTPUT] != null) return;

        SingularityPatternDetails pattern = getPattern();
        ItemStack input = inventory[INPUT];
        if (input == null) {
            progress = 0;
            cost = 0;
            pendingOutput = null;
            return;
        }

        if (pattern != null) {
            ItemStack target = pattern.getInputStack();
            if (target == null || !input.isItemEqual(target)) return;
            if (cost <= 0) {
                cost = pattern.getCost();
                pendingOutput = pattern.getOutput(null, worldObj);
            }
        } else if (cost <= 0) {
            ItemStack result = CompressorManager.getOutput(input);
            if (result == null) return;
            cost = Math.max(1, CompressorManager.getCost(input));
            pendingOutput = result;
        }

        input.stackSize--;
        progress++;
        if (input.stackSize <= 0) inventory[INPUT] = null;

        if (progress >= cost) {
            if (pendingOutput != null) inventory[OUTPUT] = pendingOutput;
            pendingOutput = null;
            progress = 0;
            cost = 0;
        }
        markDirty();
    }

    @Override
    public int getSizeInventory() {
        return SIZE;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return inventory[slot];
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        if (inventory[slot] == null) return null;
        ItemStack result = inventory[slot];
        inventory[slot] = null;
        markDirty();
        return result;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        inventory[slot] = stack;
        markDirty();
    }

    @Override
    public String getInventoryName() {
        return "container.berlit.neutron_compressor";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 4096;
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
        if (slot == PATTERN) return stack != null && stack.getItem() == Berlit.singularityPattern;
        if (slot != INPUT || stack == null) return false;

        SingularityPatternDetails pattern = getPattern();
        if (pattern != null) {
            ItemStack target = pattern.getInputStack();
            return target != null && stack.isItemEqual(target);
        }
        return CompressorManager.getOutput(stack) != null;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        progress = nbt.getInteger("Progress");
        cost = nbt.getInteger("Cost");
        for (int i = 0; i < SIZE; i++) {
            inventory[i] = nbt.hasKey("Item" + i) ? ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("Item" + i))
                : null;
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("Progress", progress);
        nbt.setInteger("Cost", cost);
        for (int i = 0; i < SIZE; i++) {
            if (inventory[i] != null) {
                NBTTagCompound tag = new NBTTagCompound();
                inventory[i].writeToNBT(tag);
                nbt.setTag("Item" + i, tag);
            }
        }
    }

    private final class GridBlock implements IGridBlock {

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
            return new DimensionalCoord(TileEntityNeutronCompressor.this);
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
            return TileEntityNeutronCompressor.this;
        }

        @Override
        public void gridChanged() {
            markDirty();
        }

        @Override
        public ItemStack getMachineRepresentation() {
            return new ItemStack(Berlit.neutronCompressor);
        }
    }
}
