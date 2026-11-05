package ru.beggar.berlit.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

/**
 * Flat dark button matching the Berlit palette, so the machine and AE2 screens do not show stock grey
 * widgets over the slate panel.
 */
public class ThemedButton extends GuiButton {

    private static final int FILL = 0xFF323A48;
    private static final int FILL_HOVER = 0xFF3F4A5C;
    private static final int FILL_DISABLED = 0xFF23272F;

    private static final int BORDER = 0xFF2C8C8A;
    private static final int BORDER_HOVER = 0xFF6FE9E0;
    private static final int HIGHLIGHT = 0xFF4A5568;

    private static final int TEXT = 0xFFC8D2DE;
    private static final int TEXT_DISABLED = 0xFF6B7382;

    public ThemedButton(int id, int x, int y, int width, int height, String label) {
        super(id, x, y, width, height, label);
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (!visible) {
            return;
        }

        boolean hovered = enabled && mouseX >= xPosition
            && mouseY >= yPosition
            && mouseX < xPosition + width
            && mouseY < yPosition + height;

        int fill = !enabled ? FILL_DISABLED : hovered ? FILL_HOVER : FILL;
        int border = !enabled ? BORDER : hovered ? BORDER_HOVER : BORDER;

        drawRect(xPosition, yPosition, xPosition + width, yPosition + height, fill);
        drawRect(xPosition, yPosition, xPosition + width, yPosition + 1, border);
        drawRect(xPosition, yPosition + height - 1, xPosition + width, yPosition + height, border);
        drawRect(xPosition, yPosition, xPosition + 1, yPosition + height, border);
        drawRect(xPosition + width - 1, yPosition, xPosition + width, yPosition + height, border);
        drawRect(xPosition + 1, yPosition + 1, xPosition + width - 1, yPosition + 2, HIGHLIGHT);

        drawCenteredString(
            mc.fontRenderer,
            displayString,
            xPosition + width / 2,
            yPosition + (height - 8) / 2 + 1,
            enabled ? TEXT : TEXT_DISABLED);
    }
}
