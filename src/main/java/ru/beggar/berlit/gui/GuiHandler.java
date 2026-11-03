package ru.beggar.berlit.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import cpw.mods.fml.common.network.IGuiHandler;
import ru.beggar.berlit.tileentity.TileEntityMolecularTransformer;
import ru.beggar.berlit.tileentity.TileEntityProcessingMachine;
import ru.beggar.berlit.tileentity.TileEntitySpawnerBase;
import ru.beggar.berlit.tileentity.TileEntitySuperChest;

public class GuiHandler implements IGuiHandler {

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(x, y, z);

        if (tile instanceof TileEntityMolecularTransformer) {
            return new ContainerMolecularTransformer(player.inventory, (TileEntityMolecularTransformer) tile);
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
        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(x, y, z);

        if (tile instanceof TileEntityMolecularTransformer) {
            return new GuiMolecularTransformer(player.inventory, (TileEntityMolecularTransformer) tile);
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
        return null;
    }
}
