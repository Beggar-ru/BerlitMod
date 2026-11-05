package ru.beggar.berlit.ae2;

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
 * Widget-based screen: nothing is baked into a per-screen texture. The panel, its cells and the buttons
 * are drawn at runtime from the 9-slice kit, so the layout lives in {@link AssemblerLayout} only.
 */
public class GuiAvaritiaAssembler extends BerlitGui {

    private final TileEntityAvaritiaAssembler tile;

    public GuiAvaritiaAssembler(InventoryPlayer playerInventory, TileEntityAvaritiaAssembler tile) {
        super(
            new ContainerAvaritiaAssembler(playerInventory, tile),
            AssemblerLayout.WIDTH,
            AssemblerLayout.INVENTORY_TOP + AssemblerLayout.INVENTORY_HEIGHT);
        this.tile = tile;
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.add(
            new NineSliceButton(
                0,
                guiLeft + AssemblerLayout.BUTTON_X,
                guiTop + AssemblerLayout.BUTTON_CRAFT_Y,
                AssemblerLayout.BUTTON_WIDTH,
                AssemblerLayout.BUTTON_HEIGHT,
                "C",
                GuiKit.BUTTON));
        buttonList.add(
            new NineSliceButton(
                1,
                guiLeft + AssemblerLayout.BUTTON_X,
                guiTop + AssemblerLayout.BUTTON_CRAFT64_Y,
                AssemblerLayout.BUTTON_WIDTH,
                AssemblerLayout.BUTTON_HEIGHT,
                "64",
                GuiKit.BUTTON));
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
            .drawString(StatCollector.translateToLocal("tile.berlit.avaritia_assembler.name"), 8, 6, GuiTheme.TITLE);
        fontRendererObj.drawString(
            StatCollector.translateToLocal("container.inventory"),
            8,
            AssemblerLayout.INVENTORY_TOP + 6,
            GuiTheme.TEXT);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager()
            .bindTexture(GuiKit.KIT);

        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;

        drawPanel(x, y, AssemblerLayout.WIDTH, AssemblerLayout.CONTENT_HEIGHT);
        drawPanel(x, y + AssemblerLayout.INVENTORY_TOP, AssemblerLayout.WIDTH, AssemblerLayout.INVENTORY_HEIGHT);

        // cyan rule under the title, like the machine panels
        drawRect(x + 8, y + 15, x + AssemblerLayout.WIDTH - 9, y + 16, 0xFF2C8C8A);
        drawRect(x + 8, y + 16, x + AssemblerLayout.WIDTH - 9, y + 17, 0xFF386868);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                drawCell(
                    x + AssemblerLayout.CRAFT_X + col * AssemblerLayout.SLOT,
                    y + AssemblerLayout.CRAFT_Y + row * AssemblerLayout.SLOT);
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawCell(
                    x + AssemblerLayout.PLAYER_X + col * AssemblerLayout.SLOT,
                    y + AssemblerLayout.PLAYER_TOP + row * AssemblerLayout.SLOT);
            }
        }

        for (int col = 0; col < 9; col++) {
            drawCell(x + AssemblerLayout.PLAYER_X + col * AssemblerLayout.SLOT, y + AssemblerLayout.HOTBAR_Y);
        }
    }

    private void drawCell(int slotX, int slotY) {
        GuiKit.CELL.drawSprite(zLevel, slotX - 1, slotY - 1, AssemblerLayout.SLOT, AssemblerLayout.SLOT);
    }

    /** 9-slice panel plus a subtle top-to-bottom darkening, matching the machine panels. */
    private void drawPanel(int x, int y, int width, int height) {
        GuiKit.PANEL.draw(zLevel, x, y, width, height);
        drawGradientRect(x + 6, y + 6, x + width - 6, y + height - 6, 0x00000000, 0x3C000000);
    }
}
