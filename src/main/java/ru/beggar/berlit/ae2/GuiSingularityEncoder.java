package ru.beggar.berlit.ae2;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import ru.beggar.berlit.network.ModNetwork;
import ru.beggar.berlit.network.PacketEncode;

public class GuiSingularityEncoder extends GuiContainer {

    private static final ResourceLocation TEXTURE = new ResourceLocation("berlit:textures/gui/singularity_encoder.png");

    private final TileEntitySingularityEncoder tile;

    public GuiSingularityEncoder(InventoryPlayer playerInventory, TileEntitySingularityEncoder tile) {
        super(new ContainerSingularityEncoder(playerInventory, tile));
        this.tile = tile;
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.add(new GuiButton(0, guiLeft + 76, guiTop + 40, 24, 20, "E"));
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
            .drawString(StatCollector.translateToLocal("tile.berlit.singularity_encoder.name"), 8, 6, 0xE8F1F5);
        fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 8, 72, 0xC8D2DE);
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
