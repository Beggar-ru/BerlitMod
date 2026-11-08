package ru.beggar.berlit.ae2;

/** AE2 co-processing unit with a configurable operation weight (9, 81 or 729). */
public class BlockCraftingCoProcessor extends BlockCraftingCpuBlock {

    private final int operations;

    public BlockCraftingCoProcessor(String id, int operations) {
        super(id, TileEntityCraftingCoProcessor.class);
        this.operations = operations;
    }

    public int getOperations() {
        return operations;
    }
}
