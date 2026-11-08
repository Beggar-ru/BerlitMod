package ru.beggar.berlit.ae2;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

import appeng.tile.crafting.TileCraftingStorageTile;

/** Crafting storage tile whose capacity is supplied by {@link BlockCraftingStorageCustom}. */
public class TileEntityCraftingStorage extends TileCraftingStorageTile {

    public TileEntityCraftingStorage() {}

    private BlockCraftingStorageCustom block() {
        if (worldObj == null) return null;
        Block block = worldObj.getBlock(xCoord, yCoord, zCoord);
        return block instanceof BlockCraftingStorageCustom ? (BlockCraftingStorageCustom) block : null;
    }

    @Override
    public int getStorageBytes() {
        BlockCraftingStorageCustom block = block();
        return block == null ? 0 : block.getBytes();
    }

    @Override
    protected ItemStack getItemFromTile(Object tile) {
        BlockCraftingStorageCustom block = block();
        if (block == null) return super.getItemFromTile(tile);
        return new ItemStack(block);
    }
}
