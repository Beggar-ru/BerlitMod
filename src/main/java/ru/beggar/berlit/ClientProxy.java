package ru.beggar.berlit;

import net.minecraft.item.Item;
import net.minecraftforge.client.MinecraftForgeClient;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import ru.beggar.berlit.client.renderer.BlockMolecularTransformerRenderer;
import ru.beggar.berlit.client.renderer.CpuItemRenderer;
import ru.beggar.berlit.client.renderer.TileEntityMolecularTransformerRenderer;
import ru.beggar.berlit.tileentity.TileEntityMolecularTransformer;

public class ClientProxy extends CommonProxy {

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);

        CpuItemRenderer cpuItemRenderer = new CpuItemRenderer();
        if (Berlit.craftingStorages != null) {
            for (net.minecraft.block.Block block : Berlit.craftingStorages) {
                MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(block), cpuItemRenderer);
            }
        }
        if (Berlit.craftingProcessors != null) {
            for (net.minecraft.block.Block block : Berlit.craftingProcessors) {
                MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(block), cpuItemRenderer);
            }
        }

        Berlit.molecularTransformerRenderId = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry
            .registerBlockHandler(new BlockMolecularTransformerRenderer(Berlit.molecularTransformerRenderId));
        ClientRegistry.bindTileEntitySpecialRenderer(
            TileEntityMolecularTransformer.class,
            new TileEntityMolecularTransformerRenderer());

        if (Loader.isModLoaded("NotEnoughItems")) {
            try {
                Class.forName("ru.beggar.berlit.compat.nei.SpawnerNEI")
                    .getMethod("register")
                    .invoke(null);
                Class.forName("ru.beggar.berlit.compat.nei.MachineNEI")
                    .getMethod("register")
                    .invoke(null);
                Class.forName("ru.beggar.berlit.compat.nei.MolecularFarmNEI")
                    .getMethod("register")
                    .invoke(null);
                Class.forName("ru.beggar.berlit.compat.nei.MolecularTransformerNEI")
                    .getMethod("register")
                    .invoke(null);
                try {
                    Class.forName("ru.beggar.berlit.compat.nei.MatterNEI")
                        .getMethod("register")
                        .invoke(null);
                } catch (Throwable t) {
                    Berlit.LOG.warn("Matter NEI page is not available", t);
                }
                try {
                    Class.forName("ru.beggar.berlit.compat.nei.ReplicatorNEI")
                        .getMethod("register")
                        .invoke(null);
                } catch (Throwable t) {
                    Berlit.LOG.warn("Replicator NEI page is not available", t);
                }
                if (Berlit.digitalPattern != null) {
                    try {
                        codechicken.nei.api.API.hideItem(new net.minecraft.item.ItemStack(Berlit.digitalPattern));
                    } catch (Throwable t) {
                        Berlit.LOG.warn("Could not hide the digital pattern item in NEI", t);
                    }
                }
                try {
                    codechicken.nei.api.API.hideItem(new net.minecraft.item.ItemStack(Berlit.cobbleGeneratorMk2));
                    codechicken.nei.api.API.hideItem(new net.minecraft.item.ItemStack(Berlit.cobbleGeneratorMk3));
                    codechicken.nei.api.API.hideItem(new net.minecraft.item.ItemStack(Berlit.testGenerator));
                } catch (Throwable t) {
                    Berlit.LOG.warn("Could not hide legacy items in NEI", t);
                }
                if (Loader.isModLoaded("Avaritia")) {
                    try {
                        Class.forName("ru.beggar.berlit.compat.nei.CompressorNEI")
                            .getMethod("register")
                            .invoke(null);
                    } catch (Throwable t) {
                        Berlit.LOG.warn("Compressor NEI page is not available", t);
                    }
                }
            } catch (Throwable t) {
                Berlit.LOG.warn("NEI integration is not available", t);
            }
        }
    }
}
