package ru.beggar.berlit.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import ru.beggar.berlit.ae2.DigitalRecipeFilter;
import ru.beggar.berlit.ae2.TileEntityDigitalMachine;

/** Client asks a digital machine for the current recipe-filter state. */
public class PacketDigitalFilterRequest implements IMessage {

    private int x;
    private int y;
    private int z;

    public PacketDigitalFilterRequest() {}

    public PacketDigitalFilterRequest(int x, int y, int z) {
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

    public static class Handler implements IMessageHandler<PacketDigitalFilterRequest, IMessage> {

        @Override
        public IMessage onMessage(PacketDigitalFilterRequest message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            if (player == null) return null;

            World world = player.worldObj;
            TileEntity tile = world.getTileEntity(message.x, message.y, message.z);
            if (tile instanceof TileEntityDigitalMachine
                && ((TileEntityDigitalMachine) tile).isUseableByPlayer(player)) {
                TileEntityDigitalMachine machine = (TileEntityDigitalMachine) tile;
                ModNetwork.INSTANCE.sendTo(
                    new PacketDigitalFilterSync(
                        machine.getMachineType()
                            .getId(),
                        DigitalRecipeFilter.getDisabledKeys(world, machine.getMachineType()),
                        DigitalRecipeFilter.getRevision()),
                    player);
            }
            return null;
        }
    }
}
