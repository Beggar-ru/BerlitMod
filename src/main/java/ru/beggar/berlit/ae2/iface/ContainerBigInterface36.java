package ru.beggar.berlit.ae2.iface;

import net.minecraft.entity.player.InventoryPlayer;

import appeng.helpers.IInterfaceHost;

/** 36-pattern ME Interface container. */
public class ContainerBigInterface36 extends ContainerBigInterface {

    public ContainerBigInterface36(InventoryPlayer playerInventory, IInterfaceHost host) {
        super(playerInventory, host);
    }

    @Override
    protected int getPatternRows() {
        return 4;
    }

    @Override
    protected int getPatternY() {
        return 78;
    }

    @Override
    protected int getPatternStep() {
        return 19;
    }

    @Override
    protected int getHeight() {
        return 250;
    }
}
