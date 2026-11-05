package ru.beggar.berlit.gui;

import net.minecraft.client.Minecraft;

/**
 * A {@link ThemedButton} that shows a floppy disk instead of a caption - used for the encoding buttons,
 * where the action is "write the grid into a pattern".
 */
public class DiskButton extends ThemedButton {

    private static final int BODY = 0xFFC8CDD8;
    private static final int EDGE = 0xFF8A92A2;
    private static final int SHUTTER = 0xFF3A4050;
    private static final int LABEL = 0xFF6E7688;

    public DiskButton(int id, int x, int y, int width, int height) {
        super(id, x, y, width, height, "");
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        super.drawButton(mc, mouseX, mouseY);
        if (!visible) {
            return;
        }

        int iconWidth = 10;
        int iconHeight = 10;
        int iconX = xPosition + (width - iconWidth) / 2;
        int iconY = yPosition + (height - iconHeight) / 2;

        drawRect(iconX, iconY, iconX + iconWidth, iconY + iconHeight, BODY);
        drawRect(iconX, iconY, iconX + iconWidth, iconY + 1, EDGE);
        drawRect(iconX, iconY + iconHeight - 1, iconX + iconWidth, iconY + iconHeight, EDGE);
        drawRect(iconX, iconY, iconX + 1, iconY + iconHeight, EDGE);
        drawRect(iconX + iconWidth - 1, iconY, iconX + iconWidth, iconY + iconHeight, EDGE);
        drawRect(iconX + 2, iconY + 1, iconX + 7, iconY + 4, SHUTTER);
        drawRect(iconX + 2, iconY + 6, iconX + iconWidth - 2, iconY + iconHeight - 2, LABEL);
    }
}
