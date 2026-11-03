package ru.beggar.berlit;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import ru.beggar.berlit.block.BlockMolecularTransformer;
import ru.beggar.berlit.block.BlockProcessingMachine;
import ru.beggar.berlit.block.BlockSpawner;
import ru.beggar.berlit.block.BlockSpawnerT2;
import ru.beggar.berlit.block.BlockTestGenerator;
import ru.beggar.berlit.gui.GuiHandler;
import ru.beggar.berlit.item.ItemAccelerator;
import ru.beggar.berlit.item.ItemEntityCard;
import ru.beggar.berlit.item.ItemSpawnerCore;
import ru.beggar.berlit.item.ItemStackUpgrade;
import ru.beggar.berlit.machine.MachineRecipes;
import ru.beggar.berlit.machine.MachineType;
import ru.beggar.berlit.network.ModNetwork;
import ru.beggar.berlit.recipe.MolecularRecipes;
import ru.beggar.berlit.spawner.EntityRegistry;
import ru.beggar.berlit.spawner.UpgradeRegistry;
import ru.beggar.berlit.tileentity.TileEntityMolecularTransformer;
import ru.beggar.berlit.tileentity.TileEntityProcessingMachine;
import ru.beggar.berlit.tileentity.TileEntitySpawner;
import ru.beggar.berlit.tileentity.TileEntitySpawnerT2;
import ru.beggar.berlit.tileentity.TileEntityTestGenerator;

@Mod(modid = Berlit.MODID, name = Berlit.MOD_NAME, version = Tags.VERSION, acceptedMinecraftVersions = "[1.7.10]")
public class Berlit {

    public static final String MODID = "berlit";
    public static final String MOD_NAME = "Berlit";
    public static final Logger LOG = LogManager.getLogger(MODID);

    /** Creative inventory tab, redstone dust as the tab icon. */
    public static final CreativeTabs TAB = new CreativeTabs(MODID) {

        @Override
        public Item getTabIconItem() {
            return Items.redstone;
        }
    };

    @Mod.Instance(MODID)
    public static Berlit instance;

    public static BlockMolecularTransformer molecularTransformer;
    public static BlockTestGenerator testGenerator;
    public static BlockSpawner spawner;
    public static BlockSpawnerT2 spawnerT2;
    public static ItemEntityCard entityCard;
    public static ItemAccelerator acceleratorT2;
    public static ItemAccelerator acceleratorT3;
    public static ItemSpawnerCore spawnerCore1;
    public static ItemSpawnerCore spawnerCore2;
    public static ItemSpawnerCore spawnerCore3;
    public static BlockProcessingMachine macerator;
    public static BlockProcessingMachine compressor;
    public static BlockProcessingMachine metalFormer;
    public static BlockProcessingMachine metalRoller;
    public static BlockProcessingMachine electricFurnace;
    public static ItemStackUpgrade stackUpgrade;
    public static final java.util.Map<MachineType, BlockProcessingMachine> machineBlocks = new java.util.EnumMap<MachineType, BlockProcessingMachine>(
        MachineType.class);

    @SidedProxy(clientSide = "ru.beggar.berlit.ClientProxy", serverSide = "ru.beggar.berlit.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    // preInit "Run before anything else. Read your config, create blocks, items, etc, and register them with the
    // GameRegistry." (Remove if not needed)
    public void preInit(FMLPreInitializationEvent event) {
        EntityRegistry.init();
        UpgradeRegistry.init();

        molecularTransformer = new BlockMolecularTransformer();
        GameRegistry.registerBlock(molecularTransformer, "berlit.molecular_transformer");
        GameRegistry.registerTileEntity(TileEntityMolecularTransformer.class, "berlit:molecular_transformer");

        testGenerator = new BlockTestGenerator();
        GameRegistry.registerBlock(testGenerator, "berlit.test_generator");
        GameRegistry.registerTileEntity(TileEntityTestGenerator.class, "berlit:test_generator");

        spawner = new BlockSpawner();
        GameRegistry.registerBlock(spawner, "berlit.spawner");
        GameRegistry.registerTileEntity(TileEntitySpawner.class, "berlit:spawner");

        spawnerT2 = new BlockSpawnerT2();
        GameRegistry.registerBlock(spawnerT2, "berlit.spawner_t2");
        GameRegistry.registerTileEntity(TileEntitySpawnerT2.class, "berlit:spawner_t2");

        entityCard = new ItemEntityCard();
        GameRegistry.registerItem(entityCard, "entity_card");

        acceleratorT2 = new ItemAccelerator(ItemAccelerator.TIER_2);
        GameRegistry.registerItem(acceleratorT2, "accelerator_t2");

        acceleratorT3 = new ItemAccelerator(ItemAccelerator.TIER_3);
        GameRegistry.registerItem(acceleratorT3, "accelerator_t3");

        spawnerCore1 = new ItemSpawnerCore(1);
        GameRegistry.registerItem(spawnerCore1, "spawner_core_t1");

        spawnerCore2 = new ItemSpawnerCore(2);
        GameRegistry.registerItem(spawnerCore2, "spawner_core_t2");

        spawnerCore3 = new ItemSpawnerCore(3);
        GameRegistry.registerItem(spawnerCore3, "spawner_core_t3");

        macerator = new BlockProcessingMachine(MachineType.MACERATOR);
        GameRegistry.registerBlock(macerator, "berlit.machine_macerator");
        machineBlocks.put(MachineType.MACERATOR, macerator);

        compressor = new BlockProcessingMachine(MachineType.COMPRESSOR);
        GameRegistry.registerBlock(compressor, "berlit.machine_compressor");
        machineBlocks.put(MachineType.COMPRESSOR, compressor);

        metalFormer = new BlockProcessingMachine(MachineType.METAL_FORMER);
        GameRegistry.registerBlock(metalFormer, "berlit.machine_metal_former");
        machineBlocks.put(MachineType.METAL_FORMER, metalFormer);

        metalRoller = new BlockProcessingMachine(MachineType.METAL_ROLLER);
        GameRegistry.registerBlock(metalRoller, "berlit.machine_metal_roller");
        machineBlocks.put(MachineType.METAL_ROLLER, metalRoller);

        electricFurnace = new BlockProcessingMachine(MachineType.ELECTRIC_FURNACE);
        GameRegistry.registerBlock(electricFurnace, "berlit.machine_electric_furnace");
        machineBlocks.put(MachineType.ELECTRIC_FURNACE, electricFurnace);

        GameRegistry.registerTileEntity(TileEntityProcessingMachine.class, "berlit:processing_machine");

        stackUpgrade = new ItemStackUpgrade();
        GameRegistry.registerItem(stackUpgrade, "stack_upgrade");

        ModNetwork.init();

        proxy.preInit(event);
    }

    @Mod.EventHandler
    // load "Do your mod setup. Build whatever data structures you care about. Register recipes." (Remove if not needed)
    public void init(FMLInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());

        MolecularRecipes.init();
        MachineRecipes.init();
        EntityRegistry.registerOptional();
        registerRecipes();
        registerCraftTweaker();

        proxy.init(event);
    }

