package ru.beggar.berlit.gui;

import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.network.IGuiHandler;
import ru.beggar.berlit.ae2.iface.ContainerBigInterface18;
import ru.beggar.berlit.ae2.iface.ContainerBigInterface27;
import ru.beggar.berlit.ae2.iface.ContainerBigInterface36;
import ru.beggar.berlit.ae2.iface.GuiBigInterface;
import ru.beggar.berlit.ae2.iface.PartBigInterface18;
import ru.beggar.berlit.ae2.iface.PartBigInterface27;
import ru.beggar.berlit.ae2.iface.PartBigInterface36;
import ru.beggar.berlit.ae2.iface.TileBigInterface18;
import ru.beggar.berlit.ae2.iface.TileBigInterface27;
import ru.beggar.berlit.ae2.iface.TileBigInterface36;
import ru.beggar.berlit.tileentity.TileEntityMolecularFarm;
import ru.beggar.berlit.tileentity.TileEntityMolecularTransformer;
import ru.beggar.berlit.tileentity.TileEntityProcessingMachine;
import ru.beggar.berlit.tileentity.TileEntitySpawnerBase;
import ru.beggar.berlit.tileentity.TileEntitySuperChest;

public class GuiHandler implements IGuiHandler {

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(x, y, z);

        if (Loader.isModLoaded("appliedenergistics2")) {
            Object bigInterface = bigInterfaceContainer(player, tile);
            if (bigInterface != null) return bigInterface;
        }

        if (tile instanceof TileEntityMolecularTransformer) {
            return new ContainerMolecularTransformer(player.inventory, (TileEntityMolecularTransformer) tile);
        }
        if (tile instanceof TileEntityMolecularFarm) {
            return new ContainerMolecularFarm(player.inventory, (TileEntityMolecularFarm) tile);
        }
        if (tile instanceof ru.beggar.berlit.tileentity.TileEntityReplicator) {
            return new ContainerReplicator(player.inventory, (ru.beggar.berlit.tileentity.TileEntityReplicator) tile);
        }
        if (tile instanceof TileEntitySpawnerBase) {
            return new ContainerSpawner(player.inventory, (TileEntitySpawnerBase) tile);
        }
        if (tile instanceof TileEntityProcessingMachine) {
            return new ContainerProcessingMachine(player.inventory, (TileEntityProcessingMachine) tile);
        }
        if (tile instanceof TileEntitySuperChest) {
            return new ContainerSuperChest(player.inventory, (TileEntitySuperChest) tile);
        }
        if (tile instanceof ru.beggar.berlit.tileentity.TileEntityCobbleGenerator) {
            return new ContainerChest(player.inventory, (IInventory) tile);
        }
        if (Loader.isModLoaded("appliedenergistics2")) {
            if (tile instanceof ru.beggar.berlit.ae2.TileEntityDigitalMachine) {
                return new ru.beggar.berlit.ae2.ContainerDigitalMachine(
                    player.inventory,
                    (ru.beggar.berlit.ae2.TileEntityDigitalMachine) tile);
            }
            if (tile instanceof ru.beggar.berlit.ae2.TileEntityDigitalAvaritiaAssembler) {
                return new ru.beggar.berlit.ae2.ContainerDigitalMachine(
                    player.inventory,
                    (ru.beggar.berlit.ae2.TileEntityDigitalAvaritiaAssembler) tile);
            }
            if (tile instanceof ru.beggar.berlit.ae2.TileEntityDigitalNeutronCompressor) {
                return new ru.beggar.berlit.ae2.ContainerDigitalMachine(
                    player.inventory,
                    (ru.beggar.berlit.ae2.TileEntityDigitalNeutronCompressor) tile);
            }
            if (tile instanceof ru.beggar.berlit.ae2.TileEntityPatternEncoder) {
                return new ru.beggar.berlit.ae2.ContainerPatternEncoder(
                    player.inventory,
                    (ru.beggar.berlit.ae2.TileEntityPatternEncoder) tile);
            }
            if (tile instanceof ru.beggar.berlit.ae2.TileEntitySingularityEncoder) {
                return new ru.beggar.berlit.ae2.ContainerSingularityEncoder(
                    player.inventory,
                    (ru.beggar.berlit.ae2.TileEntitySingularityEncoder) tile);
            }
            if (tile instanceof ru.beggar.berlit.ae2.TileEntityAvaritiaAssembler) {
                return new ru.beggar.berlit.ae2.ContainerAvaritiaAssembler(
                    player.inventory,
                    (ru.beggar.berlit.ae2.TileEntityAvaritiaAssembler) tile);
            }
        }
        if (Loader.isModLoaded("Avaritia") && Loader.isModLoaded("appliedenergistics2")
            && tile instanceof ru.beggar.berlit.machine.TileEntityNeutronCompressor) {
            return new ru.beggar.berlit.machine.ContainerNeutronCompressor(
                player.inventory,
                (ru.beggar.berlit.machine.TileEntityNeutronCompressor) tile);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(x, y, z);

        if (Loader.isModLoaded("appliedenergistics2")) {
            Object bigInterface = bigInterfaceGui(player, tile);
            if (bigInterface != null) return bigInterface;
        }

        if (tile instanceof TileEntityMolecularTransformer) {
            return new GuiMolecularTransformer(player.inventory, (TileEntityMolecularTransformer) tile);
        }
        if (tile instanceof TileEntityMolecularFarm) {
            return new GuiMolecularFarm(player.inventory, (TileEntityMolecularFarm) tile);
        }
        if (tile instanceof ru.beggar.berlit.tileentity.TileEntityReplicator) {
            return new GuiReplicator(player.inventory, (ru.beggar.berlit.tileentity.TileEntityReplicator) tile);
        }
        if (tile instanceof TileEntitySpawnerBase) {
            return new GuiSpawner(player.inventory, (TileEntitySpawnerBase) tile);
        }
        if (tile instanceof TileEntityProcessingMachine) {
            return new GuiProcessingMachine(player.inventory, (TileEntityProcessingMachine) tile);
        }
        if (tile instanceof TileEntitySuperChest) {
            return new GuiSuperChest(player.inventory, (TileEntitySuperChest) tile);
        }
        if (tile instanceof ru.beggar.berlit.tileentity.TileEntityCobbleGenerator) {
            return new GuiChest(player.inventory, (IInventory) tile);
        }
        if (Loader.isModLoaded("appliedenergistics2")) {
            if (tile instanceof ru.beggar.berlit.ae2.TileEntityDigitalMachine) {
                return new ru.beggar.berlit.ae2.GuiDigitalMachine(
                    player.inventory,
                    (ru.beggar.berlit.ae2.TileEntityDigitalMachine) tile);
            }
            if (tile instanceof ru.beggar.berlit.ae2.TileEntityDigitalAvaritiaAssembler) {
                return new ru.beggar.berlit.ae2.GuiDigitalMachine(
                    player.inventory,
                    (ru.beggar.berlit.ae2.TileEntityDigitalAvaritiaAssembler) tile);
            }
            if (tile instanceof ru.beggar.berlit.ae2.TileEntityDigitalNeutronCompressor) {
                return new ru.beggar.berlit.ae2.GuiDigitalMachine(
                    player.inventory,
                    (ru.beggar.berlit.ae2.TileEntityDigitalNeutronCompressor) tile);
            }
            if (tile instanceof ru.beggar.berlit.ae2.TileEntityPatternEncoder) {
                return new ru.beggar.berlit.ae2.GuiPatternEncoder(
                    player.inventory,
                    (ru.beggar.berlit.ae2.TileEntityPatternEncoder) tile);
            }
            if (tile instanceof ru.beggar.berlit.ae2.TileEntitySingularityEncoder) {
                return new ru.beggar.berlit.ae2.GuiSingularityEncoder(
                    player.inventory,
                    (ru.beggar.berlit.ae2.TileEntitySingularityEncoder) tile);
            }
            if (tile instanceof ru.beggar.berlit.ae2.TileEntityAvaritiaAssembler) {
                return new ru.beggar.berlit.ae2.GuiAvaritiaAssembler(
                    player.inventory,
                    (ru.beggar.berlit.ae2.TileEntityAvaritiaAssembler) tile);
            }
        }
        if (Loader.isModLoaded("Avaritia") && Loader.isModLoaded("appliedenergistics2")
            && tile instanceof ru.beggar.berlit.machine.TileEntityNeutronCompressor) {
            return new ru.beggar.berlit.machine.GuiNeutronCompressor(
                player.inventory,
                (ru.beggar.berlit.machine.TileEntityNeutronCompressor) tile);
        }
        return null;
    }

