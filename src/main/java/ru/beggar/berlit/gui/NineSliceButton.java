package ru.beggar.berlit.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

import org.lwjgl.opengl.GL11;

/** Button drawn from the 9-slice kit: one stretched tile, a hover veil and a centred caption. */
public class NineSliceButton extends GuiButton {

    private static final int TEXT = 0xFFC8D2DE;
    private static final int TEXT_DISABLED = 0xFF6B7382;
    private static final int HOVER_VEIL = 0x22FFFFFF;

    private final NineSlice slice;
    private final String icon;

    public NineSliceButton(int id, int x, int y, int width, int height, String label, NineSlice slice) {
        this(id, x, y, width, height, label, slice, null);
    }

    public NineSliceButton(int id, int x, int y, int width, int height, String label, NineSlice slice, String icon) {
        super(id, x, y, width, height, label);
        this.slice = slice;
        this.icon = icon;
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

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager()
            .bindTexture(slice.getTexture());
        slice.draw(this.zLevel, xPosition, yPosition, width, height);

        if (hovered) {
            drawRect(xPosition + 1, yPosition + 1, xPosition + width - 1, yPosition + height - 1, HOVER_VEIL);
        }

        if (icon != null) {
            drawDisk(mc, hovered);
            return;
        }

        drawCenteredString(
            mc.fontRenderer,
            displayString,
            xPosition + width / 2,
            yPosition + (height - 8) / 2 + 1,
            enabled ? TEXT : TEXT_DISABLED);
    }

    /** 10x10 floppy glyph, kept pixel-sized so it stays crisp. */
    private void drawDisk(Minecraft mc, boolean hovered) {
        int iconSize = 10;
        int iconX = xPosition + (width - iconSize) / 2;
        int iconY = yPosition + (height - iconSize) / 2;

        int body = enabled ? 0xFFC8CDD8 : 0xFF707885;
        int edge = 0xFF8A92A2;
        int shutter = 0xFF3A4050;
        int label = 0xFF6E7688;

        drawRect(iconX, iconY, iconX + iconSize, iconY + iconSize, body);
        drawRect(iconX, iconY, iconX + iconSize, iconY + 1, edge);
        drawRect(iconX, iconY + iconSize - 1, iconX + iconSize, iconY + iconSize, edge);
        drawRect(iconX, iconY, iconX + 1, iconY + iconSize, edge);
        drawRect(iconX + iconSize - 1, iconY, iconX + iconSize, iconY + iconSize, edge);
        drawRect(iconX + 2, iconY + 1, iconX + 7, iconY + 4, shutter);
        drawRect(iconX + 2, iconY + 6, iconX + iconSize - 2, iconY + iconSize - 2, label);

        if (hovered) {
            drawRect(iconX, iconY, iconX + iconSize, iconY + 1, 0xFFFFFFFF);
        }
    }
}
