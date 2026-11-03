package ru.beggar.berlit.ae2;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import ru.beggar.berlit.network.ModNetwork;
import ru.beggar.berlit.network.PacketEncode;

public class GuiPatternEncoder extends GuiContainer {

    private static final ResourceLocation TEXTURE = new ResourceLocation("berlit:textures/gui/pattern_encoder.png");

    private final TileEntityPatternEncoder tile;

    public GuiPatternEncoder(InventoryPlayer playerInventory, TileEntityPatternEncoder tile) {
        super(new ContainerPatternEncoder(playerInventory, tile));
        this.tile = tile;
        this.xSize = 220;
        this.ySize = 282;
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.add(new GuiButton(0, guiLeft + 188, guiTop + 62, 24, 20, "E"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            ModNetwork.INSTANCE.sendToServer(new PacketEncode(tile.xCoord, tile.yCoord, tile.zCoord));
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRendererObj.drawString(StatCollector.translateToLocal("tile.berlit.pattern_encoder.name"), 8, 6, 0xE8F1F5);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager()
            .bindTexture(TEXTURE);
        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;
        drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
    }
}
