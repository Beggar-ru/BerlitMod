package ru.beggar.berlit.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

import org.lwjgl.opengl.GL11;

/** Bagazh-style complex model for the molecular transformer. */
public class ModelMolecularTransformer extends ModelBase {

    private final ModelRenderer coreBottom;
    private final ModelRenderer coreWorkZone;
    private final ModelRenderer coreTopElectr;
    private final ModelRenderer coreTopPlate;

    private final ModelRenderer topClamp1;
    private final ModelRenderer topClamp2;
    private final ModelRenderer topClamp3;
    private final ModelRenderer botClamp1;
    private final ModelRenderer botClamp2;
    private final ModelRenderer botClamp3;

    public ModelMolecularTransformer() {
        textureWidth = 128;
        textureHeight = 64;

        coreBottom = new ModelRenderer(this, 0, 0);
        coreBottom.addBox(-5.0F, 4.0F, -5.0F, 10, 3, 10);
        coreBottom.setRotationPoint(0.0F, 16.0F, 0.0F);

        coreTopPlate = new ModelRenderer(this, 0, 30);
        coreTopPlate.addBox(-5.0F, -7.0F, -4.5F, 9, 3, 9);
        coreTopPlate.setRotationPoint(0.0F, 16.0F, 0.0F);

        coreWorkZone = new ModelRenderer(this, 0, 44);
        coreWorkZone.addBox(-3.0F, -4.0F, -3.0F, 6, 9, 6);
        coreWorkZone.setRotationPoint(0.0F, 16.0F, 0.0F);

        coreTopElectr = new ModelRenderer(this, 25, 44);
        coreTopElectr.addBox(-2.0F, -8.0F, -1.466667F, 3, 2, 3);
        coreTopElectr.setRotationPoint(0.0F, 16.0F, 0.0F);

        topClamp1 = new ModelRenderer(this, 20, 16);
        topClamp1.addBox(3.0F, -8.0F, -5.0F, 4, 3, 10);
        topClamp1.setRotationPoint(0.0F, 16.0F, 0.0F);

        topClamp2 = new ModelRenderer(this, 20, 16);
        topClamp2.addBox(3.0F, -8.0F, -5.0F, 4, 3, 10);
        topClamp2.setRotationPoint(0.0F, 16.0F, 0.0F);
        topClamp2.rotateAngleY = -2.094395F;

        topClamp3 = new ModelRenderer(this, 20, 16);
        topClamp3.addBox(3.0F, -8.0F, -5.0F, 4, 3, 10);
        topClamp3.setRotationPoint(0.0F, 16.0F, 0.0F);
        topClamp3.rotateAngleY = 2.094395F;

        botClamp1 = new ModelRenderer(this, 49, 16);
        botClamp1.addBox(4.0F, 3.0F, -3.0F, 3, 5, 6);
        botClamp1.setRotationPoint(0.0F, 16.0F, 0.0F);

        botClamp2 = new ModelRenderer(this, 49, 16);
        botClamp2.addBox(4.0F, 3.0F, -3.0F, 3, 5, 6);
        botClamp2.setRotationPoint(0.0F, 16.0F, 0.0F);
        botClamp2.rotateAngleY = -2.094395F;

        botClamp3 = new ModelRenderer(this, 49, 16);
        botClamp3.addBox(4.0F, 3.0F, -3.0F, 3, 5, 6);
        botClamp3.setRotationPoint(0.0F, 16.0F, 0.0F);
        botClamp3.rotateAngleY = 2.094395F;
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        super.render(entity, f, f1, f2, f3, f4, f5);

        coreBottom.render(f5);
        botClamp1.render(f5);
        botClamp2.render(f5);
        botClamp3.render(f5);

        GL11.glPushMatrix();
        GL11.glDepthMask(false);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        coreWorkZone.render(f5);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDepthMask(true);
        GL11.glPopMatrix();

        coreTopPlate.render(f5);
        coreTopElectr.render(f5);
        topClamp1.render(f5);
        topClamp2.render(f5);
        topClamp3.render(f5);
    }
}
