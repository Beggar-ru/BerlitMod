package ru.beggar.berlit.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

public class PacketMachineSync implements IMessage {

    public int x;
    public int y;
    public int z;
    public int energy;
    public int progress;
    public int recipeEnergy;
    public int lastInput;
    public int mode;
    public boolean active;

    public PacketMachineSync() {}

    public PacketMachineSync(int x, int y, int z, int energy, int progress, int recipeEnergy, int lastInput, int mode,
        boolean active) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.energy = energy;
        this.progress = progress;
        this.recipeEnergy = recipeEnergy;
        this.lastInput = lastInput;
        this.mode = mode;
        this.active = active;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        energy = buf.readInt();
        progress = buf.readInt();
        recipeEnergy = buf.readInt();
        lastInput = buf.readInt();
        mode = buf.readInt();
        active = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(energy);
        buf.writeInt(progress);
        buf.writeInt(recipeEnergy);
        buf.writeInt(lastInput);
        buf.writeInt(mode);
        buf.writeBoolean(active);
    }
}
