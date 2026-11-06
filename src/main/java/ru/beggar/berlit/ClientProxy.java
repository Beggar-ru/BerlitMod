package ru.beggar.berlit;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);

        if (Loader.isModLoaded("NotEnoughItems")) {
            try {
                Class.forName("ru.beggar.berlit.compat.nei.SpawnerNEI")
                    .getMethod("register")
                    .invoke(null);
                Class.forName("ru.beggar.berlit.compat.nei.MachineNEI")
                    .getMethod("register")
                    .invoke(null);
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
