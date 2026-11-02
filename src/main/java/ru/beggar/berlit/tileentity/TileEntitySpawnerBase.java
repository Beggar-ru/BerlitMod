package ru.beggar.berlit.tileentity;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import ru.beggar.berlit.item.ItemEntityCard;
import ru.beggar.berlit.network.ModNetwork;
import ru.beggar.berlit.network.PacketSpawnerSync;
import ru.beggar.berlit.spawner.EntityDefinition;
import ru.beggar.berlit.spawner.EntityDrop;
import ru.beggar.berlit.spawner.EntityRegistry;
import ru.beggar.berlit.spawner.UpgradeRegistry;
import ru.beggar.berlit.spawner.UpgradeStats;

public abstract class TileEntitySpawnerBase extends TileEntity implements IInventory {

    public static final int UPGRADE_SLOTS = 7;

    protected final ItemStack[] inventory;
    private final int[] cycles;
    private final int[] intervals;
    private int syncCounter;

    protected TileEntitySpawnerBase(int entitySlots) {
        inventory = new ItemStack[entitySlots + UPGRADE_SLOTS];
        cycles = new int[entitySlots];
        intervals = new int[entitySlots];
    }

    public abstract int getEntitySlotCount();

    /** Max number of entity cards in one entity slot (1 for tier 1, 10 for tier 2). */
    public abstract int getEntitySlotLimit();

    public abstract int getEntitySlotX(int index);

    public abstract int getEntitySlotY(int index);

    public abstract String getGuiTexture();

    public abstract String getTitleKey();

    public int getUpgradeSlotStart() {
        return getEntitySlotCount();
    }

    public int getUpgradeSlotX(int index) {
        return 62 + (index % 4) * 18;
    }

    public int getUpgradeSlotY(int index) {
        return 17 + (index / 4) * 18;
    }

    public int getSlotCount() {
        return getEntitySlotCount() + UPGRADE_SLOTS;
    }

    @Override
    public void updateEntity() {
        super.updateEntity();
        if (worldObj.isRemote) return;

        UpgradeStats stats = getTotalUpgradeStats();

        for (int i = 0; i < getEntitySlotCount(); i++) {
            EntityDefinition definition = getDefinition(i);
            if (definition == null) {
                cycles[i] = 0;
                intervals[i] = 0;
                continue;
            }

            intervals[i] = Math.max(20, (int) (definition.getBaseInterval() / (1.0F + stats.getSpeedBonus())));
            if (++cycles[i] >= intervals[i]) {
                cycles[i] = 0;
                produce(definition, stats, i);
            }
        }

        if (++syncCounter >= 10) {
            syncCounter = 0;
            syncToClients();
        }
    }

    private void produce(EntityDefinition definition, UpgradeStats stats, int slot) {
        ItemStack card = inventory[slot];
        int units = card == null ? 0 : card.stackSize;

        for (EntityDrop drop : definition.getDrops()) {
            int total = 0;
            for (int unit = 0; unit < units; unit++) {
                if (worldObj.rand.nextFloat() > drop.getChance()) continue;
                int extra = drop.getMax() > drop.getMin() ? worldObj.rand.nextInt(drop.getMax() - drop.getMin() + 1)
                    : 0;
                total += drop.getMin() + extra;
            }

            if (total > 0) total += stats.getAmountBonus();

            while (total > 0) {
                int batch = Math.min(
                    total,
                    drop.getStack()
                        .getMaxStackSize());
                ItemStack part = drop.getStack()
                    .copy();
                part.stackSize = batch;
                total -= batch;
                outputToInventories(part);
            }
        }
    }

    private void outputToInventories(ItemStack stack) {
        ItemStack remaining = stack.copy();

        for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
            TileEntity tile = worldObj
                .getTileEntity(xCoord + direction.offsetX, yCoord + direction.offsetY, zCoord + direction.offsetZ);
            if (tile instanceof IInventory && tile != this) {
                remaining = insert((IInventory) tile, remaining);
                if (remaining == null) return;
            }
        }