    private void registerCraftTweaker() {
        if (!Loader.isModLoaded("MineTweaker3")) return;

        for (String className : new String[] { "ru.beggar.berlit.compat.MolecularTransformerTweaker",
            "ru.beggar.berlit.compat.SpawnerTweaker", "ru.beggar.berlit.compat.MachinesTweaker" }) {
            try {
                Class.forName(className)
                    .getMethod("register")
                    .invoke(null);
            } catch (Throwable t) {
                LOG.warn("CraftTweaker integration " + className + " is not available", t);
            }
        }
    }

    private void registerRecipes() {
        GameRegistry.addRecipe(
            new ItemStack(molecularTransformer),
            "IRI",
            "RMR",
            "IRI",
            'I',
            Items.iron_ingot,
            'R',
            Items.redstone,
            'M',
            Blocks.redstone_block);

        GameRegistry.addRecipe(
            new ItemStack(testGenerator),
            "IRI",
            "IFI",
            "IRI",
            'I',
            Items.iron_ingot,
            'R',
            Items.redstone,
            'F',
            Blocks.furnace);

        GameRegistry.addRecipe(
            new ItemStack(spawner),
            "IRI",
            "RER",
            "IRI",
            'I',
            Items.iron_ingot,
            'R',
            Items.redstone,
            'E',
            Items.emerald);

        GameRegistry.addRecipe(
            new ItemStack(spawnerT2),
            "DED",
            "ESE",
            "DED",
            'D',
            Items.diamond,
            'E',
            Items.emerald,
            'S',
            spawner);

        GameRegistry.addRecipe(
            new ItemStack(acceleratorT2),
            "DRD",
            "RGR",
            "DRD",
            'D',
            Items.diamond,
            'R',
            Items.redstone,
            'G',
            Items.gold_ingot);

        GameRegistry.addRecipe(
            new ItemStack(acceleratorT3),
            "DED",
            "ETE",
            "DED",
            'D',
            Items.diamond,
            'E',
            Items.emerald,
            'T',
            acceleratorT2);

        GameRegistry.addShapelessRecipe(new ItemStack(spawnerCore1), Items.diamond, Items.redstone, Items.gold_ingot);
        GameRegistry.addShapelessRecipe(new ItemStack(spawnerCore2), spawnerCore1, Items.emerald, Items.diamond);
        GameRegistry
            .addShapelessRecipe(new ItemStack(spawnerCore3), spawnerCore2, Blocks.diamond_block, Items.nether_star);

        GameRegistry.addRecipe(
            new ItemStack(macerator),
            "IFI",
            "IRI",
            "IFI",
            'I',
            Items.iron_ingot,
            'F',
            Items.flint,
            'R',
            Items.redstone);

        GameRegistry.addRecipe(
            new ItemStack(compressor),
            "IPI",
            "IRI",
            "IPI",
            'I',
            Items.iron_ingot,
            'P',
            Blocks.piston,
            'R',
            Items.redstone);

        GameRegistry.addRecipe(
            new ItemStack(metalFormer),
            "IBI",
            "IRI",
            "IBI",
            'I',
            Items.iron_ingot,
            'B',
            Blocks.iron_block,
            'R',
            Items.redstone);

        GameRegistry.addRecipe(
            new ItemStack(metalRoller),
            "IPI",
            "IRI",
            "IPI",
            'I',
            Items.iron_ingot,
            'P',
            Blocks.piston,
            'R',
            Items.redstone);

        GameRegistry.addRecipe(
            new ItemStack(electricFurnace),
            "IFI",
            "IRI",
            "IFI",
            'I',
            Items.iron_ingot,
            'F',
            Blocks.furnace,
            'R',
            Items.redstone);

        GameRegistry.addShapelessRecipe(new ItemStack(stackUpgrade), Items.paper, Blocks.chest, Items.redstone);

        GameRegistry
            .addShapelessRecipe(ItemEntityCard.create("iron_golem"), Items.paper, Items.iron_ingot, Items.emerald);
    }

    @Mod.EventHandler
    // postInit "Handle interaction with other mods, complete your setup based on this." (Remove if not needed)
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @Mod.EventHandler
    // register server commands in this event handler (Remove if not needed)
    public void serverStarting(FMLServerStartingEvent event) {
        proxy.serverStarting(event);
    }
}
