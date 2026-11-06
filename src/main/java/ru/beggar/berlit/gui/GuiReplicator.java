package ru.beggar.berlit.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import ru.beggar.berlit.network.ModNetwork;
import ru.beggar.berlit.network.PacketReplicatorAction;
import ru.beggar.berlit.tileentity.TileEntityReplicator;

public class GuiReplicator extends BerlitGui {

    private static final int TANK_FILL = 0xFFB36BFF;
    private static final int TANK_GLASS = 0xFF4A2C6B;

    private final TileEntityReplicator tile;

    public GuiReplicator(InventoryPlayer playerInventory, TileEntityReplicator tile) {
        super(new ContainerReplicator(playerInventory, tile));
        this.tile = tile;
    }

    @Override
    public void initGui() {
        super.initGui();

        buttonList.add(
            new ThemedButton(
                0,
                guiLeft + ReplicatorLayout.PREV_X,
                guiTop + ReplicatorLayout.BUTTON_Y,
                ReplicatorLayout.BUTTON_WIDTH,
                ReplicatorLayout.BUTTON_HEIGHT,
                "<"));
        buttonList.add(
            new ThemedButton(
                1,
                guiLeft + ReplicatorLayout.NEXT_X,
                guiTop + ReplicatorLayout.BUTTON_Y,
                ReplicatorLayout.BUTTON_WIDTH,
                ReplicatorLayout.BUTTON_HEIGHT,
                ">"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0 || button.id == 1) {
            ModNetwork.INSTANCE
                .sendToServer(new PacketReplicatorAction(tile.xCoord, tile.yCoord, tile.zCoord, button.id));
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

        boolean multiple = tile.getPatternCount() > 1;
        for (Object object : buttonList) {
            GuiButton button = (GuiButton) object;
            if (button.id == 0 || button.id == 1) {
                button.enabled = multiple;
            }
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        fontRendererObj.drawString(StatCollector.translateToLocal("tile.berlit.replicator.name"), 8, 6, GuiTheme.TITLE);

        ItemStack pattern = tile.getCurrentPattern();
        if (pattern == null) {
            drawTrimmed(StatCollector.translateToLocal("berlit.gui.replicator.no_pattern"), 76, 44, 108, GuiTheme.TEXT);
            drawTrimmed(
                StatCollector.translateToLocal("berlit.gui.replicator.scan_hint"),
                76,
                54,
                108,
                GuiTheme.TEXT_DISABLED);
        } else {
            drawTrimmed(pattern.getDisplayName(), 76, 44, 108, GuiTheme.TEXT);
            drawTrimmed(
                StatCollector.translateToLocalFormatted("berlit.gui.replicator.cost", tile.getMatterPerItem()),
                76,
                54,
                108,
                GuiTheme.TEXT_DISABLED);
        }

        fontRendererObj.drawString(
            StatCollector.translateToLocalFormatted("berlit.gui.energy", tile.getLastInput()),
            12,
            78,
            GuiTheme.TEXT);

        drawRightAligned(tile.getProgressScaled(100) + "%", 186, 78, GuiTheme.ACCENT);

        drawTrimmed(statusText(), 8, 116, 160, statusColor());

        if (tile.getPatternCount() > 1) {
            drawTrimmed(
                StatCollector.translateToLocalFormatted(
                    "berlit.gui.replicator.pattern_index",
                    tile.getPatternIndex() + 1,
                    tile.getPatternCount()),
                8,
                128,
                160,
                GuiTheme.TEXT_DISABLED);
        }

        if (pattern != null) {
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            RenderHelper.enableGUIStandardItemLighting();
            RenderItem.getInstance()
                .renderItemAndEffectIntoGUI(
                    fontRendererObj,
                    mc.getTextureManager(),
                    pattern,
                    ReplicatorLayout.GHOST_X,
                    ReplicatorLayout.GHOST_Y);
            ItemStack count = pattern.copy();
            count.stackSize = Math.max(1, tile.getStackSize());
            RenderItem.getInstance()
                .renderItemOverlayIntoGUI(
                    fontRendererObj,
                    mc.getTextureManager(),
                    count,
                    ReplicatorLayout.GHOST_X,
                    ReplicatorLayout.GHOST_Y);
            RenderHelper.disableStandardItemLighting();
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        }

        drawInfoTooltip(mouseX, mouseY, infoLines());
        drawGhostTooltip(mouseX, mouseY, pattern);
        drawTankTooltip(mouseX, mouseY);
    }

    private List<String> infoLines() {
        List<String> lines = new ArrayList<String>();
        lines.add(
            StatCollector.translateToLocalFormatted("berlit.gui.info.energy", tile.getEnergy(), tile.getMaxEnergy()));
        lines.add(
            StatCollector.translateToLocalFormatted(
                "berlit.gui.replicator.matter",
                tile.getFluidAmount(),
                tile.getFluidCapacity()));
        lines.add(
            StatCollector.translateToLocalFormatted("berlit.gui.replicator.matter_per_item", tile.getMatterPerItem()));
        lines.add(
            StatCollector.translateToLocalFormatted(
                "berlit.gui.replicator.matter_per_stack",
                tile.getMatterPerStack(),
                tile.getStackSize()));
        lines.add(StatCollector.translateToLocal("berlit.gui.replicator.info.scan"));
        lines.add(StatCollector.translateToLocal("berlit.gui.replicator.info.fluid"));
        return lines;
    }

    private void drawGhostTooltip(int mouseX, int mouseY, ItemStack pattern) {
        if (pattern == null) return;
        if (!inside(mouseX, mouseY, ReplicatorLayout.GHOST_X, ReplicatorLayout.GHOST_Y, 18, 18)) return;

        List<String> lines = new ArrayList<String>();
        lines.add(pattern.getDisplayName());
        lines.add(
            StatCollector.translateToLocalFormatted("berlit.gui.replicator.matter_per_item", tile.getMatterPerItem()));
        lines.add(
            StatCollector.translateToLocalFormatted(
                "berlit.gui.replicator.matter_per_stack",
                tile.getMatterPerStack(),
                tile.getStackSize()));
        drawHoveringText(lines, mouseX, mouseY, fontRendererObj);
    }

    private void drawTankTooltip(int mouseX, int mouseY) {
        if (!inside(
            mouseX,
            mouseY,
            ReplicatorLayout.TANK_X,
            ReplicatorLayout.TANK_Y,
            ReplicatorLayout.TANK_WIDTH,
            ReplicatorLayout.TANK_HEIGHT)) {
            return;
        }

        List<String> lines = new ArrayList<String>();
        lines.add(StatCollector.translateToLocal("berlit.gui.replicator.tank"));
        lines.add(
            StatCollector.translateToLocalFormatted(
                "berlit.gui.replicator.matter",
                tile.getFluidAmount(),
                tile.getFluidCapacity()));
        drawHoveringText(lines, mouseX, mouseY, fontRendererObj);
    }

    private static boolean inside(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private void drawTrimmed(String text, int x, int y, int width, int color) {
        fontRendererObj.drawString(fontRendererObj.trimStringToWidth(text, width), x, y, color);
    }

    private String statusText() {
        if (tile.isActive()) return StatCollector.translateToLocal("berlit.gui.replicator.status.active");

        switch (tile.getStatus()) {
            case TileEntityReplicator.STATUS_READY:
                return StatCollector.translateToLocal("berlit.gui.replicator.status.ready");
            case TileEntityReplicator.STATUS_NO_VALUE:
                return StatCollector.translateToLocal("berlit.gui.replicator.status.no_value");
            case TileEntityReplicator.STATUS_OUTPUT_FULL:
                return StatCollector.translateToLocal("berlit.gui.replicator.status.output_full");
            case TileEntityReplicator.STATUS_NO_MATTER:
                return StatCollector.translateToLocal("berlit.gui.replicator.status.no_matter");
            case TileEntityReplicator.STATUS_NO_ENERGY:
                return StatCollector.translateToLocal("berlit.gui.replicator.status.no_energy");
            case TileEntityReplicator.STATUS_PATTERN_LIMIT:
                return StatCollector.translateToLocal("berlit.gui.replicator.status.pattern_limit");
            default:
                return StatCollector.translateToLocal("berlit.gui.replicator.status.no_pattern");
        }
    }

    private int statusColor() {
        if (tile.isActive()) return GuiTheme.ACCENT;

        switch (tile.getStatus()) {
            case TileEntityReplicator.STATUS_READY:
                return GuiTheme.TEXT;
            case TileEntityReplicator.STATUS_NO_PATTERN:
                return GuiTheme.TEXT_DISABLED;
            default:
                return 0xFFFFA35C;
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        if (button != 0) return;

        // Match the processing machines: clicking the remembered item opens its NEI page.
        ItemStack pattern = tile.getCurrentPattern();
        if (pattern != null
            && inside(mouseX, mouseY, guiLeft + ReplicatorLayout.GHOST_X, guiTop + ReplicatorLayout.GHOST_Y, 18, 18)
            && cpw.mods.fml.common.Loader.isModLoaded("NotEnoughItems")) {
            codechicken.nei.recipe.GuiUsageRecipe.openRecipeGui("item", pattern);
        }
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

        drawTank(x, y);

        drawKitCell(x, y, ReplicatorLayout.SCAN_X, ReplicatorLayout.SCAN_Y);
        drawKitCell(x, y, ReplicatorLayout.OUTPUT_X, ReplicatorLayout.OUTPUT_Y);
        drawKitCell(x, y, ReplicatorLayout.FLUID_X, ReplicatorLayout.FLUID_Y);
        drawKitCell(x, y, ReplicatorLayout.EMPTY_X, ReplicatorLayout.EMPTY_Y);
        drawKitCell(x, y, ReplicatorLayout.GHOST_X, ReplicatorLayout.GHOST_Y);

        for (int i = 0; i < TileEntityReplicator.SLOT_UPGRADE_COUNT; i++) {
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

    private void drawTank(int x, int y) {
        int tx = x + ReplicatorLayout.TANK_X;
        int ty = y + ReplicatorLayout.TANK_Y;
        int width = ReplicatorLayout.TANK_WIDTH;
        int height = ReplicatorLayout.TANK_HEIGHT;

        drawRect(tx, ty, tx + width, ty + height, GuiTheme.BAR_EDGE);
        drawRect(tx + 1, ty + 1, tx + width - 1, ty + height - 1, GuiTheme.BAR_TROUGH);

        int capacity = tile.getFluidCapacity();
        int amount = Math.max(0, Math.min(tile.getFluidAmount(), capacity));
        int fill = capacity <= 0 ? 0 : (height - 2) * amount / capacity;
        if (fill > 0) {
            drawRect(tx + 1, ty + height - 1 - fill, tx + width - 1, ty + height - 1, TANK_FILL);
            drawRect(tx + 1, ty + height - 1 - fill, tx + width - 1, ty + height - fill, TANK_GLASS);
        }
    }
}
