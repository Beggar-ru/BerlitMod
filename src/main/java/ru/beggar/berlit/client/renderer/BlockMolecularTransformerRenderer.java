package ru.beggar.berlit.client.renderer;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.world.IBlockAccess;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import ru.beggar.berlit.tileentity.TileEntityMolecularTransformer;

/** Lets the TESR model render in the block inventory item as well as in the world. */
public class BlockMolecularTransformerRenderer implements ISimpleBlockRenderingHandler {

    private final int renderId;

    public BlockMolecularTransformerRenderer(int renderId) {
        this.renderId = renderId;
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
        TileEntityRendererDispatcher.instance
            .renderTileEntityAt(new TileEntityMolecularTransformer(), 0.0F, 0.0F, 0.0F, 0.0F);
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId,
        RenderBlocks renderer) {
        return false;
    }

    @Override
    public int getRenderId() {
        return renderId;
    }

    @Override
    public boolean shouldRender3DInInventory(int modelId) {
        return true;
    }
}
