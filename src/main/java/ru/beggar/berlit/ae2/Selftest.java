package ru.beggar.berlit.ae2;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import cpw.mods.fml.common.FMLLog;
import fox.spiteful.avaritia.crafting.CompressorManager;
import fox.spiteful.avaritia.crafting.ExtremeCraftingManager;
import ic2.api.item.IC2Items;
import ru.beggar.berlit.Berlit;
import ru.beggar.berlit.machine.TileEntityNeutronCompressor;
import ru.beggar.berlit.recipe.ReplicatorPattern;
import ru.beggar.berlit.recipe.ReplicatorRecipes;
import ru.beggar.berlit.spawner.UpgradeRegistry;
import ru.beggar.berlit.tileentity.TileEntityReplicator;
import ru.beggar.berlit.tileentity.TileEntitySpawner;
import ru.beggar.berlit.upgrade.UpgradeHelper;
import ru.beggar.berlit.util.ReplicatorFluid;

/** Server-side smoke test of the AE2/Avaritia machines, enabled with BERLIT_SELFTEST=1. */
public final class Selftest {

    private Selftest() {}

    public static void run(World world) {
        testUpgrades();
        testReplicator();
        testReplicatorMachine(world);
        testAssembler(world);
        testCompressor(world);
        FMLLog.info("BERLIT_SELFTEST optional items: infinityFluidCell=%s", Berlit.infinityFluidCell != null);
    }

    /**
     * Verifies that IC2's UU graph is available at runtime and that a scanned diamond gets a
     * sensible per-item value, a whole-stack value and that CraftTweaker overrides work.
     */
    private static void testReplicator() {
        ItemStack diamond = new ItemStack(Items.diamond);
        int matter = ReplicatorRecipes.getMatterPerItem(diamond);
        ReplicatorPattern pattern = ReplicatorRecipes.createPattern(diamond);

        ReplicatorRecipes.setMatter(diamond, 7);
        int overridden = ReplicatorRecipes.getMatterPerItem(diamond);
        ReplicatorRecipes.removeMatter(diamond);
        int restored = ReplicatorRecipes.getMatterPerItem(diamond);

        int ic2Matter = -1;
        try {
            ItemStack ic2Generator = IC2Items.getItem("generator");
            if (ic2Generator != null) {
                ic2Matter = ReplicatorRecipes.getMatterPerItem(ic2Generator);
            }
        } catch (Throwable ignored) {
            // The IC2 item name can differ between forks; the diamond value is enough then.
        }

        FMLLog.info(
            "BERLIT_SELFTEST replicator: matter=%s stack=%s perStack=%s fluid=%s override=%s restored=%s ic2=%s",
            matter,
            pattern == null ? -1 : pattern.getStackSize(),
            pattern == null ? -1 : pattern.getMatterPerStack(),
            ReplicatorFluid.get() != null,
            overridden,
            restored,
            ic2Matter);
    }

    /**
     * End-to-end check of one stack: scan a diamond, feed exactly the matter for 64 diamonds,
     * give the machine EU and tick it until the output appears. The chunk is loaded for the same
     * reason as in the assembler test - a detached tile still posts to the IC2 energy net.
     */
    private static void testReplicatorMachine(World world) {
        int x = 1000;
        int y = 64;
        int z = 1008;
        world.getChunkFromBlockCoords(x, z);

        TileEntityReplicator replicator = new TileEntityReplicator();
        replicator.setWorldObj(world);
        replicator.xCoord = x;
        replicator.yCoord = y;
        replicator.zCoord = z;

        replicator.setInventorySlotContents(TileEntityReplicator.SLOT_SCAN, new ItemStack(Items.diamond));
        replicator.fill(ForgeDirection.UP, ReplicatorFluid.createStack(2880), true);

        // The first tick scans the item and refreshes the demand; only then can energy be injected.
        replicator.updateEntity();
        replicator.injectEnergy(ForgeDirection.UP, TileEntityReplicator.ENERGY_CAPACITY, 512.0D);

        for (int i = 0; i < 4000; i++) {
            replicator.updateEntity();
        }

        FMLLog.info(
            "BERLIT_SELFTEST replicator machine: patterns=%s matter=%s tank=%s output=%s progress=%s",
            replicator.getPatternCount(),
            replicator.getMatterPerItem(),
            replicator.getFluidAmount(),
            replicator.getStackInSlot(TileEntityReplicator.SLOT_OUTPUT),
            replicator.getProgress());
    }

    /**
     * Checks the upgrade lookups, which are the kind of bug only a hand test would catch:
     * IC2 keeps every module in a single item and puts the kind on the stack, and a
     * Draconic Evolution core is registered as {@code item.draconicevolution:<core>}.
     */
    private static void testUpgrades() {
        ItemStack overclocker = IC2Items.getItem("overclockerUpgrade");
        ItemStack energyStorage = IC2Items.getItem("energyStorageUpgrade");

        UpgradeHelper.Stats overclockerStats = UpgradeHelper.get(overclocker);
        UpgradeHelper.Stats storageStats = UpgradeHelper.get(energyStorage);

        ItemStack draconicCore = new ItemStack(new Item().setUnlocalizedName("draconicevolution:draconicCore"));
        ItemStack chaoticCore = new ItemStack(new Item().setUnlocalizedName("draconicevolution:chaoticCore"));

        FMLLog.info(
            "BERLIT_SELFTEST upgrades: overclocker=%s storage=%s draconic=%s chaotic=%s",
            overclockerStats != null && overclockerStats.speed > 1.0F,
            storageStats != null && storageStats.storage > 0,
            UpgradeRegistry.get(draconicCore) != null,
            UpgradeRegistry.get(chaoticCore) != null);

        TileEntitySpawner spawner = new TileEntitySpawner();
        int first = spawner.getUpgradeSlotStart();
        ItemStack tierOne = new ItemStack(Berlit.spawnerCore1);
        ItemStack tierTwo = new ItemStack(Berlit.spawnerCore2);

        boolean acceptsFirst = spawner.canAcceptUpgrade(first, tierOne);
        spawner.setInventorySlotContents(first, tierOne);
        boolean rejectsDuplicate = spawner.canAcceptUpgrade(first + 1, tierOne);
        boolean acceptsOther = spawner.canAcceptUpgrade(first + 1, tierTwo);

        FMLLog.info(
            "BERLIT_SELFTEST spawner cores: first=%s duplicate=%s other=%s",
            acceptsFirst,
            rejectsDuplicate,
            acceptsOther);
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
        // A fresh dev world has no chunks loaded at the test rig, so both the chunk and a
        // real chest block are needed: a hand-made TileEntityChest is not picked up by
        // world.getTileEntity, which made pushPattern report a false failure.
        world.getChunkFromBlockCoords(x, z);
        world.setBlock(x, y + 1, z, Blocks.chest);
        IInventory chest = world.getTileEntity(x, y + 1, z) instanceof IInventory
            ? (IInventory) world.getTileEntity(x, y + 1, z)
            : null;

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
