package ru.beggar.berlit.network;

import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

/** Server to client snapshot of a digital machine. */
public class PacketDigitalSync implements IMessage {

    public int x;
    public int y;
    public int z;
    public int energy;
    public int maxEnergy;
    public int progressTicks;
    public int requiredTicks;
    public int requiredEnergy;
    public int queueSize;
    public int recipeCount;
    public int status;
    public int lastInput;
    public boolean active;
    public ItemStack output;

    public PacketDigitalSync() {}

    public PacketDigitalSync(int x, int y, int z, int energy, int maxEnergy, int progressTicks, int requiredTicks,
        int requiredEnergy, int queueSize, int recipeCount, int status, int lastInput, boolean active,
        ItemStack output) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.energy = energy;
        this.maxEnergy = maxEnergy;
        this.progressTicks = progressTicks;
        this.requiredTicks = requiredTicks;
        this.requiredEnergy = requiredEnergy;
        this.queueSize = queueSize;
        this.recipeCount = recipeCount;
        this.status = status;
        this.lastInput = lastInput;
        this.active = active;
        this.output = output;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        energy = buf.readInt();
        maxEnergy = buf.readInt();
        progressTicks = buf.readInt();
        requiredTicks = buf.readInt();
        requiredEnergy = buf.readInt();
        queueSize = buf.readInt();
        recipeCount = buf.readInt();
        status = buf.readInt();
        lastInput = buf.readInt();
        active = buf.readBoolean();
        output = ByteBufUtils.readItemStack(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(energy);
        buf.writeInt(maxEnergy);
        buf.writeInt(progressTicks);
        buf.writeInt(requiredTicks);
        buf.writeInt(requiredEnergy);
        buf.writeInt(queueSize);
        buf.writeInt(recipeCount);
        buf.writeInt(status);
        buf.writeInt(lastInput);
        buf.writeBoolean(active);
        ByteBufUtils.writeItemStack(buf, output);
    }
}
