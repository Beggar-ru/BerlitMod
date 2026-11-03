package ru.beggar.berlit.ae2;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.World;

import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import cpw.mods.fml.common.FMLLog;
import fox.spiteful.avaritia.crafting.CompressorManager;
import fox.spiteful.avaritia.crafting.ExtremeCraftingManager;
import ru.beggar.berlit.machine.TileEntityNeutronCompressor;

/** Server-side smoke test of the AE2/Avaritia machines, enabled with BERLIT_SELFTEST=1. */
public final class Selftest {

    private Selftest() {}

    public static void run(World world) {
        testAssembler(world);
        testCompressor(world);
    }

    private static void testAssembler(World world) {
        int x = 1000;
        int y = 64;
        int z = 1000;

        ExtremeCraftingManager.getInstance()
            .addRecipe(
                new ItemStack(Items.diamond),
                "IIIIIIIII",
                "IIIIIIIII",
                "IIIIIIIII",
                "IIIIIIIII",
                "IIIIIIIII",
                "IIIIIIIII",
                "IIIIIIIII",
                "IIIIIIIII",
                "IIIIIIIII",
                'I',
                Items.iron_ingot);

        ItemStack[] grid = new ItemStack[81];
        for (int i = 0; i < grid.length; i++) grid[i] = new ItemStack(Items.iron_ingot);

        ItemStack patternItem = ItemAvaritiaPattern.create(grid, new ItemStack(Items.diamond));
        ICraftingPatternDetails details = ((ICraftingPatternItem) patternItem.getItem())
            .getPatternForItem(patternItem, world);

        net.minecraft.inventory.InventoryCrafting direct = new net.minecraft.inventory.InventoryCrafting(
            new appeng.container.ContainerNull(),
            9,
            9);
        for (int i = 0; i < 81; i++) direct.setInventorySlotContents(i, new ItemStack(Items.iron_ingot));
        FMLLog.info(
            "BERLIT_SELFTEST direct recipe: %s",
            ExtremeCraftingManager.getInstance()
                .findMatchingRecipe(direct, world));
        TileEntityChest chest = new TileEntityChest();
        chest.setWorldObj(world);
        chest.xCoord = x;
        chest.yCoord = y + 1;
        chest.zCoord = z;
        world.setTileEntity(x, y + 1, z, chest);

        TileEntityAvaritiaAssembler assembler = new TileEntityAvaritiaAssembler();
        assembler.setWorldObj(world);
        assembler.xCoord = x;
        assembler.yCoord = y;
        assembler.zCoord = z;

        boolean pushed = assembler.pushPattern(details, null);
        ItemStack result = chest == null ? null : chest.getStackInSlot(0);

        FMLLog.info(
            "BERLIT_SELFTEST assembler: pushed=%s result=%s inputs=%s condensed=%s",
            pushed,
            result,
            details.getInputs().length,
            details.getCondensedInputs().length);
    }

    private static void testCompressor(World world) {
        ItemStack input = new ItemStack(Blocks.iron_block);
        ItemStack singularity = CompressorManager.getOutput(input);
        int cost = CompressorManager.getCost(input);
        if (singularity == null || cost <= 0) {
            FMLLog.info("BERLIT_SELFTEST compressor: no iron recipe (cost=%s)", cost);
            return;
        }

        TileEntityNeutronCompressor compressor = new TileEntityNeutronCompressor();
        compressor.setWorldObj(world);
        compressor.xCoord = 1000;
        compressor.yCoord = 64;
        compressor.zCoord = 1004;

        ItemStack stack = new ItemStack(Blocks.iron_block, cost);
        compressor.setInventorySlotContents(TileEntityNeutronCompressor.INPUT, stack);
        compressor.setInventorySlotContents(
            TileEntityNeutronCompressor.PATTERN,
            ItemSingularityPattern.create(new ItemStack(Blocks.iron_block), cost, singularity));

        for (int i = 0; i < cost + 1; i++) {
            compressor.updateEntity();
        }

        FMLLog.info(
            "BERLIT_SELFTEST compressor: cost=%s out=%s inputLeft=%s",
            cost,
            compressor.getStackInSlot(TileEntityNeutronCompressor.OUTPUT),
            compressor.getStackInSlot(TileEntityNeutronCompressor.INPUT));
    }
}
