package ru.beggar.berlit.ae2;

/** Large AE2 crafting storage block. It forms a normal AE2 crafting CPU multiblock. */
public class BlockCraftingStorageCustom extends BlockCraftingCpuBlock {

    private final int bytes;

    public BlockCraftingStorageCustom(String id, int bytes) {
        super(id, TileEntityCraftingStorage.class);
        this.bytes = bytes;
    }

    public int getBytes() {
        return bytes;
    }
}
