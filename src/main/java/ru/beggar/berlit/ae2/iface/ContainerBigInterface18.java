package ru.beggar.berlit.ae2.iface;

import net.minecraft.entity.player.InventoryPlayer;

import appeng.helpers.IInterfaceHost;

/** 18-pattern ME Interface container. */
public class ContainerBigInterface18 extends ContainerBigInterface {

    public ContainerBigInterface18(InventoryPlayer playerInventory, IInterfaceHost host) {
        super(playerInventory, host);
    }

    @Override
    protected int getPatternRows() {
        return 2;
    }

    @Override
    protected int getPatternY() {
        return 97;
    }

    @Override
    protected int getPatternStep() {
        return 18;
    }

    @Override
    protected int getHeight() {
        return 229;
    }
}
