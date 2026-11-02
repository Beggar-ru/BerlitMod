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
            } catch (Throwable t) {
                Berlit.LOG.warn("NEI integration is not available", t);
            }
        }
    }
}
