package ru.beggar.berlit.network;

import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import ru.beggar.berlit.ae2.TileEntityDigitalMachine;

public class DigitalSyncHandler implements IMessageHandler<PacketDigitalSync, IMessage> {

    @Override
    public IMessage onMessage(final PacketDigitalSync message, MessageContext ctx) {
        Minecraft.getMinecraft()
            .func_152344_a(new Runnable() {

                @Override
                public void run() {
                    World world = Minecraft.getMinecraft().theWorld;
                    if (world == null) return;

                    TileEntity tile = world.getTileEntity(message.x, message.y, message.z);
                    if (tile instanceof TileEntityDigitalMachine) {
                        ((TileEntityDigitalMachine) tile).setClientData(
                            message.energy,
                            message.maxEnergy,
                            message.progressTicks,
                            message.requiredTicks,
                            message.requiredEnergy,
                            message.queueSize,
                            message.recipeCount,
                            message.status,
                            message.lastInput,
                            message.active,
                            message.output);
                    } else if (tile instanceof ru.beggar.berlit.ae2.TileEntityDigitalAvaritiaAssembler) {
                        ((ru.beggar.berlit.ae2.TileEntityDigitalAvaritiaAssembler) tile).setClientData(
                            message.energy,
                            message.maxEnergy,
                            message.progressTicks,
                            message.requiredTicks,
                            message.requiredEnergy,
                            message.queueSize,
                            message.recipeCount,
                            message.status,
                            message.lastInput,
                            message.active,
                            message.output);
                    } else if (tile instanceof ru.beggar.berlit.ae2.TileEntityDigitalNeutronCompressor) {
                        ((ru.beggar.berlit.ae2.TileEntityDigitalNeutronCompressor) tile).setClientData(
                            message.energy,
                            message.maxEnergy,
                            message.progressTicks,
                            message.requiredTicks,
                            message.requiredEnergy,
                            message.queueSize,
                            message.recipeCount,
                            message.status,
                            message.lastInput,
                            message.active,
                            message.output);
                    }
                }
            });
        return null;
    }
}
