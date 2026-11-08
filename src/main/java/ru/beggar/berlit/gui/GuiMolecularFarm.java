package ru.beggar.berlit.gui;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import ru.beggar.berlit.tileentity.TileEntityMolecularFarm;

public class GuiMolecularFarm extends BerlitGui {

    private final TileEntityMolecularFarm tile;

    public GuiMolecularFarm(InventoryPlayer playerInventory, TileEntityMolecularFarm tile) {
        super(new ContainerMolecularFarm(playerInventory, tile));
        this.tile = tile;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        fontRendererObj.drawString(StatCollector.translateToLocal(tile.getInventoryName()), 8, 6, GuiTheme.TITLE);

        fontRendererObj.drawString(
            StatCollector.translateToLocalFormatted("berlit.gui.farm.seeds", tile.getSeedCount()),
            8,
            78,
            GuiTheme.TEXT);

        ContainerMolecularFarm container = (ContainerMolecularFarm) inventorySlots;
        drawRightAligned(container.getClientProgress() + "%", 186, 78, GuiTheme.ACCENT);

        fontRendererObj
            .drawString(StatCollector.translateToLocal("berlit.gui.farm.hint"), 8, 116, GuiTheme.TEXT_DISABLED);
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

        drawKitCell(x, y, ContainerMolecularFarm.INPUT_X, ContainerMolecularFarm.INPUT_Y);
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 3; col++) {
                drawKitCell(
                    x,
                    y,
                    ContainerMolecularFarm.OUTPUT_X + col * MachineLayout.SLOT,
                    ContainerMolecularFarm.OUTPUT_Y + row * MachineLayout.SLOT);
            }
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

        drawKitArrow(x + 68, y + 38);
        drawProgressTrough(x, y);
        ContainerMolecularFarm container = (ContainerMolecularFarm) inventorySlots;
        drawProgressBar(x, y, container.getClientProgress());
    }
}
