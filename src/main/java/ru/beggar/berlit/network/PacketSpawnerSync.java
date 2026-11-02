package ru.beggar.berlit.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

public class PacketSpawnerSync implements IMessage {

    public int x;
    public int y;
    public int z;
    public int cycle;
    public int interval;
    public boolean active;

    public PacketSpawnerSync() {}

    public PacketSpawnerSync(int x, int y, int z, int cycle, int interval, boolean active) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.cycle = cycle;
        this.interval = interval;
        this.active = active;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        cycle = buf.readInt();
        interval = buf.readInt();
        active = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(cycle);
        buf.writeInt(interval);
        buf.writeBoolean(active);
    }
}
