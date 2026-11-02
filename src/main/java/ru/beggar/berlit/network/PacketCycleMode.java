package ru.beggar.berlit.network;

import net.minecraft.tileentity.TileEntity;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import ru.beggar.berlit.tileentity.TileEntityProcessingMachine;

/** Client -> server: cycle the metal former mode. */
public class PacketCycleMode implements IMessage {

    public int x;
    public int y;
    public int z;

    public PacketCycleMode() {}

    public PacketCycleMode(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void fromBytes(io.netty.buffer.ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
    }

    @Override
    public void toBytes(io.netty.buffer.ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
    }

    public static class Handler implements IMessageHandler<PacketCycleMode, IMessage> {

        @Override
        public IMessage onMessage(final PacketCycleMode message, MessageContext ctx) {
            TileEntity tile = ctx.getServerHandler().playerEntity.worldObj
                .getTileEntity(message.x, message.y, message.z);
            if (tile instanceof TileEntityProcessingMachine) {
                ((TileEntityProcessingMachine) tile).cycleMode();
            }
            return null;
        }
    }
}