    private static appeng.helpers.IInterfaceHost findBigInterface(TileEntity tile) {
        if (tile instanceof TileBigInterface18 || tile instanceof TileBigInterface27
            || tile instanceof TileBigInterface36) {
            return (appeng.helpers.IInterfaceHost) tile;
        }
        if (tile instanceof appeng.api.parts.IPartHost) {
            appeng.api.parts.IPartHost host = (appeng.api.parts.IPartHost) tile;
            for (net.minecraftforge.common.util.ForgeDirection direction : net.minecraftforge.common.util.ForgeDirection
                .values()) {
                appeng.api.parts.IPart part = host.getPart(direction);
                if (part instanceof PartBigInterface18 || part instanceof PartBigInterface27
                    || part instanceof PartBigInterface36) {
                    return (appeng.helpers.IInterfaceHost) part;
                }
            }
        }
        return null;
    }

    private static Object bigInterfaceContainer(EntityPlayer player, TileEntity tile) {
        appeng.helpers.IInterfaceHost host = findBigInterface(tile);
        if (host == null) return null;
        int patterns = host.getInterfaceDuality()
            .getPatterns()
            .getSizeInventory();
        if (patterns == 18) return new ContainerBigInterface18(player.inventory, host);
        if (patterns == 27) return new ContainerBigInterface27(player.inventory, host);
        if (patterns == 36) return new ContainerBigInterface36(player.inventory, host);
        return null;
    }

    private static Object bigInterfaceGui(EntityPlayer player, TileEntity tile) {
        appeng.helpers.IInterfaceHost host = findBigInterface(tile);
        if (host == null) return null;
        int patterns = host.getInterfaceDuality()
            .getPatterns()
            .getSizeInventory();
        if (patterns == 18) return new GuiBigInterface(new ContainerBigInterface18(player.inventory, host));
        if (patterns == 27) return new GuiBigInterface(new ContainerBigInterface27(player.inventory, host));
        if (patterns == 36) return new GuiBigInterface(new ContainerBigInterface36(player.inventory, host));
        return null;
    }
}
