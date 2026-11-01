package ru.beggar.berlit.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import cpw.mods.fml.common.network.IGuiHandler;
import ru.beggar.berlit.tileentity.TileEntityMolecularTransformer;

public class GuiHandler implements IGuiHandler {

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileEntityMolecularTransformer) {
            return new ContainerMolecularTransformer(player.inventory, (TileEntityMolecularTransformer) tile);
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity tile = world.getTileEntity(x, y, z);
        if (tile instanceof TileEntityMolecularTransformer) {
            return new GuiMolecularTransformer(player.inventory, (TileEntityMolecularTransformer) tile);
        }
        return null;
    }
}
