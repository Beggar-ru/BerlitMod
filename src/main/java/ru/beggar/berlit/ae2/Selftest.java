package ru.beggar.berlit.ae2;

import java.util.List;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.crafting.ICraftingMedium;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.storage.data.IAEItemStack;
import cpw.mods.fml.common.FMLLog;
import fox.spiteful.avaritia.crafting.CompressorManager;
import fox.spiteful.avaritia.crafting.ExtremeCraftingManager;
import ic2.api.item.IC2Items;
import ru.beggar.berlit.Berlit;
import ru.beggar.berlit.machine.MachineRecipe;
import ru.beggar.berlit.machine.MachineRecipes;
import ru.beggar.berlit.machine.MachineType;
import ru.beggar.berlit.machine.TileEntityNeutronCompressor;
import ru.beggar.berlit.recipe.ReplicatorPattern;
import ru.beggar.berlit.recipe.ReplicatorRecipes;
import ru.beggar.berlit.spawner.UpgradeRegistry;
import ru.beggar.berlit.tileentity.TileEntityCobbleGenerator;
import ru.beggar.berlit.tileentity.TileEntityMolecularFarm;
import ru.beggar.berlit.tileentity.TileEntityProcessingMachine;
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
        testDigitalMachines();
        testProcessingPersistence();
        testMolecularFarm(world);
        testCobbleGenerator(world);
        testSpawnerBuffer(world);
        testCraftingCpu(world);
        testCpuMultiblock(world);
        testInterfaceExpansion();
        testBigInterfaces();
        testBigInterfaceCraftingList(world);
        testDigitalFilter(world);
        testDigitalProcessing(world);
        testDigitalCraftingMachines(world);
        testAssembler(world);
        testCompressor(world);
        FMLLog.info("BERLIT_SELFTEST optional items: infinityFluidCell=%s", Berlit.infinityFluidCell != null);
    }

    /** Every digital machine must publish its whole recipe table to the AE2 crafting grid. */
    private static void testDigitalMachines() {
        int total = 0;
        for (MachineType type : MachineType.values()) {
            TileEntityDigitalMachine machine = new TileEntityDigitalMachine(type);
            final java.util.List<ICraftingPatternDetails> patterns = new java.util.ArrayList<ICraftingPatternDetails>();

            machine.provideCrafting(new ICraftingProviderHelper() {

                @Override
                public void addCraftingOption(ICraftingMedium medium, ICraftingPatternDetails details) {
                    patterns.add(details);
                }

                @Override
                public void setEmitable(IAEItemStack stack) {}
            });

            total += patterns.size();
            FMLLog.info(
                "BERLIT_SELFTEST digital %s: patterns=%s recipes=%s",
                type.getId(),
                patterns.size(),
                machine.getRecipeCount());
        }
        FMLLog.info("BERLIT_SELFTEST digital total: %s", total);

        boolean porkWildcardFixed = false;
        for (MachineRecipe recipe : MachineRecipes
            .getRecipes(MachineType.ELECTRIC_FURNACE, MachineType.ELECTRIC_FURNACE.getDefaultMode())) {
            ItemStack input = recipe.getInput();
            ItemStack output = recipe.getOutput();
            if (input != null && output != null
                && "item.porkchopRaw".equals(input.getUnlocalizedName())
                && input.getItemDamage() == 0
                && "item.porkchopCooked".equals(output.getUnlocalizedName())) {
                porkWildcardFixed = true;
                break;
            }
        }
        FMLLog.info("BERLIT_SELFTEST pork: damage0=%s", porkWildcardFixed);
    }

    /** Verifies that the shared processing-machine tile keeps its machine type across NBT. */
    private static void testProcessingPersistence() {
        TileEntityProcessingMachine saved = new TileEntityProcessingMachine(MachineType.METAL_ROLLER);
        NBTTagCompound nbt = new NBTTagCompound();
        saved.writeToNBT(nbt);

        TileEntityProcessingMachine loaded = new TileEntityProcessingMachine();
        loaded.readFromNBT(nbt);

        FMLLog.info(
            "BERLIT_SELFTEST processing: saved=%s loaded=%s",
            saved.getMachineType()
                .getId(),
            loaded.getMachineType()
                .getId());
    }

    /** Verifies the farm keeps seeds and scales the harvest with the seed stack. */
    private static void testMolecularFarm(World world) {
        TileEntityMolecularFarm farm = new TileEntityMolecularFarm();
        farm.setWorldObj(world);
        farm.setInventorySlotContents(0, new ItemStack(Items.wheat_seeds, 64));
        for (int i = 0; i < TileEntityMolecularFarm.TICKS_PER_HARVEST; i++) {
            farm.updateEntity();
        }

        ItemStack seeds = farm.getStackInSlot(0);
        ItemStack wheat = farm.getStackInSlot(1);
        ItemStack extraSeeds = farm.getStackInSlot(2);
        FMLLog.info(
            "BERLIT_SELFTEST farm: seeds=%s wheat=%s extraSeeds=%s",
            seeds == null ? null : seeds.stackSize,
            wheat == null ? null : wheat.stackSize,
            extraSeeds == null ? null : extraSeeds.stackSize);
    }

    /** Verifies the single cobble generator tier fills all 27 slots with 64 cobblestone. */
    private static void testCobbleGenerator(World world) {
        TileEntityCobbleGenerator generator = new TileEntityCobbleGenerator();
        generator.setWorldObj(world);
        generator.updateEntity();

        int full = 0;
        for (int i = 0; i < TileEntityCobbleGenerator.SLOT_COUNT; i++) {
            ItemStack stack = generator.getStackInSlot(i);
            if (stack != null && stack.getItem() == Item.getItemFromBlock(Blocks.cobblestone)
                && stack.stackSize == 64) {
                full++;
            }
        }
        FMLLog.info("BERLIT_SELFTEST cobble: fullSlots=%s", full);
    }

    /** Verifies the spawner buffer gets drops even when there is no adjacent inventory. */
    private static void testSpawnerBuffer(World world) {
        java.util.Iterator<ru.beggar.berlit.spawner.EntityDefinition> iterator = ru.beggar.berlit.spawner.EntityRegistry
            .all()
            .iterator();
        if (!iterator.hasNext()) {
            FMLLog.info("BERLIT_SELFTEST spawner buffer: no entity definitions");
            return;
        }

        ru.beggar.berlit.spawner.EntityDefinition definition = iterator.next();
        TileEntitySpawner spawner = new TileEntitySpawner();
        spawner.setWorldObj(world);
        spawner.xCoord = 2008;
        spawner.yCoord = 64;
        spawner.zCoord = 2008;
        spawner.setInventorySlotContents(0, ru.beggar.berlit.item.ItemEntityCard.create(definition.getId()));
        for (int i = 0; i < 400; i++) spawner.updateEntity();

        FMLLog.info(
            "BERLIT_SELFTEST spawner buffer: entity=%s buffered=%s stacks=%s",
            definition.getId(),
            spawner.getBufferedItemCount(),
            spawner.getBufferedStackCount());
    }

    /** Verifies custom CPU storage/co-processor blocks expose their configured capacities. */
    private static void testCraftingCpu(World world) {
        int x = 2020;
        int y = 64;
        int z = 2020;
        world.getChunkFromBlockCoords(x, z);
        world.setBlock(x, y, z, Berlit.craftingStorages[8]);
        TileEntity storage = world.getTileEntity(x, y, z);

        world.setBlock(x + 2, y, z, Berlit.craftingProcessors[2]);
        TileEntity processor = world.getTileEntity(x + 2, y, z);

        int coProcessors = -1;
        if (processor instanceof ru.beggar.berlit.ae2.TileEntityCraftingCoProcessor) {
            try {
                appeng.me.cluster.implementations.CraftingCPUCluster cluster = new appeng.me.cluster.implementations.CraftingCPUCluster(
                    new appeng.api.util.WorldCoord(x + 2, y, z),
                    new appeng.api.util.WorldCoord(x + 3, y + 1, z + 1));
                java.lang.reflect.Method addTile = appeng.me.cluster.implementations.CraftingCPUCluster.class
                    .getDeclaredMethod("addTile", appeng.tile.crafting.TileCraftingTile.class);
                addTile.setAccessible(true);
                addTile.invoke(cluster, processor);
                coProcessors = cluster.getCoProcessors();
            } catch (Throwable ignored) {
                coProcessors = -2;
            }
        }

        FMLLog.info(
            "BERLIT_SELFTEST crafting cpu: storage=%s processor=%s coprocessors=%s",
            storage instanceof ru.beggar.berlit.ae2.TileEntityCraftingStorage
                ? ((ru.beggar.berlit.ae2.TileEntityCraftingStorage) storage).getStorageBytes()
                : null,
            processor instanceof ru.beggar.berlit.ae2.TileEntityCraftingCoProcessor
                ? ((ru.beggar.berlit.ae2.TileEntityCraftingCoProcessor) processor).getOperations()
                : null,
            coProcessors);
    }

    /** Verifies that the standard AE2 interface can be expanded to a larger pattern inventory. */
    private static void testInterfaceExpansion() {
        appeng.tile.misc.TileInterface tile = new appeng.tile.misc.TileInterface();
        int before = tile.getInterfaceDuality()
            .getPatterns()
            .getSizeInventory();
        boolean expanded = InterfacePatternExpander.expand(tile, 18);
        int after = tile.getInterfaceDuality()
            .getPatterns()
            .getSizeInventory();
        FMLLog.info("BERLIT_SELFTEST interface expand: before=%s expanded=%s after=%s", before, expanded, after);
    }

    /** Builds a CPU from a standard core, our 512M storage and our 729x co-processor. */
    private static void testCpuMultiblock(World world) {
        int x = 2040;
        int y = 64;
        int z = 2040;
        world.getChunkFromBlockCoords(x, z);

        net.minecraft.item.ItemStack coreStack = appeng.api.AEApi.instance()
            .definitions()
            .blocks()
            .craftingUnit()
            .maybeStack(1)
            .orNull();
        if (coreStack == null) {
            FMLLog.info("BERLIT_SELFTEST cpu multiblock: no AE2 crafting unit");
            return;
        }

        net.minecraft.block.Block coreBlock = net.minecraft.block.Block.getBlockFromItem(coreStack.getItem());
        world.setBlock(x, y, z, coreBlock);
        world.setBlock(x + 1, y, z, Berlit.craftingStorages[8]);
        world.setBlock(x + 2, y, z, Berlit.craftingProcessors[2]);

        appeng.tile.crafting.TileCraftingTile core = (appeng.tile.crafting.TileCraftingTile) world
            .getTileEntity(x, y, z);
        appeng.tile.crafting.TileCraftingTile storage = (appeng.tile.crafting.TileCraftingTile) world
            .getTileEntity(x + 1, y, z);
        appeng.tile.crafting.TileCraftingTile processor = (appeng.tile.crafting.TileCraftingTile) world
            .getTileEntity(x + 2, y, z);
        if (core == null || storage == null || processor == null) {
            FMLLog.info("BERLIT_SELFTEST cpu multiblock: missing tiles");
            return;
        }

        core.updateMultiBlock();
        storage.updateMultiBlock();
        processor.updateMultiBlock();

        appeng.me.cluster.implementations.CraftingCPUCluster cluster = (appeng.me.cluster.implementations.CraftingCPUCluster) core
            .getCluster();
        FMLLog.info(
            "BERLIT_SELFTEST cpu multiblock: cluster=%s formed=%s storage=%s coprocessors=%s",
            cluster != null,
            cluster != null && core.isFormed(),
            cluster == null ? -1 : cluster.getAvailableStorage(),
            cluster == null ? -1 : cluster.getCoProcessors());
        if (cluster != null) {
            cluster.destroy();
        }

        int x2 = x + 5;
        world.setBlock(x2, y, z, Berlit.craftingStorages[8]);
        world.setBlock(x2 + 1, y, z, Berlit.craftingProcessors[2]);
        net.minecraft.item.ItemStack cellStack = appeng.api.AEApi.instance()
            .definitions()
            .blocks()
            .energyCellCreative()
            .maybeStack(1)
            .orNull();
        if (cellStack != null) {
            net.minecraft.block.Block cellBlock = net.minecraft.block.Block.getBlockFromItem(cellStack.getItem());
            world.setBlock(x2 + 2, y, z, cellBlock);
        }
        appeng.tile.crafting.TileCraftingTile storageOnly = (appeng.tile.crafting.TileCraftingTile) world
            .getTileEntity(x2, y, z);
        appeng.tile.crafting.TileCraftingTile processorOnly = (appeng.tile.crafting.TileCraftingTile) world
            .getTileEntity(x2 + 1, y, z);
        if (storageOnly != null && processorOnly != null) {
            storageOnly.updateMultiBlock();
            processorOnly.updateMultiBlock();
            appeng.me.cluster.implementations.CraftingCPUCluster noCoreCluster = (appeng.me.cluster.implementations.CraftingCPUCluster) storageOnly
                .getCluster();
            FMLLog.info(
                "BERLIT_SELFTEST cpu multiblock no-core: cluster=%s active=%s storage=%s coprocessors=%s",
                noCoreCluster != null,
                noCoreCluster != null && noCoreCluster.isActive(),
                noCoreCluster == null ? -1 : noCoreCluster.getAvailableStorage(),
                noCoreCluster == null ? -1 : noCoreCluster.getCoProcessors());
            if (noCoreCluster != null) {
                noCoreCluster.destroy();
            }
        }
    }

    /** Verifies that all 18 pattern slots reach DualityInterface's crafting list. */
    private static void testBigInterfaceCraftingList(World world) {
        ru.beggar.berlit.ae2.iface.TileBigInterface18 tile = new ru.beggar.berlit.ae2.iface.TileBigInterface18();
        tile.setWorldObj(world);
        tile.xCoord = 2060;
        tile.yCoord = 64;
        tile.zCoord = 2060;

        ItemStack[] grid = new ItemStack[81];
        grid[0] = new ItemStack(net.minecraft.init.Items.iron_ingot);
        ItemStack pattern = ru.beggar.berlit.ae2.ItemAvaritiaPattern
            .create(grid, new ItemStack(net.minecraft.init.Items.diamond));
        for (int slot = 0; slot < 18; slot++) {
            tile.getInterfaceDuality()
                .getPatterns()
                .setInventorySlotContents(slot, pattern.copy());
        }

        int listed = -1;
        try {
            java.lang.reflect.Field proxy = appeng.helpers.DualityInterface.class.getDeclaredField("gridProxy");
            proxy.setAccessible(true);
            Object dualityProxy = proxy.get(tile.getInterfaceDuality());
            java.lang.reflect.Field ready = appeng.me.helpers.AENetworkProxy.class.getDeclaredField("isReady");
            ready.setAccessible(true);
            ready.set(dualityProxy, true);

            java.lang.reflect.Method update = appeng.helpers.DualityInterface.class
                .getDeclaredMethod("updateCraftingList");
            update.setAccessible(true);
            update.invoke(tile.getInterfaceDuality());

            java.lang.reflect.Field list = appeng.helpers.DualityInterface.class.getDeclaredField("craftingList");
            list.setAccessible(true);
            Object value = list.get(tile.getInterfaceDuality());
            listed = value instanceof java.util.List ? ((java.util.List<?>) value).size() : 0;
        } catch (Throwable ignored) {
            listed = -2;
        }
        FMLLog.info("BERLIT_SELFTEST big interface crafting list: patterns=%s", listed);
    }

    /** Verifies the three block and three cable-part interface sizes. */
    private static void testBigInterfaces() {
        ru.beggar.berlit.ae2.iface.TileBigInterface18 tile18 = new ru.beggar.berlit.ae2.iface.TileBigInterface18();
        ru.beggar.berlit.ae2.iface.TileBigInterface27 tile27 = new ru.beggar.berlit.ae2.iface.TileBigInterface27();
        ru.beggar.berlit.ae2.iface.TileBigInterface36 tile36 = new ru.beggar.berlit.ae2.iface.TileBigInterface36();

        net.minecraft.item.ItemStack partStack = new net.minecraft.item.ItemStack(Berlit.bigInterfacePart);
        ru.beggar.berlit.ae2.iface.PartBigInterface18 part18 = new ru.beggar.berlit.ae2.iface.PartBigInterface18(
            partStack);
        ru.beggar.berlit.ae2.iface.PartBigInterface27 part27 = new ru.beggar.berlit.ae2.iface.PartBigInterface27(
            partStack);
        ru.beggar.berlit.ae2.iface.PartBigInterface36 part36 = new ru.beggar.berlit.ae2.iface.PartBigInterface36(
            partStack);

        FMLLog.info(
            "BERLIT_SELFTEST big interfaces: blocks=%s/%s/%s parts=%s/%s/%s",
            tile18.getInterfaceDuality()
                .getPatterns()
                .getSizeInventory(),
            tile27.getInterfaceDuality()
                .getPatterns()
                .getSizeInventory(),
            tile36.getInterfaceDuality()
                .getPatterns()
                .getSizeInventory(),
            part18.getInterfaceDuality()
                .getPatterns()
                .getSizeInventory(),
            part27.getInterfaceDuality()
                .getPatterns()
                .getSizeInventory(),
            part36.getInterfaceDuality()
                .getPatterns()
                .getSizeInventory());

        net.minecraft.entity.player.InventoryPlayer inventory = new net.minecraft.entity.player.InventoryPlayer(null);
        ru.beggar.berlit.ae2.iface.ContainerBigInterface18 container18 = new ru.beggar.berlit.ae2.iface.ContainerBigInterface18(
            inventory,
            tile18);
        ru.beggar.berlit.ae2.iface.ContainerBigInterface27 container27 = new ru.beggar.berlit.ae2.iface.ContainerBigInterface27(
            inventory,
            tile27);
        ru.beggar.berlit.ae2.iface.ContainerBigInterface36 container36 = new ru.beggar.berlit.ae2.iface.ContainerBigInterface36(
            inventory,
            tile36);
        FMLLog.info(
            "BERLIT_SELFTEST big interface containers: slots=%s/%s/%s height=%s/%s/%s",
            container18.inventorySlots.size(),
            container27.inventorySlots.size(),
            container36.inventorySlots.size(),
            container18.getGuiHeight(),
            container27.getGuiHeight(),
            container36.getGuiHeight());

        boolean settingsLoaded = true;
        try {
            java.lang.reflect.Method loadSettings = appeng.container.implementations.ContainerUpgradeable.class
                .getDeclaredMethod("loadSettingsFromHost", appeng.api.util.IConfigManager.class);
            loadSettings.setAccessible(true);
            loadSettings.invoke(
                container18,
                tile18.getInterfaceDuality()
                    .getConfigManager());
        } catch (Throwable ignored) {
            settingsLoaded = false;
        }
        FMLLog.info("BERLIT_SELFTEST big interface settings: loaded=%s", settingsLoaded);
    }

    /** Verifies that the persistent filter really removes one exported pattern. */
    private static void testDigitalFilter(World world) {
        MachineType type = MachineType.MACERATOR;
        List<DigitalRecipeFilter.Entry> entries = DigitalRecipeFilter.getEntries(type, type.getDefaultMode());
        if (entries.isEmpty()) {
            FMLLog.info("BERLIT_SELFTEST digital filter: no entries");
            return;
        }

        DigitalRecipeFilter.Entry first = entries.get(0);
        DigitalRecipeFilter.setEnabled(world, type, first.getKey(), false);
        boolean disabled = !DigitalRecipeFilter.isEnabled(world, type, first);

        TileEntityDigitalMachine machine = new TileEntityDigitalMachine(type);
        machine.setWorldObj(world);
        final int[] count = { 0 };
        machine.provideCrafting(new ICraftingProviderHelper() {

            @Override
            public void addCraftingOption(ICraftingMedium medium, ICraftingPatternDetails details) {
                count[0]++;
            }

            @Override
            public void setEmitable(IAEItemStack stack) {}
        });
        int disabledCount = count[0];

        DigitalRecipeFilter.setEnabled(world, type, first.getKey(), true);
        boolean restored = DigitalRecipeFilter.isEnabled(world, type, first);

        FMLLog.info(
            "BERLIT_SELFTEST digital filter: entries=%s disabled=%s remaining=%s restored=%s",
            entries.size(),
            disabled,
            disabledCount,
            restored);
    }

    /** Verifies that a generated digital pattern is processed and its output is queued. */
    private static void testDigitalProcessing(World world) {
        DigitalPatternDetails sample = null;
        for (MachineRecipe recipe : MachineRecipes
            .getRecipes(MachineType.MACERATOR, MachineType.MACERATOR.getDefaultMode())) {
            for (ItemStack input : recipe.getInputs()) {
                if (input != null && input.getItem() == Item.getItemFromBlock(Blocks.iron_ore)) {
                    sample = new DigitalPatternDetails(
                        MachineType.MACERATOR,
                        input,
                        recipe.getOutputs(),
                        recipe.getEnergy());
                    break;
                }
            }
            if (sample != null) break;
        }

        if (sample == null) {
            FMLLog.info("BERLIT_SELFTEST digital processing: no iron ore macerator recipe");
            return;
        }

        int x = 1000;
        int y = 64;
        int z = 1012;
        world.getChunkFromBlockCoords(x, z);

        TileEntityDigitalMachine machine = new TileEntityDigitalMachine(MachineType.MACERATOR);
        machine.setWorldObj(world);
        machine.xCoord = x;
        machine.yCoord = y;
        machine.zCoord = z;
        machine.queuePatternForTest(sample);

        // The first tick initialises the job; inject energy after that.
        machine.updateEntity();
        machine.injectEnergy(ForgeDirection.UP, TileEntityDigitalMachine.ENERGY_CAPACITY, 512.0D);
        for (int i = 0; i < 100; i++) {
            machine.updateEntity();
        }

        List<ItemStack> pending = machine.getPendingOutputForTest();
        ItemStack patternItem = sample.getPattern();
        boolean reloaded = patternItem != null && patternItem.getItem() instanceof ICraftingPatternItem
            && ((ICraftingPatternItem) patternItem.getItem()).getPatternForItem(patternItem, world) != null;
        FMLLog.info(
            "BERLIT_SELFTEST digital processing: pending=%s output=%s reloaded=%s",
            pending.size(),
            pending.isEmpty() ? null : pending.get(0),
            reloaded);
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
            "BERLIT_SELFTEST spawner cores: first=%s duplicate=%s other=%s stack=%s",
            acceptsFirst,
            rejectsDuplicate,
            acceptsOther,
            new ItemStack(Berlit.spawnerCore1).getMaxStackSize());
    }

    /** Verifies that the custom digital machines export and process their automatic patterns. */
    private static void testDigitalCraftingMachines(World world) {
        TileEntityDigitalAvaritiaAssembler avaritia = new TileEntityDigitalAvaritiaAssembler();
        avaritia.setWorldObj(world);
        avaritia.xCoord = 2000;
        avaritia.yCoord = 64;
        avaritia.zCoord = 2000;
        java.util.List<ICraftingPatternDetails> avaritiaPatterns = avaritia.getPatternsForTest();
        boolean avaritiaProcessed = false;
        if (!avaritiaPatterns.isEmpty()) {
            avaritia.queuePatternForTest(avaritiaPatterns.get(0));
            avaritia.injectEnergy(ForgeDirection.UP, TileEntityDigitalCrafting.ENERGY_CAPACITY, 512.0D);
            for (int i = 0; i < 150; i++) avaritia.updateEntity();
            avaritiaProcessed = !avaritia.getPendingOutputForTest()
                .isEmpty();
        }

        TileEntityDigitalNeutronCompressor compressor = new TileEntityDigitalNeutronCompressor();
        compressor.setWorldObj(world);
        compressor.xCoord = 2004;
        compressor.yCoord = 64;
        compressor.zCoord = 2004;
        java.util.List<ICraftingPatternDetails> compressorPatterns = compressor.getPatternsForTest();
        boolean compressorProcessed = false;
        if (!compressorPatterns.isEmpty()) {
            compressor.queuePatternForTest(compressorPatterns.get(0));
            compressor.injectEnergy(ForgeDirection.UP, TileEntityDigitalCrafting.ENERGY_CAPACITY, 512.0D);
            for (int i = 0; i < 150; i++) compressor.updateEntity();
            compressorProcessed = !compressor.getPendingOutputForTest()
                .isEmpty();
        }

        FMLLog.info(
            "BERLIT_SELFTEST digital custom: avaritiaPatterns=%s avaritiaProcessed=%s compressorPatterns=%s compressorProcessed=%s",
            avaritiaPatterns.size(),
            avaritiaProcessed,
            compressorPatterns.size(),
            compressorProcessed);
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
