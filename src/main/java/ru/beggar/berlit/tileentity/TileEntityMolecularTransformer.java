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

import cpw.mods.fml.common.Loader;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySink;
import ru.beggar.berlit.network.ModNetwork;
import ru.beggar.berlit.network.PacketMachineSync;
import ru.beggar.berlit.recipe.MolecularRecipe;
import ru.beggar.berlit.recipe.MolecularRecipes;
import ru.beggar.berlit.upgrade.UpgradeHelper;

public class TileEntityMolecularTransformer extends TileEntity implements IEnergySink, IInventory {

    public static final int SLOT_INPUT_START = 0;
    public static final int SLOT_INPUT_COUNT = 6;
    public static final int SLOT_OUTPUT_START = 6;
    public static final int SLOT_OUTPUT_COUNT = 6;
    public static final int SLOT_UPGRADE_START = 12;
    public static final int SLOT_UPGRADE_COUNT = 4;
    public static final int SLOT_COUNT = SLOT_UPGRADE_START + SLOT_UPGRADE_COUNT;

    /** Base energy buffer size (energy storage upgrades increase it). */
    public static final int ENERGY_CAPACITY = 8000;

    private final ItemStack[] inventory = new ItemStack[SLOT_COUNT];
    private final List<ItemStack> batchOutputs = new ArrayList<ItemStack>();

    private int energy;
    private int progress;
    private double progressExact;
    private int recipeEnergy;
    private double requiredEnergy;
    private int baseEnergy;
    private boolean active;
    private boolean addedToEnergyNet;
    private int syncCounter;
    private int lastInput;
    private int inputThisTick;

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

        if (stats.puller) {
            pullInputs();
        }

        refreshBatch(stats.batch);
        requiredEnergy = baseEnergy * stats.energyCost;
        recipeEnergy = (int) Math.ceil(requiredEnergy);
        active = false;

        if (baseEnergy > 0) {
            if (progressExact >= requiredEnergy) {
                if (canCraftAll()) {
                    craftAll();
                    progressExact = 0;
                    refreshBatch(stats.batch);
                    requiredEnergy = baseEnergy * stats.energyCost;
                    recipeEnergy = (int) Math.ceil(requiredEnergy);
                }
            } else if (energy > 0) {
                double need = requiredEnergy - progressExact;
                int use = (int) Math.min(energy, Math.ceil(need / stats.speed));
                if (use > 0) {
                    progressExact = Math.min(requiredEnergy, progressExact + use * stats.speed);
                    energy -= use;
                    active = true;
                }
            }
        } else {
            progressExact = 0;
        }

        progress = (int) progressExact;

        if (stats.ejector) {
            ejectOutputs();
        }

