package ru.beggar.berlit.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import ru.beggar.berlit.ae2.TileEntityPatternEncoder;

public class PacketEncode implements IMessage {

    private int x, y, z;

    public PacketEncode() {}

    public PacketEncode(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
    }

    public static class Handler implements IMessageHandler<PacketEncode, IMessage> {

        @Override
        public IMessage onMessage(PacketEncode message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            World world = player.worldObj;
            TileEntity tile = world.getTileEntity(message.x, message.y, message.z);
            if (tile instanceof TileEntityPatternEncoder) {
                ((TileEntityPatternEncoder) tile).encode();
            }
            return null;
        }
    }
}
