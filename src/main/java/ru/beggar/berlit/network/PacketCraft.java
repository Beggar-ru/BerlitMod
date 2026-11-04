package ru.beggar.berlit.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

/** Client asks the machine to pull a number of recipes from the ME network. */
public class PacketCraft implements IMessage {

    private int x, y, z;
    private int amount = 1;

    public PacketCraft() {}

    public PacketCraft(int x, int y, int z) {
        this(x, y, z, 1);
    }

    public PacketCraft(int x, int y, int z, int amount) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.amount = Math.max(1, amount);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        amount = Math.max(1, buf.readInt());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(amount);
    }

    public static class Handler implements IMessageHandler<PacketCraft, IMessage> {

        @Override
        public IMessage onMessage(PacketCraft message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            World world = player.worldObj;
            TileEntity tile = world.getTileEntity(message.x, message.y, message.z);

            for (int i = 0; i < message.amount; i++) {
                boolean ok = false;
                if (tile instanceof ru.beggar.berlit.ae2.TileEntityAvaritiaAssembler) {
                    ok = ((ru.beggar.berlit.ae2.TileEntityAvaritiaAssembler) tile).craftOnce();
                } else if (tile instanceof ru.beggar.berlit.machine.TileEntityNeutronCompressor) {
                    ok = ((ru.beggar.berlit.machine.TileEntityNeutronCompressor) tile).craftOnce();
                }
                if (!ok) break;
            }
            return null;
        }
    }
}
