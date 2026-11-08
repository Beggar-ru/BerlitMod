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
        // The pattern grid is 9 columns wide, so 178px hugs it with even margins. AE2 still places its
        // upgrade slot at 187, which would fall outside; it is moved into the title row instead.
        this.xSize = 178;
        this.ySize = container.getGuiHeight();

        for (Object entry : this.inventorySlots.inventorySlots) {
            Slot slot = (Slot) entry;
            if (slot.xDisplayPosition >= this.xSize) {
                slot.xDisplayPosition = 152;
                slot.yDisplayPosition = 6;
            }
        }
    }

    @Override
    protected void addButtons() {}

    @Override
    protected String getBackground() {
        return "gui/interface";
    }

    @Override
    public void drawBG(int x, int y, int mouseX, int mouseY) {
        int width = this.xSize;
        int inventoryTop = container.getInventoryTop() - 20;

        net.minecraft.client.Minecraft.getMinecraft()
            .getTextureManager()
            .bindTexture(GuiKit.KIT);

        // content panel and a separate inventory panel, like the machine screens
        drawGradientRect(x, y, x + width, y + inventoryTop - 8, GuiTheme.PANEL_TOP, GuiTheme.PANEL_BOTTOM);
        GuiKit.PANEL.draw(this.zLevel, x, y, width, inventoryTop - 8);

        drawGradientRect(x, y + inventoryTop, x + width, y + this.ySize, GuiTheme.PANEL_TOP, GuiTheme.PANEL_BOTTOM);
        GuiKit.PANEL.draw(this.zLevel, x, y + inventoryTop, width, this.ySize - inventoryTop);

        drawRect(x + 8, y + 15, x + 146, y + 16, GuiTheme.FRAME_BORDER);
        drawRect(x + 8, y + 16, x + 146, y + 17, GuiTheme.FRAME_RULE);

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
            container.getInventoryTop() - 14,
            GuiTheme.TEXT);
    }
}
