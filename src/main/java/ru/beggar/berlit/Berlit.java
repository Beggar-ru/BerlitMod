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
import ru.beggar.berlit.block.BlockSpawner;
import ru.beggar.berlit.block.BlockSpawnerT2;
import ru.beggar.berlit.block.BlockTestGenerator;
import ru.beggar.berlit.gui.GuiHandler;
import ru.beggar.berlit.item.ItemEntityCard;
import ru.beggar.berlit.network.ModNetwork;
import ru.beggar.berlit.recipe.MolecularRecipes;
import ru.beggar.berlit.spawner.EntityRegistry;
import ru.beggar.berlit.spawner.UpgradeRegistry;
import ru.beggar.berlit.tileentity.TileEntityMolecularTransformer;
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

        ModNetwork.init();

        proxy.preInit(event);
    }

    @Mod.EventHandler
    // load "Do your mod setup. Build whatever data structures you care about. Register recipes." (Remove if not needed)
    public void init(FMLInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());

        MolecularRecipes.init();
        registerRecipes();
        registerCraftTweaker();

        proxy.init(event);
    }

    private void registerCraftTweaker() {
        if (!Loader.isModLoaded("MineTweaker3")) return;

        for (String className : new String[] { "ru.beggar.berlit.compat.MolecularTransformerTweaker",
            "ru.beggar.berlit.compat.SpawnerTweaker" }) {
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
