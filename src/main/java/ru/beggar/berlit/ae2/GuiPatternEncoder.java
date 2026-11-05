package ru.beggar.berlit.ae2;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import ru.beggar.berlit.gui.BerlitGui;
import ru.beggar.berlit.gui.GuiTheme;
import ru.beggar.berlit.gui.ThemedButton;
import ru.beggar.berlit.network.ModNetwork;
import ru.beggar.berlit.network.PacketEncode;

public class GuiPatternEncoder extends BerlitGui {

    private static final ResourceLocation TEXTURE = new ResourceLocation("berlit:textures/gui/pattern_encoder.png");

    private static final int WIDTH = 220;
    private static final int CONTENT_HEIGHT = 196;
    private static final int INVENTORY_X = (WIDTH - 176) / 2;
    private static final int INVENTORY_TOP = CONTENT_HEIGHT + 8;

    private final TileEntityPatternEncoder tile;

    public GuiPatternEncoder(InventoryPlayer playerInventory, TileEntityPatternEncoder tile) {
        super(new ContainerPatternEncoder(playerInventory, tile), WIDTH, INVENTORY_TOP + 98);
        this.tile = tile;
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.add(new ThemedButton(0, guiLeft + 186, guiTop + 122, 24, 20, "E"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            ModNetwork.INSTANCE.sendToServer(new PacketEncode(tile.xCoord, tile.yCoord, tile.zCoord));
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRendererObj
            .drawString(StatCollector.translateToLocal("tile.berlit.pattern_encoder.name"), 8, 6, GuiTheme.TITLE);
        fontRendererObj.drawString(
            StatCollector.translateToLocal("container.inventory"),
            INVENTORY_X + 8,
            INVENTORY_TOP + 6,
            GuiTheme.TEXT);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager()
            .bindTexture(TEXTURE);

        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;
        drawPanel(x, y, 0, 0, WIDTH, CONTENT_HEIGHT);
        drawPanel(x, y, INVENTORY_X, INVENTORY_TOP, 176, 98);
    }
}
