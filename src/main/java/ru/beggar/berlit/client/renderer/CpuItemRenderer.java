package ru.beggar.berlit.client.renderer;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ru.beggar.berlit.ae2.BlockCraftingCpuBlock;

/** Draws CPU blocks as normal 3D cubes in GUI/NEI slots instead of AE2's world-only renderer. */
@SideOnly(Side.CLIENT)
public class CpuItemRenderer implements IItemRenderer {

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        return type == ItemRenderType.INVENTORY;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return true;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        if (type != ItemRenderType.INVENTORY) return;
        RenderBlocks renderer = (RenderBlocks) data[0];
        Block block = Block.getBlockFromItem(item.getItem());
        BlockCraftingCpuBlock.renderingItem = true;
        try {
            renderer.renderBlockAsItem(block, item.getItemDamage(), 1.0F);
        } finally {
            BlockCraftingCpuBlock.renderingItem = false;
        }
    }
}
