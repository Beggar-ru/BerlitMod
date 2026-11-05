package ru.beggar.berlit.gui;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

/**
 * Draws a sub-rect of the UI kit as a 9-slice: corners stay 1:1, edges repeat in one axis and the middle
 * stretches, so a 24x24 tile becomes a panel of any size without new art.
 */
public final class NineSlice {

    private final ResourceLocation texture;
    private final int textureWidth;
    private final int textureHeight;
    private final int u;
    private final int v;
    private final int size;
    private final int inset;

    public NineSlice(ResourceLocation texture, int textureWidth, int textureHeight, int u, int v, int size, int inset) {
        this.texture = texture;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.u = u;
        this.v = v;
        this.size = size;
        this.inset = inset;
    }

    public ResourceLocation getTexture() {
        return texture;
    }

    public void draw(float z, int x, int y, int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }

        int edge = Math.min(inset, Math.min(width, height));
        int sourceMiddle = size - 2 * inset;
        int middleWidth = width - 2 * edge;
        int middleHeight = height - 2 * edge;

        float uvWidth = 1.0F / textureWidth;
        float uvHeight = 1.0F / textureHeight;
        int rightSource = u + size - inset;
        int bottomSource = v + size - inset;

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();

        // top row
        quad(tessellator, z, x, y, edge, edge, u, v, inset, inset, uvWidth, uvHeight);
        quad(tessellator, z, x + edge, y, middleWidth, edge, u + inset, v, sourceMiddle, inset, uvWidth, uvHeight);
        quad(tessellator, z, x + width - edge, y, edge, edge, rightSource, v, inset, inset, uvWidth, uvHeight);

        // middle row
        quad(tessellator, z, x, y + edge, edge, middleHeight, u, v + inset, inset, sourceMiddle, uvWidth, uvHeight);
        quad(
            tessellator,
            z,
            x + edge,
            y + edge,
            middleWidth,
            middleHeight,
            u + inset,
            v + inset,
            sourceMiddle,
            sourceMiddle,
            uvWidth,
            uvHeight);
        quad(
            tessellator,
            z,
            x + width - edge,
            y + edge,
            edge,
            middleHeight,
            rightSource,
            v + inset,
            inset,
            sourceMiddle,
            uvWidth,
            uvHeight);

        // bottom row
        quad(tessellator, z, x, y + height - edge, edge, edge, u, bottomSource, inset, inset, uvWidth, uvHeight);
        quad(
            tessellator,
            z,
            x + edge,
            y + height - edge,
            middleWidth,
            edge,
            u + inset,
            bottomSource,
            sourceMiddle,
            inset,
            uvWidth,
            uvHeight);
        quad(
            tessellator,
            z,
            x + width - edge,
            y + height - edge,
            edge,
            edge,
            rightSource,
            bottomSource,
            inset,
            inset,
            uvWidth,
            uvHeight);

        tessellator.draw();
    }

    /** Draws a single kit sprite 1:1. */
    public void drawSprite(float z, int x, int y, int spriteWidth, int spriteHeight) {
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        quad(
            tessellator,
            z,
            x,
            y,
            spriteWidth,
            spriteHeight,
            u,
            v,
            spriteWidth,
            spriteHeight,
            1.0F / textureWidth,
            1.0F / textureHeight);
        tessellator.draw();
    }

    private static void quad(Tessellator tessellator, float z, int x, int y, int width, int height, int sourceX,
        int sourceY, int sourceWidth, int sourceHeight, float uvWidth, float uvHeight) {
        if (width <= 0 || height <= 0 || sourceWidth <= 0 || sourceHeight <= 0) {
            return;
        }

        float u0 = sourceX * uvWidth;
        float u1 = (sourceX + sourceWidth) * uvWidth;
        float v0 = sourceY * uvHeight;
        float v1 = (sourceY + sourceHeight) * uvHeight;

        tessellator.addVertexWithUV(x, y + height, z, u0, v1);
        tessellator.addVertexWithUV(x + width, y + height, z, u1, v1);
        tessellator.addVertexWithUV(x + width, y, z, u1, v0);
        tessellator.addVertexWithUV(x, y, z, u0, v0);
    }
}
