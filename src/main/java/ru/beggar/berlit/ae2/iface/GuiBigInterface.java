package ru.beggar.berlit.ae2.iface;

import net.minecraft.inventory.Slot;
import net.minecraft.util.StatCollector;

import appeng.client.gui.implementations.GuiUpgradeable;
import ru.beggar.berlit.gui.GuiKit;
import ru.beggar.berlit.gui.GuiTheme;

/** Berlit-styled GUI for the expanded ME Interfaces. */
public class GuiBigInterface extends GuiUpgradeable {

    private final ContainerBigInterface container;

    public GuiBigInterface(ContainerBigInterface container) {
        super(container);
        this.container = container;
        this.xSize = 211;
        this.ySize = container.getGuiHeight();
    }

    @Override
    protected void addButtons() {}

    @Override
    protected String getBackground() {
        return "berlit:textures/gui/kit.png";
    }

    @Override
    public void drawBG(int x, int y, int mouseX, int mouseY) {
        int width = this.xSize;
        int height = this.ySize;
        drawGradientRect(x, y, x + width, y + height, GuiTheme.PANEL_TOP, GuiTheme.PANEL_BOTTOM);
        bindTexture("berlit:textures/gui/kit.png");
        GuiKit.PANEL.draw(this.zLevel, x, y, width, height);

        for (Object entry : this.inventorySlots.inventorySlots) {
            Slot slot = (Slot) entry;
            if (slot.xDisplayPosition < 0 || slot.yDisplayPosition < 0) continue;
            GuiKit.CELL.drawSprite(this.zLevel, x + slot.xDisplayPosition - 1, y + slot.yDisplayPosition - 1, 18, 18);
        }
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        drawString(fontRendererObj, StatCollector.translateToLocal("berlit.gui.interface.title"), 8, 7, GuiTheme.TITLE);
        drawString(
            fontRendererObj,
            StatCollector.translateToLocal("berlit.gui.interface.patterns"),
            8,
            container.getPatternLabelY(),
            GuiTheme.ACCENT);
        drawString(
            fontRendererObj,
            StatCollector.translateToLocal("berlit.gui.interface.config"),
            8,
            26,
            GuiTheme.TEXT);
        drawString(
            fontRendererObj,
            StatCollector.translateToLocal("berlit.gui.interface.storage"),
            8,
            44,
            GuiTheme.TEXT);
        drawString(
            fontRendererObj,
            StatCollector.translateToLocal("berlit.gui.interface.upgrades"),
            187,
            0,
            GuiTheme.TEXT_DISABLED);
        drawString(
            fontRendererObj,
            StatCollector.translateToLocal("container.inventory"),
            8,
            container.getGuiHeight() - 93,
            GuiTheme.TEXT);
    }
}
