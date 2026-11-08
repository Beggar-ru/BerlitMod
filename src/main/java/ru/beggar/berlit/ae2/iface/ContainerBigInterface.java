package ru.beggar.berlit.ae2.iface;

import net.minecraft.entity.player.InventoryPlayer;

import appeng.container.implementations.ContainerUpgradeable;
import appeng.container.slot.SlotFake;
import appeng.container.slot.SlotNormal;
import appeng.container.slot.SlotRestrictedInput;
import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;

/** Shared container for the 18/27/36 slot ME Interfaces. */
public abstract class ContainerBigInterface extends ContainerUpgradeable {

    private final DualityInterface duality;

    protected ContainerBigInterface(InventoryPlayer playerInventory, IInterfaceHost host) {
        super(
            playerInventory,
            host.getInterfaceDuality()
                .getHost());
        this.duality = host.getInterfaceDuality();

        int patterns = getPatternRows() * 9;
        for (int i = 0; i < patterns; i++) {
            int row = i / 9;
            int column = i % 9;
            addSlotToContainer(
                new SlotRestrictedInput(
                    SlotRestrictedInput.PlacableItemType.ENCODED_PATTERN,
                    duality.getPatterns(),
                    i,
                    8 + column * 18,
                    getPatternY() + row * getPatternStep(),
                    playerInventory));
        }

        for (int i = 0; i < 9; i++) {
            addSlotToContainer(new SlotFake(duality.getConfig(), i, 8 + i * 18, 35));
        }
        for (int i = 0; i < 9; i++) {
            addSlotToContainer(new SlotNormal(duality.getStorage(), i, 8 + i * 18, 53));
        }
    }

    protected abstract int getPatternRows();

    protected abstract int getPatternY();

    protected abstract int getPatternStep();

    public int getGuiHeight() {
        return getHeight();
    }

    public int getPatternLabelY() {
        return getPatternY() - 10;
    }

    @Override
    protected void setupConfig() {
        setupUpgrades();
    }

    @Override
    public int availableUpgrades() {
        return 1;
    }
}
