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

    private IGridNode getNode() {
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

    @Override
    public void provideCrafting(ICraftingProviderHelper helper) {
        int berlitCount = 0;
        for (ItemStack pattern : patterns) {
            if (pattern == null || !(pattern.getItem() instanceof ICraftingPatternItem)) continue;
            ICraftingPatternDetails details = ((ICraftingPatternItem) pattern.getItem())
                .getPatternForItem(pattern, worldObj);
            if (details != null) {
                helper.addCraftingOption(this, details);
                berlitCount++;
            }
        }
        cpw.mods.fml.common.FMLLog.info("[berlit] assembler provideCrafting: " + berlitCount + " patterns");
    }

    @Override
    public boolean pushPattern(ICraftingPatternDetails details, InventoryCrafting crafting) {
        cpw.mods.fml.common.FMLLog.info("[berlit] assembler pushPattern: " + details);
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

        IGrid g = getNode().getGrid();
        if (g != null) {
            IStorageGrid storage = g.getCache(IStorageGrid.class);
            if (storage != null) {
                IMEMonitor<IAEItemStack> itemInventory = storage.getItemInventory();
                itemInventory.injectItems(
                    AEApi.instance()
                        .storage()
                        .createItemStack(result),
                    Actionable.MODULATE,
                    new MachineSource(this));
                return true;
            }
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
    }

    @Override
    public String getInventoryName() {
        return "container.berlit.avaritia_assembler";
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
            return new ItemStack(Berlit.avaritiaAssembler);
        }
    }
}
