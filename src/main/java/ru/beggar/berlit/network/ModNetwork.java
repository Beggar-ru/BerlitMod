package ru.beggar.berlit.network;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public final class ModNetwork {

    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel("berlit");

    private ModNetwork() {}

    public static void init() {
        INSTANCE.registerMessage(MachineSyncHandler.class, PacketMachineSync.class, 0, Side.CLIENT);
        INSTANCE.registerMessage(SpawnerSyncHandler.class, PacketSpawnerSync.class, 1, Side.CLIENT);
        INSTANCE.registerMessage(PacketCycleMode.Handler.class, PacketCycleMode.class, 2, Side.SERVER);
    }
}
