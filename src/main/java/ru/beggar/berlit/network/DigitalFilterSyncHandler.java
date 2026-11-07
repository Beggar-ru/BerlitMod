package ru.beggar.berlit.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import ru.beggar.berlit.ae2.DigitalRecipeFilter;
import ru.beggar.berlit.ae2.GuiDigitalFilter;
import ru.beggar.berlit.machine.MachineType;

public class DigitalFilterSyncHandler implements IMessageHandler<PacketDigitalFilterSync, IMessage> {

    @Override
    public IMessage onMessage(final PacketDigitalFilterSync message, MessageContext ctx) {
        Minecraft.getMinecraft()
            .func_152344_a(new Runnable() {

                @Override
                public void run() {
                    MachineType type = MachineType.byId(message.machineType);
                    if (type == null) return;

                    DigitalRecipeFilter.setClientDisabled(type, message.keys);

                    GuiScreen screen = Minecraft.getMinecraft().currentScreen;
                    if (screen instanceof GuiDigitalFilter) {
                        ((GuiDigitalFilter) screen).onFilterSync();
                    }
                }
            });
        return null;
    }
}
