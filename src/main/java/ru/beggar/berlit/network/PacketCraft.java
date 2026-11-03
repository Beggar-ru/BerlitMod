package ru.beggar.berlit.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

/** Client asks the machine to pull one recipe from the ME network and craft it. */
public class PacketCraft implements IMessage {

    private int x, y, z;

    public PacketCraft() {}

    public PacketCraft(int x, int y, int z) {
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

    public static class Handler implements IMessageHandler<PacketCraft, IMessage> {

        @Override
        public IMessage onMessage(PacketCraft message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            World world = player.worldObj;
            TileEntity tile = world.getTileEntity(message.x, message.y, message.z);
            if (tile instanceof ru.beggar.berlit.ae2.TileEntityAvaritiaAssembler) {
                ((ru.beggar.berlit.ae2.TileEntityAvaritiaAssembler) tile).craftOnce();
            } else if (tile instanceof ru.beggar.berlit.machine.TileEntityNeutronCompressor) {
                ((ru.beggar.berlit.machine.TileEntityNeutronCompressor) tile).craftOnce();
            }
            return null;
        }
    }
}
