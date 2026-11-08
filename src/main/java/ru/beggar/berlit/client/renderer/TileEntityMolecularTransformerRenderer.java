package ru.beggar.berlit.client.renderer;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;
import ru.beggar.berlit.client.model.ModelMolecularTransformer;
import ru.beggar.berlit.tileentity.TileEntityMolecularTransformer;

/** Renders the Bagazh-style complex model for the Berlit molecular transformer. */
public class TileEntityMolecularTransformerRenderer extends TileEntitySpecialRenderer {

    private static final ResourceLocation MODEL_TEXTURE = new ResourceLocation(
        "berlit",
        "textures/blocks/molecular_transformer_model.png");

    private final ModelMolecularTransformer model = new ModelMolecularTransformer();

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTick) {
        if (!(tileEntity instanceof TileEntityMolecularTransformer)) return;
        renderTransformer((TileEntityMolecularTransformer) tileEntity, x, y, z);
    }

    private void renderTransformer(TileEntityMolecularTransformer transformer, double x, double y, double z) {
        GL11.glPushMatrix();
        GL11.glTranslatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
        GL11.glPushMatrix();
        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);

        if (transformer.getWorldObj() != null) {
            int meta = transformer.getBlockMetadata();
            GL11.glRotatef(meta * 90.0F, 0.0F, 1.0F, 0.0F);
        }

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);

        FMLClientHandler.instance()
            .getClient()
            .getTextureManager()
            .bindTexture(MODEL_TEXTURE);
        model.render(null, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();
        GL11.glPopMatrix();
    }
}
