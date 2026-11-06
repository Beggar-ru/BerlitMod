package ru.beggar.berlit.network;

import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

/** Server to client snapshot of a replicator's state, including the selected remembered item. */
public class PacketReplicatorSync implements IMessage {

    public int x;
    public int y;
    public int z;
    public int energy;
    public int maxEnergy;
    public int fluid;
    public int fluidCapacity;
    public int progress;
    public int requiredMatter;
    public int matterPerItem;
    public int stackSize;
    public int patternIndex;
    public int patternCount;
    public int status;
    public int lastInput;
    public boolean active;
    public ItemStack pattern;

    public PacketReplicatorSync() {}

    public PacketReplicatorSync(int x, int y, int z, int energy, int maxEnergy, int fluid, int fluidCapacity,
        int progress, int requiredMatter, int matterPerItem, int stackSize, int patternIndex, int patternCount,
        int status, int lastInput, boolean active, ItemStack pattern) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.energy = energy;
        this.maxEnergy = maxEnergy;
        this.fluid = fluid;
        this.fluidCapacity = fluidCapacity;
        this.progress = progress;
        this.requiredMatter = requiredMatter;
        this.matterPerItem = matterPerItem;
        this.stackSize = stackSize;
        this.patternIndex = patternIndex;
        this.patternCount = patternCount;
        this.status = status;
        this.lastInput = lastInput;
        this.active = active;
        this.pattern = pattern;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        energy = buf.readInt();
        maxEnergy = buf.readInt();
        fluid = buf.readInt();
        fluidCapacity = buf.readInt();
        progress = buf.readInt();
        requiredMatter = buf.readInt();
        matterPerItem = buf.readInt();
        stackSize = buf.readInt();
        patternIndex = buf.readInt();
        patternCount = buf.readInt();
        status = buf.readInt();
        lastInput = buf.readInt();
        active = buf.readBoolean();
        pattern = ByteBufUtils.readItemStack(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(energy);
        buf.writeInt(maxEnergy);
        buf.writeInt(fluid);
        buf.writeInt(fluidCapacity);
        buf.writeInt(progress);
        buf.writeInt(requiredMatter);
        buf.writeInt(matterPerItem);
        buf.writeInt(stackSize);
        buf.writeInt(patternIndex);
        buf.writeInt(patternCount);
        buf.writeInt(status);
        buf.writeInt(lastInput);
        buf.writeBoolean(active);
        ByteBufUtils.writeItemStack(buf, pattern);
    }
}
