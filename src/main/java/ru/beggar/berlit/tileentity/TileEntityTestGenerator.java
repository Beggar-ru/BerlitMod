package ru.beggar.berlit.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.common.Loader;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IEnergySource;

public class TileEntityTestGenerator extends TileEntity implements IEnergySource {

    /** EU/t produced, same value as the Advanced Solar Panel. */
    public static final int GENERATION_PER_TICK = 8;
    public static final int ENERGY_CAPACITY = 8000;

    private double energy;
    private boolean addedToEnergyNet;

    @Override
    public void updateEntity() {
        super.updateEntity();
        if (worldObj.isRemote) return;

        if (!addedToEnergyNet && Loader.isModLoaded("IC2")) {
            MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
            addedToEnergyNet = true;
        }

        if (energy < ENERGY_CAPACITY) {
            energy = Math.min(ENERGY_CAPACITY, energy + GENERATION_PER_TICK);
        }
    }

    public double getEnergy() {
        return energy;
    }

    public int getMaxEnergy() {
        return ENERGY_CAPACITY;
    }

    @Override
    public double getOfferedEnergy() {
        return Math.min(GENERATION_PER_TICK, energy);
    }

    @Override
    public void drawEnergy(double amount) {
        energy = Math.max(0.0D, energy - amount);
    }

    @Override
    public int getSourceTier() {
        return 1;
    }

    @Override
    public boolean emitsEnergyTo(TileEntity receiver, ForgeDirection direction) {
        return direction != ForgeDirection.UP;
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

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        energy = nbt.getDouble("Energy");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setDouble("Energy", energy);
    }
}
