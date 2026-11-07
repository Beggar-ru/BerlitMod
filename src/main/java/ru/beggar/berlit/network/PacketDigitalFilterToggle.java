package ru.beggar.berlit.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import ru.beggar.berlit.ae2.DigitalRecipeFilter;
import ru.beggar.berlit.ae2.TileEntityDigitalMachine;

/** Client enables/disables one digital recipe. */
public class PacketDigitalFilterToggle implements IMessage {

    private int x;
    private int y;
    private int z;
    private String key;
    private boolean enabled;

    public PacketDigitalFilterToggle() {}

    public PacketDigitalFilterToggle(int x, int y, int z, String key, boolean enabled) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.key = key;
        this.enabled = enabled;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        key = ByteBufUtils.readUTF8String(buf);
        enabled = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        ByteBufUtils.writeUTF8String(buf, key);
        buf.writeBoolean(enabled);
    }

    public static class Handler implements IMessageHandler<PacketDigitalFilterToggle, IMessage> {

        @Override
        public IMessage onMessage(PacketDigitalFilterToggle message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            if (player == null) return null;

            World world = player.worldObj;
            TileEntity tile = world.getTileEntity(message.x, message.y, message.z);
            if (tile instanceof TileEntityDigitalMachine
                && ((TileEntityDigitalMachine) tile).isUseableByPlayer(player)) {
                TileEntityDigitalMachine machine = (TileEntityDigitalMachine) tile;
                DigitalRecipeFilter.setEnabled(world, machine.getMachineType(), message.key, message.enabled);
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
