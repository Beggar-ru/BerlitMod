package ru.beggar.berlit.machine;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import ru.beggar.berlit.gui.BerlitGui;
import ru.beggar.berlit.gui.GuiKit;
import ru.beggar.berlit.gui.GuiTheme;
import ru.beggar.berlit.gui.NineSliceButton;
import ru.beggar.berlit.network.ModNetwork;
import ru.beggar.berlit.network.PacketCraft;

/**
 * Widget-based screen like the Avaritia assembler: no per-screen texture, everything comes from the
 * 9-slice kit and {@link CompressorLayout}.
 */
public class GuiNeutronCompressor extends BerlitGui {

    private final TileEntityNeutronCompressor tile;

    public GuiNeutronCompressor(InventoryPlayer playerInventory, TileEntityNeutronCompressor tile) {
        super(
            new ContainerNeutronCompressor(playerInventory, tile),
            CompressorLayout.WIDTH,
            CompressorLayout.INVENTORY_TOP + CompressorLayout.INVENTORY_HEIGHT);
        this.tile = tile;
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.add(
            new NineSliceButton(
                0,
                guiLeft + CompressorLayout.BUTTON_X,
                guiTop + CompressorLayout.BUTTON_CRAFT_Y,
                CompressorLayout.BUTTON_WIDTH,
                CompressorLayout.BUTTON_HEIGHT,
                "",
                GuiKit.BUTTON,
                "one"));
        buttonList.add(
            new NineSliceButton(
                1,
                guiLeft + CompressorLayout.BUTTON_X,
                guiTop + CompressorLayout.BUTTON_CRAFT64_Y,
                CompressorLayout.BUTTON_WIDTH,
                CompressorLayout.BUTTON_HEIGHT,
                "",
                GuiKit.BUTTON,
                "stack"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            ModNetwork.INSTANCE.sendToServer(new PacketCraft(tile.xCoord, tile.yCoord, tile.zCoord, 1));
        } else if (button.id == 1) {
            ModNetwork.INSTANCE.sendToServer(new PacketCraft(tile.xCoord, tile.yCoord, tile.zCoord, 64));
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRendererObj
            .drawString(StatCollector.translateToLocal("tile.berlit.neutron_compressor.name"), 8, 6, GuiTheme.TITLE);
        fontRendererObj.drawString(
            StatCollector.translateToLocal("container.inventory"),
            8,
            CompressorLayout.INVENTORY_TOP + 6,
            GuiTheme.TEXT);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager()
            .bindTexture(GuiKit.KIT);

        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;

        drawKitPanel(x, y, CompressorLayout.WIDTH, CompressorLayout.CONTENT_HEIGHT);
        drawKitPanel(x, y + CompressorLayout.INVENTORY_TOP, CompressorLayout.WIDTH, CompressorLayout.INVENTORY_HEIGHT);

        drawRect(x + 8, y + 15, x + CompressorLayout.WIDTH - 9, y + 16, GuiTheme.FRAME_BORDER);
        drawRect(x + 8, y + 16, x + CompressorLayout.WIDTH - 9, y + 17, GuiTheme.FRAME_RULE);

        drawCell(x, y, CompressorLayout.INPUT_X, CompressorLayout.SLOT_Y);
        drawCell(x, y, CompressorLayout.PATTERN_X, CompressorLayout.SLOT_Y);
        drawCell(x, y, CompressorLayout.OUTPUT_X, CompressorLayout.SLOT_Y);

        drawArrow(x + CompressorLayout.ARROW_X1, y + CompressorLayout.ARROW_Y - 6);
        drawArrow(x + CompressorLayout.ARROW_X2, y + CompressorLayout.ARROW_Y - 6);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawCell(
                    x,
                    y,
                    CompressorLayout.PLAYER_X + col * CompressorLayout.SLOT,
                    CompressorLayout.PLAYER_TOP + row * CompressorLayout.SLOT);
            }
        }
        for (int col = 0; col < 9; col++) {
            drawCell(x, y, CompressorLayout.PLAYER_X + col * CompressorLayout.SLOT, CompressorLayout.HOTBAR_Y);
        }
    }

    private void drawArrow(int x, int y) {
        GuiKit.ARROW.drawSprite(zLevel, x, y, 12, 12);
    }

    private void drawCell(int originX, int originY, int slotX, int slotY) {
        GuiKit.CELL
            .drawSprite(zLevel, originX + slotX - 1, originY + slotY - 1, CompressorLayout.SLOT, CompressorLayout.SLOT);
    }
}
