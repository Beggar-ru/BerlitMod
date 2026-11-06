package ru.beggar.berlit.ae2;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import ru.beggar.berlit.gui.BerlitGui;
import ru.beggar.berlit.gui.GuiKit;
import ru.beggar.berlit.gui.GuiTheme;
import ru.beggar.berlit.gui.MachineLayout;

public class GuiDigitalMachine extends BerlitGui {

    private final TileEntityDigitalMachine tile;

    public GuiDigitalMachine(InventoryPlayer playerInventory, TileEntityDigitalMachine tile) {
        super(new ContainerDigitalMachine(playerInventory, tile));
        this.tile = tile;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        fontRendererObj.drawString(
            StatCollector.translateToLocal(
                tile.getMachineType()
                    .getDigitalTitleKey()),
            8,
            6,
            GuiTheme.TITLE);

        boolean online = tile.getStatus() != TileEntityDigitalMachine.STATUS_OFFLINE;
        fontRendererObj
            .drawString(
                StatCollector.translateToLocalFormatted(
                    "berlit.gui.digital.me",
                    StatCollector
                        .translateToLocal(online ? "berlit.gui.digital.online" : "berlit.gui.digital.offline")),
                16,
                25,
                online ? GuiTheme.ACCENT : 0xFFFF7A6B);
        drawRightAligned(
            StatCollector.translateToLocalFormatted("berlit.gui.digital.recipes", tile.getRecipeCount()),
            184,
            25,
            GuiTheme.TEXT);
        fontRendererObj.drawString(
            StatCollector.translateToLocalFormatted("berlit.gui.digital.queue", tile.getQueueSize()),
            16,
            37,
            GuiTheme.TEXT);

        ItemStack output = tile.getCurrentOutput();
        String processing = output == null ? StatCollector.translateToLocal("berlit.gui.digital.idle")
            : output.getDisplayName();
        drawTrimmed(
            StatCollector.translateToLocalFormatted("berlit.gui.digital.processing", processing),
            16,
            49,
            168,
            output == null ? GuiTheme.TEXT_DISABLED : GuiTheme.TEXT);

        fontRendererObj.drawString(
            StatCollector.translateToLocalFormatted("berlit.gui.energy", tile.getLastInput()),
            12,
            78,
            GuiTheme.TEXT);

        drawRightAligned(tile.getProgressScaled(100) + "%", 186, 78, GuiTheme.ACCENT);
        fontRendererObj.drawString(statusText(), 8, 116, statusColor());

        drawInfoTooltip(mouseX, mouseY, infoLines());
    }

    private List<String> infoLines() {
        List<String> lines = new ArrayList<String>();
        lines.add(
            StatCollector.translateToLocalFormatted("berlit.gui.info.energy", tile.getEnergy(), tile.getMaxEnergy()));
        lines.add(StatCollector.translateToLocalFormatted("berlit.gui.digital.recipes", tile.getRecipeCount()));
        lines.add(StatCollector.translateToLocalFormatted("berlit.gui.digital.queue", tile.getQueueSize()));
        lines
            .add(StatCollector.translateToLocalFormatted("berlit.gui.digital.per_operation", tile.getRequiredEnergy()));
        lines.add(StatCollector.translateToLocal("berlit.gui.digital.info.auto"));
        return lines;
    }

    private String statusText() {
        switch (tile.getStatus()) {
            case TileEntityDigitalMachine.STATUS_READY:
                return StatCollector.translateToLocal("berlit.gui.digital.status.ready");
            case TileEntityDigitalMachine.STATUS_WORKING:
                return StatCollector.translateToLocal("berlit.gui.digital.status.working");
            case TileEntityDigitalMachine.STATUS_NO_ENERGY:
                return StatCollector.translateToLocal("berlit.gui.digital.status.no_energy");
            case TileEntityDigitalMachine.STATUS_NO_RECIPES:
                return StatCollector.translateToLocal("berlit.gui.digital.status.no_recipes");
            case TileEntityDigitalMachine.STATUS_QUEUE_FULL:
                return StatCollector.translateToLocal("berlit.gui.digital.status.queue_full");
            default:
                return StatCollector.translateToLocal("berlit.gui.digital.status.offline");
        }
    }

    private int statusColor() {
        switch (tile.getStatus()) {
            case TileEntityDigitalMachine.STATUS_READY:
                return GuiTheme.TEXT;
            case TileEntityDigitalMachine.STATUS_WORKING:
                return GuiTheme.ACCENT;
            default:
                return 0xFFFFA35C;
        }
    }

    private void drawTrimmed(String text, int x, int y, int width, int color) {
        fontRendererObj.drawString(fontRendererObj.trimStringToWidth(text, width), x, y, color);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager()
            .bindTexture(GuiKit.KIT);

        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;

        drawKitPanel(x, y, MachineLayout.WIDTH, MachineLayout.CONTENT_HEIGHT);
        drawKitPanel(
            x + MachineLayout.INVENTORY_X,
            y + MachineLayout.INVENTORY_TOP,
            MachineLayout.INVENTORY_WIDTH,
            MachineLayout.INVENTORY_HEIGHT);

        drawRect(x + 8, y + 15, x + MachineLayout.WIDTH - 9, y + 16, GuiTheme.FRAME_BORDER);
        drawRect(x + 8, y + 16, x + MachineLayout.WIDTH - 9, y + 17, GuiTheme.FRAME_RULE);

        drawDigitalScreen(x, y);

        for (int i = 0; i < TileEntityDigitalMachine.UPGRADE_SLOTS; i++) {
            drawKitCell(x, y, MachineLayout.UPGRADE_X + i * MachineLayout.SLOT, MachineLayout.UPGRADE_Y);
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawKitCell(
                    x,
                    y,
                    MachineLayout.PLAYER_X + col * MachineLayout.SLOT,
                    MachineLayout.PLAYER_TOP + row * MachineLayout.SLOT);
            }
        }
        for (int col = 0; col < 9; col++) {
            drawKitCell(x, y, MachineLayout.PLAYER_X + col * MachineLayout.SLOT, MachineLayout.HOTBAR_Y);
        }

        drawProgressTrough(x, y);
        drawProgressBar(x, y, tile.getProgressScaled(100));
        drawInfoBadge(x + MachineLayout.INFO_X, y + MachineLayout.INFO_Y);
    }

    /** Dark "digital terminal" display above the progress bar. */
    private void drawDigitalScreen(int x, int y) {
        int left = x + 12;
        int top = y + 22;
        int right = x + 188;
        int bottom = y + 58;

        drawRect(left, top, right, bottom, GuiTheme.BAR_TROUGH);
        drawRect(left, top, right, top + 1, GuiTheme.FRAME_BORDER);
        drawRect(left, bottom - 1, right, bottom, GuiTheme.FRAME_BORDER);
        drawRect(left, top, left + 1, bottom, GuiTheme.FRAME_BORDER);
        drawRect(right - 1, top, right, bottom, GuiTheme.FRAME_BORDER);

        for (int line = top + 3; line < bottom - 2; line += 4) {
            drawRect(left + 2, line, right - 2, line + 1, 0x1A6FE9E0);
        }
    }
}