        if (++syncCounter >= 10) {
            syncCounter = 0;
            syncToClients();
        }
    }

    private UpgradeHelper.Stats getUpgradeStats() {
        List<ItemStack> stacks = new ArrayList<ItemStack>();
        for (int i = SLOT_UPGRADE_START; i < SLOT_COUNT; i++) {
            stacks.add(inventory[i]);
        }
        return UpgradeHelper.combine(stacks);
    }

    /** Builds the output batch: one item normally, or a whole stack with the Stack Upgrade. */
    private void refreshBatch(boolean batch) {
        batchOutputs.clear();
        baseEnergy = 0;

        for (int i = SLOT_INPUT_START; i < SLOT_INPUT_START + SLOT_INPUT_COUNT; i++) {
            ItemStack stack = inventory[i];
            if (stack == null) continue;

            MolecularRecipe recipe = MolecularRecipes.find(stack);
            if (recipe == null) continue;

            int units = batch ? stack.stackSize : 1;
            for (int unit = 0; unit < units; unit++) {
                baseEnergy += recipe.getEnergy();
                batchOutputs.add(
                    recipe.getOutput()
                        .copy());
            }
            break;
        }
    }

    private boolean canCraftAll() {
        ItemStack[] simulated = new ItemStack[SLOT_OUTPUT_COUNT];
        for (int i = 0; i < SLOT_OUTPUT_COUNT; i++) {
            ItemStack stack = inventory[SLOT_OUTPUT_START + i];
            simulated[i] = stack == null ? null : stack.copy();
        }

        for (ItemStack output : batchOutputs) {
            int remaining = output.stackSize;
            for (int i = 0; i < SLOT_OUTPUT_COUNT && remaining > 0; i++) {
                ItemStack stack = simulated[i];
                if (stack == null) {
                    int added = Math.min(getInventoryStackLimit(), remaining);
                    simulated[i] = output.copy();
                    simulated[i].stackSize = added;
                    remaining -= added;
                } else if (stack.getItem() == output.getItem() && stack.getItemDamage() == output.getItemDamage()) {
                    int space = getInventoryStackLimit() - stack.stackSize;
                    int added = Math.min(space, remaining);
                    stack.stackSize += added;
                    remaining -= added;
                }
            }
            if (remaining > 0) return false;
        }
        return true;
    }

    private void craftAll() {
        int remaining = batchOutputs.size();
        for (int i = SLOT_INPUT_START; i < SLOT_INPUT_START + SLOT_INPUT_COUNT && remaining > 0; i++) {
            ItemStack stack = inventory[i];
            if (stack == null || MolecularRecipes.find(stack) == null) continue;

            int consumed = Math.min(stack.stackSize, remaining);
            stack.stackSize -= consumed;
            remaining -= consumed;
            if (stack.stackSize <= 0) {
                inventory[i] = null;
            }
        }

        for (ItemStack output : batchOutputs) {
            addOutput(output.copy());
        }
        markDirty();
    }

    private void addOutput(ItemStack output) {
        for (int i = SLOT_OUTPUT_START; i < SLOT_OUTPUT_START + SLOT_OUTPUT_COUNT && output.stackSize > 0; i++) {
            ItemStack stack = inventory[i];
            if (stack == null) {
                int added = Math.min(getInventoryStackLimit(), output.stackSize);
                ItemStack copy = output.copy();
                copy.stackSize = added;
                inventory[i] = copy;
                output.stackSize -= added;
            } else if (stack.getItem() == output.getItem() && stack.getItemDamage() == output.getItemDamage()) {
                int space = getInventoryStackLimit() - stack.stackSize;
                int moved = Math.min(space, output.stackSize);
                stack.stackSize += moved;
                output.stackSize -= moved;
            }
        }
    }

    private void ejectOutputs() {
        for (int i = SLOT_OUTPUT_START; i < SLOT_OUTPUT_START + SLOT_OUTPUT_COUNT; i++) {
            ItemStack stack = inventory[i];
            if (stack == null) continue;

            ItemStack remaining = insertIntoNeighbours(stack.copy());
            inventory[i] = remaining == null || remaining.stackSize <= 0 ? null : remaining;
        }
    }

    private ItemStack insertIntoNeighbours(ItemStack stack) {
        ItemStack remaining = stack.copy();

        for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
            TileEntity tile = worldObj
                .getTileEntity(xCoord + direction.offsetX, yCoord + direction.offsetY, zCoord + direction.offsetZ);
            if (tile instanceof IInventory && tile != this) {
                remaining = insert((IInventory) tile, remaining);
                if (remaining == null) return null;
            }
        }
        return remaining;
    }

    private ItemStack insert(IInventory target, ItemStack stack) {
        ItemStack remaining = stack.copy();

        for (int i = 0; i < target.getSizeInventory(); i++) {
            ItemStack slot = target.getStackInSlot(i);
            if (slot == null) continue;
            if (slot.getItem() != remaining.getItem() || slot.getItemDamage() != remaining.getItemDamage()) continue;

            int limit = Math.min(target.getInventoryStackLimit(), slot.getMaxStackSize());
            int space = limit - slot.stackSize;
            if (space <= 0) continue;

            int moved = Math.min(space, remaining.stackSize);
            slot.stackSize += moved;
            remaining.stackSize -= moved;
            target.markDirty();
            if (remaining.stackSize <= 0) return null;
        }

        for (int i = 0; i < target.getSizeInventory(); i++) {
            if (target.getStackInSlot(i) != null) continue;
            if (!target.isItemValidForSlot(i, remaining)) continue;

            int limit = Math.min(target.getInventoryStackLimit(), remaining.getMaxStackSize());
            ItemStack copy = remaining.copy();
            copy.stackSize = Math.min(limit, remaining.stackSize);
            target.setInventorySlotContents(i, copy);
            remaining.stackSize -= copy.stackSize;
            if (remaining.stackSize <= 0) return null;
        }

        return remaining.stackSize > 0 ? remaining : null;
    }

    private void pullInputs() {
        for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
            TileEntity tile = worldObj
                .getTileEntity(xCoord + direction.offsetX, yCoord + direction.offsetY, zCoord + direction.offsetZ);
            if (!(tile instanceof IInventory) || tile == this) continue;

            IInventory source = (IInventory) tile;
            for (int i = 0; i < source.getSizeInventory(); i++) {
                ItemStack stack = source.getStackInSlot(i);
                if (stack == null || MolecularRecipes.find(stack) == null) continue;

                if (addInput(stack)) {
                    source.decrStackSize(i, 1);
                    return;
                }
            }
        }
    }

    private boolean addInput(ItemStack stack) {
        for (int i = SLOT_INPUT_START; i < SLOT_INPUT_START + SLOT_INPUT_COUNT; i++) {
            ItemStack slot = inventory[i];
            if (slot != null && slot.getItem() == stack.getItem()
                && slot.getItemDamage() == stack.getItemDamage()
                && slot.stackSize < getInventoryStackLimit()) {
                slot.stackSize++;
                markDirty();
                return true;
            }
        }
        for (int i = SLOT_INPUT_START; i < SLOT_INPUT_START + SLOT_INPUT_COUNT; i++) {
            if (inventory[i] == null) {
                ItemStack copy = stack.copy();
                copy.stackSize = 1;
                inventory[i] = copy;
                markDirty();
                return true;
            }
        }
        return false;
    }

    private void syncToClients() {
        for (Object object : worldObj.playerEntities) {
            EntityPlayerMP player = (EntityPlayerMP) object;
            if (player.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 4096.0D) {
                ModNetwork.INSTANCE.sendTo(
                    new PacketMachineSync(xCoord, yCoord, zCoord, energy, progress, recipeEnergy, lastInput, 0, active),
                    player);
            }
        }
    }

    public boolean canProcess() {
        UpgradeHelper.Stats stats = getUpgradeStats();
        refreshBatch(stats.batch);
        if (baseEnergy <= 0) return false;

        double required = baseEnergy * stats.energyCost;
        return progressExact < required && canCraftAll();
    }

    public int getEnergy() {
        return energy;
    }

    public int getMaxEnergy() {
        return ENERGY_CAPACITY + getUpgradeStats().storage;
    }

    public int getProgressScaled(int scale) {
        if (recipeEnergy <= 0) return 0;
        return Math.min(scale, progress * scale / recipeEnergy);
    }

    public int getProgress() {
        return progress;
    }

    public int getRecipeEnergy() {
        return recipeEnergy;
    }

    /** EU received during the last tick (server side, synced to the GUI). */
    public int getLastInput() {
        return lastInput;
    }

    /** Applies the data received from the server (client side). */
    public void setClientData(int energy, int progress, int recipeEnergy, int lastInput, boolean active) {
        this.energy = energy;
        this.progress = progress;
        this.recipeEnergy = recipeEnergy;
        this.lastInput = lastInput;
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    // ----- IC2 energy sink -----

    @Override
    public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction) {
        return true;
    }

    @Override
    public double getDemandedEnergy() {
        if (!canProcess()) return 0;
        return getMaxEnergy() - energy;
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
        return "container.berlit.molecular_transformer";
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
        if (slot >= SLOT_INPUT_START && slot < SLOT_INPUT_START + SLOT_INPUT_COUNT) {
            return MolecularRecipes.find(stack) != null;
        }
        return slot >= SLOT_UPGRADE_START && slot < SLOT_COUNT && UpgradeHelper.isMachineUpgrade(stack);
    }

    // ----- NBT -----

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        energy = nbt.getInteger("Energy");
        progressExact = nbt.getDouble("ProgressExact");
        progress = (int) progressExact;
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
        nbt.setInteger("Energy", energy);
        nbt.setDouble("ProgressExact", progressExact);
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
