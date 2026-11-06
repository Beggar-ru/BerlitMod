package ru.beggar.berlit.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import ru.beggar.berlit.tileentity.TileEntityReplicator;

/** Client asks the replicator to select the previous/next remembered pattern. */
public class PacketReplicatorAction implements IMessage {

    private int x;
    private int y;
    private int z;
    private int action;

    public PacketReplicatorAction() {}

    public PacketReplicatorAction(int x, int y, int z, int action) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.action = action;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        action = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(action);
    }

    public static class Handler implements IMessageHandler<PacketReplicatorAction, IMessage> {

        @Override
        public IMessage onMessage(PacketReplicatorAction message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            if (player == null) return null;

            World world = player.worldObj;
            if (world == null) return null;

            TileEntity tile = world.getTileEntity(message.x, message.y, message.z);
            if (tile instanceof TileEntityReplicator && ((TileEntityReplicator) tile).isUseableByPlayer(player)) {
                ((TileEntityReplicator) tile).handleAction(message.action);
            }
            return null;
        }
    }
}
