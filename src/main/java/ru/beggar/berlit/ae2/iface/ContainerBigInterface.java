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

        // AE2 placed the 36 player slots for its own 211px screen; move them under the grid so the
        // inventory panel lines up with the rest of the mod.
        int inventoryTop = getInventoryTop();
        for (int i = 0; i < 36 && i < inventorySlots.size(); i++) {
            Slot slot = (Slot) inventorySlots.get(i);
            if (i < 27) {
                slot.xDisplayPosition = 8 + (i % 9) * 18;
                slot.yDisplayPosition = inventoryTop + (i / 9) * 18;
            } else {
                slot.xDisplayPosition = 8 + (i - 27) * 18;
                slot.yDisplayPosition = inventoryTop + 58;
            }
        }
    }

    protected abstract int getPatternRows();

    protected abstract int getPatternY();

    protected abstract int getPatternStep();

    /** Player inventory sits under the pattern grid with the same breathing room as on the machines. */
    public int getInventoryTop() {
        return getPatternY() + getPatternRows() * getPatternStep() + 24;
    }

    public int getGuiHeight() {
        return getInventoryTop() + 84;
    }

    public int getPatternLabelY() {
        return getPatternY() - 10;
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
