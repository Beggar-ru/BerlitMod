package ru.beggar.berlit.gui;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import ru.beggar.berlit.Berlit;
import ru.beggar.berlit.tileentity.TileEntityProcessingMachine;

public class GuiProcessingMachine extends BerlitGui {

    private final TileEntityProcessingMachine tile;

    public GuiProcessingMachine(InventoryPlayer playerInventory, TileEntityProcessingMachine tile) {
        super(new ContainerProcessingMachine(playerInventory, tile));
        this.tile = tile;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);

        fontRendererObj.drawString(
            StatCollector.translateToLocal(
                tile.getMachineType()
                    .getTitleKey()),
            8,
            6,
            GuiTheme.TITLE);

        fontRendererObj.drawString(
            StatCollector.translateToLocalFormatted("berlit.gui.energy", tile.getLastInput()),
            12,
            78,
            GuiTheme.TEXT);

        drawRightAligned(tile.getProgressScaled(100) + "%", 186, 78, GuiTheme.ACCENT);

    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        if (button != 0) return;

        int x = (width - xSize) / 2 + MachineLayout.ARROW_X;
        int y = (height - ySize) / 2 + MachineLayout.ARROW_Y;
        if (mouseX >= x && mouseX < x + 12 && mouseY >= y && mouseY < y + 12) {
            if (cpw.mods.fml.common.Loader.isModLoaded("NotEnoughItems")) {
                codechicken.nei.recipe.GuiUsageRecipe.openRecipeGui(
                    "item",
                    new net.minecraft.item.ItemStack(Berlit.machineBlocks.get(tile.getMachineType())));
            }
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager()
            .bindTexture(GuiKit.KIT);

        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;
        drawMachineFrame(x, y, false);
        drawProgressBar(x, y, tile.getProgressScaled(100));
    }
}
