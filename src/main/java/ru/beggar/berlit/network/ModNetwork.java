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
        INSTANCE.registerMessage(PacketEncode.Handler.class, PacketEncode.class, 3, Side.SERVER);
        INSTANCE.registerMessage(PacketCraft.Handler.class, PacketCraft.class, 4, Side.SERVER);
        INSTANCE.registerMessage(ReplicatorSyncHandler.class, PacketReplicatorSync.class, 5, Side.CLIENT);
        INSTANCE.registerMessage(PacketReplicatorAction.Handler.class, PacketReplicatorAction.class, 6, Side.SERVER);
        INSTANCE.registerMessage(DigitalSyncHandler.class, PacketDigitalSync.class, 7, Side.CLIENT);
        INSTANCE.registerMessage(DigitalFilterSyncHandler.class, PacketDigitalFilterSync.class, 8, Side.CLIENT);
        INSTANCE.registerMessage(
            PacketDigitalFilterRequest.Handler.class,
            PacketDigitalFilterRequest.class,
            9,
            Side.SERVER);
        INSTANCE
            .registerMessage(PacketDigitalFilterToggle.Handler.class, PacketDigitalFilterToggle.class, 10, Side.SERVER);
    }
}
