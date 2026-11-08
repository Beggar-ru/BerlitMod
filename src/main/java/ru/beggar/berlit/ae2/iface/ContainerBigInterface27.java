package ru.beggar.berlit.ae2.iface;

import net.minecraft.entity.player.InventoryPlayer;

import appeng.helpers.IInterfaceHost;

/** 27-pattern ME Interface container. */
public class ContainerBigInterface27 extends ContainerBigInterface {

    public ContainerBigInterface27(InventoryPlayer playerInventory, IInterfaceHost host) {
        super(playerInventory, host);
    }

    @Override
    protected int getPatternRows() {
        return 3;
    }

    @Override
    protected int getPatternY() {
        return 97;
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
