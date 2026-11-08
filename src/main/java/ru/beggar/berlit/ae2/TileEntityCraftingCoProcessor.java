package ru.beggar.berlit.ae2;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

import appeng.tile.crafting.TileCraftingTile;

/** Co-processing tile; its operation weight is supplied by the block. */
public class TileEntityCraftingCoProcessor extends TileCraftingTile {

    public TileEntityCraftingCoProcessor() {}

    private BlockCraftingCoProcessor block() {
        if (worldObj == null) return null;
        Block block = worldObj.getBlock(xCoord, yCoord, zCoord);
        return block instanceof BlockCraftingCoProcessor ? (BlockCraftingCoProcessor) block : null;
    }

    public int getOperations() {
        BlockCraftingCoProcessor block = block();
        return block == null ? 0 : block.getOperations();
    }

    @Override
    public boolean isAccelerator() {
        return true;
    }

    @Override
    protected ItemStack getItemFromTile(Object tile) {
        BlockCraftingCoProcessor block = block();
        if (block == null) return super.getItemFromTile(tile);
        return new ItemStack(block);
    }
}
