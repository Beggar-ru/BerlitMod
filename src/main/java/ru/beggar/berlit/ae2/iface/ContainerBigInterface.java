package ru.beggar.berlit.ae2.iface;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;

import appeng.api.util.IConfigManager;
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

        // AE2 placed the player slots for its own 211px screen; move them under the grid so the
        // inventory panel lines up. Picking them by class matters: the pattern and upgrade slots are
        // added at other points, so index arithmetic would move the wrong ones and overlap the grid.
        int inventoryTop = getInventoryTop();
        int player = 0;
        for (Object entry : inventorySlots) {
            boolean hotbar = entry instanceof appeng.container.slot.SlotPlayerHotBar;
            if (!hotbar && !(entry instanceof appeng.container.slot.SlotPlayerInv)) {
                continue;
            }
            Slot slot = (Slot) entry;
            slot.xDisplayPosition = 8 + (player % 9) * 18;
            slot.yDisplayPosition = hotbar ? inventoryTop + 58 : inventoryTop + (player / 9) * 18;
            player++;
        }
    }

    protected abstract int getPatternRows();

    protected abstract int getPatternY();

    protected abstract int getPatternStep();

    /** Bottom edge of the pattern grid, in GUI pixels. */
    public int getPatternBottom() {
        return getPatternY() + getPatternRows() * getPatternStep();
    }

    /** Player inventory sits under the pattern grid with the same breathing room as on the machines. */
    public int getInventoryTop() {
        return getPatternBottom() + 24;
    }

    public int getGuiHeight() {
        return getInventoryTop() + 84;
    }

    public int getPatternLabelY() {
        // a clear 3px gap above the first cell, so the text never touches the grid
        return getPatternY() - 13;
    }

    @Override
    protected void setupConfig() {
        setupUpgrades();
    }

    /**
     * The expanded interface GUI has no fuzzy/redstone/block-mode buttons. The base
     * ContainerUpgradeable implementation nevertheless asks the DualityInterface config
     * manager for those settings and crashes with "Expected a non-null value for
     * FUZZY_MODE", so this screen deliberately loads none of them.
     */
    @Override
    protected void loadSettingsFromHost(IConfigManager configManager) {}

    @Override
    public int availableUpgrades() {
        return 1;
    }
}
