package ru.beggar.berlit.tileentity;

import java.util.EnumSet;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;
import ru.beggar.berlit.Berlit;

/** Digital tier-3 spawner: 10x10 entity slots plus direct drop output into the ME network. */
public class TileEntityDigitalSpawner extends TileEntitySpawnerT2 implements IGridHost, IActionHost {

    private IGridNode node;

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
    public void validate() {
        super.validate();
        if (worldObj != null && !worldObj.isRemote) getNode().updateState();
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

    @Override
    protected void flushBuffer() {
        if (worldObj == null || worldObj.isRemote) return;

        IGrid grid = networkGrid();
        if (grid != null) {
            IStorageGrid storage = grid.getCache(IStorageGrid.class);
            if (storage != null) {
                IMEMonitor<IAEItemStack> items = storage.getItemInventory();
                MachineSource source = new MachineSource(this);
                for (int i = 0; i < buffer.length; i++) {
                    ItemStack stack = buffer[i];
                    if (stack == null) continue;

                    IAEItemStack leftover = items.injectItems(
                        AEApi.instance()
                            .storage()
                            .createItemStack(stack),
                        Actionable.MODULATE,
                        source);
                    if (leftover == null || leftover.getStackSize() <= 0) {
                        buffer[i] = null;
                    } else {
                        stack.stackSize = (int) Math.min(Integer.MAX_VALUE, leftover.getStackSize());
                    }
                }
                markDirty();
            }
        }

        // If the ME network is absent/full, the base implementation feeds adjacent inventories.
        super.flushBuffer();
    }

    private IGrid networkGrid() {
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

    @Override
    public String getTitleKey() {
        return "tile.berlit.digital_spawner.name";
    }

    private final class GridBlock implements IGridBlock {

        @Override
        public double getIdlePowerUsage() {
            return 2.0D;
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
            return new DimensionalCoord(TileEntityDigitalSpawner.this);
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
            return TileEntityDigitalSpawner.this;
        }

        @Override
        public void gridChanged() {}

        @Override
        public ItemStack getMachineRepresentation() {
            return new ItemStack(Berlit.digitalSpawner);
        }
    }
}
