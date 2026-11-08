package ru.beggar.berlit.ae2.iface;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;

import appeng.parts.misc.PartInterface;
import ru.beggar.berlit.Berlit;
import ru.beggar.berlit.ae2.InterfacePatternExpander;

/** Cable-attached ME Interface with 36 pattern slots. */
public class PartBigInterface36 extends PartInterface {

    public PartBigInterface36(ItemStack stack) {
        super(stack);
        InterfacePatternExpander.expand(this, 36);
    }

    @Override
    public boolean onPartActivate(EntityPlayer player, Vec3 hit) {
        if (!player.worldObj.isRemote) {
            player.openGui(
                Berlit.instance,
                7,
                player.worldObj,
                getTileEntity().xCoord,
                getTileEntity().yCoord,
                getTileEntity().zCoord);
        }
        return true;
    }
}