        if (remaining != null && remaining.stackSize > 0) {
            EntityItem entity = new EntityItem(worldObj, xCoord + 0.5D, yCoord + 1.2D, zCoord + 0.5D, remaining);
            entity.motionX = 0.0D;
            entity.motionY = 0.0D;
            entity.motionZ = 0.0D;
            worldObj.spawnEntityInWorld(entity);
        }
    }

    private ItemStack insert(IInventory inventory, ItemStack stack) {
        ItemStack remaining = stack.copy();

        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            ItemStack slot = inventory.getStackInSlot(i);
            if (slot == null) continue;
            if (slot.getItem() != remaining.getItem() || slot.getItemDamage() != remaining.getItemDamage()) continue;

            int limit = Math.min(inventory.getInventoryStackLimit(), slot.getMaxStackSize());
            int space = limit - slot.stackSize;
            if (space <= 0) continue;

            int moved = Math.min(space, remaining.stackSize);
            slot.stackSize += moved;
            remaining.stackSize -= moved;
            inventory.markDirty();
            if (remaining.stackSize <= 0) return null;
        }

        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            if (inventory.getStackInSlot(i) != null) continue;
            if (!inventory.isItemValidForSlot(i, remaining)) continue;

            int limit = Math.min(inventory.getInventoryStackLimit(), remaining.getMaxStackSize());
            ItemStack copy = remaining.copy();
            copy.stackSize = Math.min(limit, remaining.stackSize);
            inventory.setInventorySlotContents(i, copy);
            remaining.stackSize -= copy.stackSize;
            if (remaining.stackSize <= 0) return null;
        }

        return remaining.stackSize > 0 ? remaining : null;
    }

    public EntityDefinition getDefinition(int slot) {
        return EntityRegistry.get(ItemEntityCard.getEntityId(inventory[slot]));
    }

    public UpgradeStats getTotalUpgradeStats() {
        float speed = 0.0F;
        int amount = 0;
        for (int i = getUpgradeSlotStart(); i < getSlotCount(); i++) {
            UpgradeStats stats = UpgradeRegistry.get(inventory[i]);
            if (stats != null) {
                speed += stats.getSpeedBonus();
                amount += stats.getAmountBonus();
            }
        }
        return new UpgradeStats(speed, amount);
    }

    public int getTotalEntityCount() {
        int total = 0;
        for (int i = 0; i < getEntitySlotCount(); i++) {
            if (inventory[i] != null) total += inventory[i].stackSize;
        }
        return total;
    }

    public int getActiveSlot() {
        for (int i = 0; i < getEntitySlotCount(); i++) {
            if (getDefinition(i) != null) return i;
        }
        return -1;
    }

    public int getCycle() {
        int slot = getActiveSlot();
        return slot < 0 ? 0 : cycles[slot];
    }

    public int getInterval() {
        int slot = getActiveSlot();
        return slot < 0 ? 0 : intervals[slot];
    }

    public int getCycleScaled(int scale) {
        int interval = getInterval();
        if (interval <= 0) return 0;
        return Math.min(scale, getCycle() * scale / interval);
    }

    public boolean isActive() {
        return getActiveSlot() >= 0;
    }

    public void setClientData(int cycle, int interval, boolean active) {
        int slot = getActiveSlot();
        if (slot >= 0 && interval > 0) {
            cycles[slot] = cycle;
            intervals[slot] = interval;
        }
    }

    private void syncToClients() {
        int slot = getActiveSlot();
        int cycle = slot < 0 ? 0 : cycles[slot];
        int interval = slot < 0 ? 0 : intervals[slot];

        for (Object object : worldObj.playerEntities) {
            EntityPlayerMP player = (EntityPlayerMP) object;
            if (player.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 4096.0D) {
                ModNetwork.INSTANCE
                    .sendTo(new PacketSpawnerSync(xCoord, yCoord, zCoord, cycle, interval, isActive()), player);
            }
        }
    }

    // ----- inventory -----

    @Override
    public int getSizeInventory() {
        return getSlotCount();
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
        if (stack != null && slot < getEntitySlotCount()) {
            int limit = getEntitySlotLimit();
            if (stack.stackSize > limit) {
                stack.stackSize = limit;
            }
        }
        inventory[slot] = stack;
        if (stack != null && stack.stackSize > getInventoryStackLimit()) {
            stack.stackSize = getInventoryStackLimit();
        }
        markDirty();
    }

    @Override
    public String getInventoryName() {
        return getTitleKey();
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
        if (slot < getEntitySlotCount()) {
            return stack.getItem() instanceof ItemEntityCard;
        }
        return slot >= getUpgradeSlotStart() && slot < getSlotCount() && UpgradeRegistry.get(stack) != null;
    }

    // ----- NBT -----

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        NBTTagList list = nbt.getTagList("Items", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            int slot = tag.getByte("Slot") & 255;
            if (slot >= 0 && slot < inventory.length) {
                inventory[slot] = ItemStack.loadItemStackFromNBT(tag);
            }
        }
        NBTTagList cyclesTag = nbt.getTagList("Cycles", 10);
        for (int i = 0; i < cyclesTag.tagCount() && i < cycles.length; i++) {
            cycles[i] = cyclesTag.getCompoundTagAt(i)
                .getInteger("C");
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
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

        NBTTagList cyclesTag = new NBTTagList();
        for (int i = 0; i < cycles.length; i++) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("C", cycles[i]);
            cyclesTag.appendTag(tag);
        }
        nbt.setTag("Cycles", cyclesTag);
    }
}
