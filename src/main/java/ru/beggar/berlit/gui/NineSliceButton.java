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
            drawIcon(hovered);
            return;
        }

        drawCenteredString(
            mc.fontRenderer,
            displayString,
            xPosition + width / 2,
            yPosition + (height - 8) / 2 + 1,
            enabled ? TEXT : TEXT_DISABLED);
    }

    private void drawIcon(boolean hovered) {
        int size = 10;
        int iconX = xPosition + (width - size) / 2;
        int iconY = yPosition + (height - size) / 2;

        if ("disk".equals(icon)) {
            drawDisk(iconX, iconY, size);
        } else if ("one".equals(icon)) {
            drawCube(iconX + 1, iconY + 1, 8);
        } else if ("stack".equals(icon)) {
            drawCube(iconX + 1, iconY + 4, 6);
            drawCube(iconX + 2, iconY + 2, 6);
            drawCube(iconX + 3, iconY, 6);
        }

        if (hovered) {
            drawRect(iconX, iconY, iconX + size, iconY + 1, 0xFFFFFFFF);
        }
    }

    /** 10x10 floppy glyph, kept pixel-sized so it stays crisp. */
    private void drawDisk(int iconX, int iconY, int size) {
        int body = enabled ? 0xFFC8CDD8 : 0xFF707885;
        int edge = 0xFF8A92A2;
        int shutter = 0xFF3A4050;
        int label = 0xFF6E7688;

        drawRect(iconX, iconY, iconX + size, iconY + size, body);
        drawRect(iconX, iconY, iconX + size, iconY + 1, edge);
        drawRect(iconX, iconY + size - 1, iconX + size, iconY + size, edge);
        drawRect(iconX, iconY, iconX + 1, iconY + size, edge);
        drawRect(iconX + size - 1, iconY, iconX + size, iconY + size, edge);
        drawRect(iconX + 2, iconY + 1, iconX + 7, iconY + 4, shutter);
        drawRect(iconX + 2, iconY + 6, iconX + 7, iconY + size - 2, label);
    }

    /** Small isometric cube; several of them read as a stack. */
    private void drawCube(int x, int y, int size) {
        int face = enabled ? 0xFFC8CDD8 : 0xFF707885;
        int shade = enabled ? 0xFF8A92A2 : 0xFF5C6472;
        int edge = enabled ? 0xFF5A6272 : 0xFF474E5A;

        drawRect(x, y, x + size, y + size, face);
        drawRect(x, y, x + size, y + size / 2, shade);
        drawRect(x, y, x + size, y + 1, edge);
        drawRect(x, y + size - 1, x + size, y + size, edge);
        drawRect(x, y, x + 1, y + size, edge);
        drawRect(x + size - 1, y, x + size, y + size, edge);
    }
}
