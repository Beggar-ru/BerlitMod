package ru.beggar.berlit.tileentity;

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

public class TileEntityMolecularTransformer extends TileEntity implements IEnergySink, IInventory {

    public static final int SLOT_INPUT_START = 0;
    public static final int SLOT_INPUT_COUNT = 6;
    public static final int SLOT_OUTPUT_START = 6;
    public static final int SLOT_OUTPUT_COUNT = 6;
    public static final int SLOT_COUNT = SLOT_INPUT_COUNT + SLOT_OUTPUT_COUNT;

    /** Energy buffer size. The machine accepts and spends energy without a per-tick limit. */
    public static final int ENERGY_CAPACITY = 8000;

    private final ItemStack[] inventory = new ItemStack[SLOT_COUNT];
    private int energy;
    private int progress;
    private int recipeEnergy;
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

        int slot = findRecipeSlot();
        active = false;

        if (slot >= 0) {
            MolecularRecipe recipe = MolecularRecipes.find(inventory[slot]);
            recipeEnergy = recipe.getEnergy();

            if (progress >= recipeEnergy) {
                craft(slot, recipe);
                progress = 0;
            } else if (energy > 0) {
                int used = Math.min(energy, recipeEnergy - progress);
                energy -= used;
                progress += used;
                active = true;
            }
        } else {
            progress = 0;
            recipeEnergy = 0;
        }

        if (++syncCounter >= 10) {
            syncCounter = 0;
            syncToClients();
        }
    }

    private void syncToClients() {
        for (Object object : worldObj.playerEntities) {
            EntityPlayerMP player = (EntityPlayerMP) object;
            if (player.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 4096.0D) {
                ModNetwork.INSTANCE.sendTo(
                    new PacketMachineSync(xCoord, yCoord, zCoord, energy, progress, recipeEnergy, lastInput, active),
                    player);
            }
        }
    }

    private int findRecipeSlot() {
        for (int i = SLOT_INPUT_START; i < SLOT_INPUT_START + SLOT_INPUT_COUNT; i++) {
            MolecularRecipe recipe = MolecularRecipes.find(inventory[i]);
            if (recipe != null && canOutput(recipe.getOutput())) {
                return i;
            }
        }
        return -1;
    }

    private boolean canOutput(ItemStack output) {
        for (int i = SLOT_OUTPUT_START; i < SLOT_OUTPUT_START + SLOT_OUTPUT_COUNT; i++) {
            ItemStack stack = inventory[i];
            if (stack == null) return true;
            if (stack.getItem() == output.getItem() && stack.getItemDamage() == output.getItemDamage()
                && stack.stackSize + output.stackSize <= getInventoryStackLimit()) {
                return true;
            }
        }
        return false;
    }

    private void craft(int slot, MolecularRecipe recipe) {
        ItemStack input = inventory[slot];
        input.stackSize--;
        if (input.stackSize <= 0) {
            inventory[slot] = null;
        }

        ItemStack output = recipe.getOutput()
            .copy();
        for (int i = SLOT_OUTPUT_START; i < SLOT_OUTPUT_START + SLOT_OUTPUT_COUNT && output.stackSize > 0; i++) {
            ItemStack stack = inventory[i];
            if (stack == null) {
                inventory[i] = output.copy();
                output.stackSize = 0;
            } else if (stack.getItem() == output.getItem() && stack.getItemDamage() == output.getItemDamage()) {
                int space = getInventoryStackLimit() - stack.stackSize;
                int moved = Math.min(space, output.stackSize);
                stack.stackSize += moved;
                output.stackSize -= moved;
            }
        }
        markDirty();
    }

    public boolean canProcess() {
        return findRecipeSlot() >= 0;
    }

    public int getEnergy() {
        return energy;
    }

    public int getMaxEnergy() {
        return ENERGY_CAPACITY;
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
        return ENERGY_CAPACITY - energy;
    }

    @Override
    public int getSinkTier() {
        return 4;
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
        return slot >= SLOT_INPUT_START && slot < SLOT_INPUT_START + SLOT_INPUT_COUNT
            && MolecularRecipes.find(stack) != null;
    }

    // ----- NBT -----

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        energy = nbt.getInteger("Energy");
        progress = nbt.getInteger("Progress");
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
        nbt.setInteger("Progress", progress);
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
