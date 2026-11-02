package ru.beggar.berlit.network;

import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import ru.beggar.berlit.tileentity.TileEntitySpawner;

public class SpawnerSyncHandler implements IMessageHandler<PacketSpawnerSync, IMessage> {

    @Override
    public IMessage onMessage(final PacketSpawnerSync message, MessageContext ctx) {
        Minecraft.getMinecraft()
            .func_152344_a(new Runnable() {

                @Override
                public void run() {
                    World world = Minecraft.getMinecraft().theWorld;
                    if (world == null) return;

                    TileEntity tile = world.getTileEntity(message.x, message.y, message.z);
                    if (tile instanceof TileEntitySpawner) {
                        ((TileEntitySpawner) tile).setClientData(message.cycle, message.interval, message.active);
                    }
                }
            });
        return null;
    }
}
