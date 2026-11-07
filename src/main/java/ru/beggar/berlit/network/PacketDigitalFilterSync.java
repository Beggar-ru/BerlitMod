package ru.beggar.berlit.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

/** Server sends the disabled-pattern keys of one digital machine type. */
public class PacketDigitalFilterSync implements IMessage {

    public String machineType;
    public String[] keys;
    public int revision;

    public PacketDigitalFilterSync() {}

    public PacketDigitalFilterSync(String machineType, String[] keys, int revision) {
        this.machineType = machineType;
        this.keys = keys == null ? new String[0] : keys;
        this.revision = revision;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        machineType = ByteBufUtils.readUTF8String(buf);
        int count = buf.readInt();
        keys = new String[Math.max(0, count)];
        for (int i = 0; i < keys.length; i++) {
            keys[i] = ByteBufUtils.readUTF8String(buf);
        }
        revision = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, machineType);
        buf.writeInt(keys.length);
        for (String key : keys) {
            ByteBufUtils.writeUTF8String(buf, key);
        }
        buf.writeInt(revision);
    }
}
