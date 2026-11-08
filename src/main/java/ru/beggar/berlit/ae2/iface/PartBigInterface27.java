package ru.beggar.berlit.ae2.iface;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;

import appeng.parts.misc.PartInterface;
import ru.beggar.berlit.Berlit;
import ru.beggar.berlit.ae2.InterfacePatternExpander;

/** Cable-attached ME Interface with 27 pattern slots. */
public class PartBigInterface27 extends PartInterface {

    public PartBigInterface27(ItemStack stack) {
        super(stack);
        InterfacePatternExpander.expand(this, 27);
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
