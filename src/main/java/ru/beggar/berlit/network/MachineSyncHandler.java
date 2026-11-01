package ru.beggar.berlit.network;

import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import ru.beggar.berlit.tileentity.TileEntityMolecularTransformer;

public class MachineSyncHandler implements IMessageHandler<PacketMachineSync, IMessage> {

    @Override
    public IMessage onMessage(final PacketMachineSync message, MessageContext ctx) {
        Minecraft.getMinecraft()
            .func_152344_a(new Runnable() {

                @Override
                public void run() {
                    World world = Minecraft.getMinecraft().theWorld;
                    if (world == null) return;

                    TileEntity tile = world.getTileEntity(message.x, message.y, message.z);
                    if (tile instanceof TileEntityMolecularTransformer) {
                        ((TileEntityMolecularTransformer) tile).setClientData(
                            message.energy,
                            message.progress,
                            message.recipeEnergy,
                            message.lastInput,
                            message.active);
                    }
                }
            });
        return null;
    }
}
