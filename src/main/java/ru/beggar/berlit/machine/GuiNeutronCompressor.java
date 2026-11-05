package ru.beggar.berlit.machine;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import ru.beggar.berlit.gui.BerlitGui;
import ru.beggar.berlit.gui.GuiTheme;
import ru.beggar.berlit.gui.ThemedButton;
import ru.beggar.berlit.network.ModNetwork;
import ru.beggar.berlit.network.PacketCraft;

public class GuiNeutronCompressor extends BerlitGui {

    private static final ResourceLocation TEXTURE = new ResourceLocation("berlit:textures/gui/neutron_compressor.png");

    private static final int WIDTH = 176;
    private static final int CONTENT_HEIGHT = 90;
    private static final int INVENTORY_TOP = CONTENT_HEIGHT + 8;

    private final TileEntityNeutronCompressor tile;

    public GuiNeutronCompressor(InventoryPlayer playerInventory, TileEntityNeutronCompressor tile) {
        super(new ContainerNeutronCompressor(playerInventory, tile), WIDTH, INVENTORY_TOP + 98);
        this.tile = tile;
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.add(new ThemedButton(0, guiLeft + 148, guiTop + 18, 22, 20, "C"));
        buttonList.add(new ThemedButton(1, guiLeft + 148, guiTop + 40, 22, 20, "64"));
    }

    @Override
    protected void actionPerformed(net.minecraft.client.gui.GuiButton button) {
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
        fontRendererObj
            .drawString(StatCollector.translateToLocal("container.inventory"), 8, INVENTORY_TOP + 6, GuiTheme.TEXT);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager()
            .bindTexture(TEXTURE);

        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;
        drawPanel(x, y, 0, 0, WIDTH, CONTENT_HEIGHT);
        drawPanel(x, y, 0, INVENTORY_TOP, WIDTH, 98);
    }
}
