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

public class TileEntitySpawner extends TileEntity implements IInventory {

    public static final int SLOT_ENTITY = 0;
    public static final int SLOT_UPGRADE_START = 1;
    public static final int SLOT_UPGRADE_COUNT = 7;
    public static final int SLOT_COUNT = 1 + SLOT_UPGRADE_COUNT;

    private final ItemStack[] inventory = new ItemStack[SLOT_COUNT];
    private int cycle;
    private int interval;
    private boolean active;
    private int syncCounter;

    @Override
    public void updateEntity() {
        super.updateEntity();
        if (worldObj.isRemote) return;

        EntityDefinition definition = getDefinition();
        if (definition == null) {
            cycle = 0;
            interval = 0;
            active = false;
        } else {
            UpgradeStats stats = getTotalUpgradeStats();
            interval = Math.max(20, (int) (definition.getBaseInterval() / (1.0F + stats.getSpeedBonus())));
            active = true;

            if (++cycle >= interval) {
                cycle = 0;
                produce(definition, stats);
            }
        }

        if (++syncCounter >= 10) {
            syncCounter = 0;
            syncToClients();
        }
    }

    private void produce(EntityDefinition definition, UpgradeStats stats) {
        for (EntityDrop drop : definition.getDrops()) {
            if (worldObj.rand.nextFloat() > drop.getChance()) continue;

            int extra = drop.getMax() > drop.getMin() ? worldObj.rand.nextInt(drop.getMax() - drop.getMin() + 1) : 0;
            int amount = drop.getMin() + extra + stats.getAmountBonus();
            ItemStack output = drop.getStack()
                .copy();
            output.stackSize = Math.min(amount, output.getMaxStackSize());

            while (output.stackSize > 0) {
                int batch = Math.min(output.stackSize, output.getMaxStackSize());
                ItemStack part = output.copy();
                part.stackSize = batch;
                output.stackSize -= batch;
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

    public EntityDefinition getDefinition() {
        return EntityRegistry.get(ItemEntityCard.getEntityId(inventory[SLOT_ENTITY]));
    }

    public UpgradeStats getTotalUpgradeStats() {
        float speed = 0.0F;
        int amount = 0;
        for (int i = SLOT_UPGRADE_START; i < SLOT_COUNT; i++) {
            UpgradeStats stats = UpgradeRegistry.get(inventory[i]);
            if (stats != null) {
                speed += stats.getSpeedBonus();
                amount += stats.getAmountBonus();
            }
        }
        return new UpgradeStats(speed, amount);
    }

    public int getCycle() {
        return cycle;
    }

    public int getInterval() {
        return interval;
    }

    public int getCycleScaled(int scale) {
        if (interval <= 0) return 0;
        return Math.min(scale, cycle * scale / interval);
    }

    public boolean isActive() {
        return active;
    }

    public void setClientData(int cycle, int interval, boolean active) {
        this.cycle = cycle;
        this.interval = interval;
        this.active = active;
    }

    private void syncToClients() {
        for (Object object : worldObj.playerEntities) {
            EntityPlayerMP player = (EntityPlayerMP) object;
            if (player.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 4096.0D) {
                ModNetwork.INSTANCE
                    .sendTo(new PacketSpawnerSync(xCoord, yCoord, zCoord, cycle, interval, active), player);
            }
        }
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
        return "container.berlit.spawner";
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
        if (slot == SLOT_ENTITY) {
            return stack.getItem() instanceof ItemEntityCard;
        }
        return slot >= SLOT_UPGRADE_START && slot < SLOT_COUNT && UpgradeRegistry.get(stack) != null;
    }

    // ----- NBT -----

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        cycle = nbt.getInteger("Cycle");
        NBTTagList list = nbt.getTagList("Items", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            int slot = tag.getByte("Slot") & 255;
            if (slot >= 0 && slot < inventory.length) {
                inventory[slot] = ItemStack.loadItemStackFromNBT(tag);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("Cycle", cycle);
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
