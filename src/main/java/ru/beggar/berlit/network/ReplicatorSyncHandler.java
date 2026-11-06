package ru.beggar.berlit.network;

import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import ru.beggar.berlit.tileentity.TileEntityReplicator;

public class ReplicatorSyncHandler implements IMessageHandler<PacketReplicatorSync, IMessage> {

    @Override
    public IMessage onMessage(final PacketReplicatorSync message, MessageContext ctx) {
        Minecraft.getMinecraft()
            .func_152344_a(new Runnable() {

                @Override
                public void run() {
                    World world = Minecraft.getMinecraft().theWorld;
                    if (world == null) return;

                    TileEntity tile = world.getTileEntity(message.x, message.y, message.z);
                    if (tile instanceof TileEntityReplicator) {
                        ((TileEntityReplicator) tile).setClientData(
                            message.energy,
                            message.maxEnergy,
                            message.fluid,
                            message.fluidCapacity,
                            message.progress,
                            message.requiredMatter,
                            message.matterPerItem,
                            message.stackSize,
                            message.patternIndex,
                            message.patternCount,
                            message.status,
                            message.lastInput,
                            message.active,
                            message.pattern);
                    }
                }
            });
        return null;
    }
}
