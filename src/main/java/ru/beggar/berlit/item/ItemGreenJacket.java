package ru.beggar.berlit.item;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

import ru.beggar.berlit.Berlit;

/** Green jacket: chest armor with 20 armour points (easter egg for LOTerGun). */
public class ItemGreenJacket extends ItemArmor {

    public ItemGreenJacket(ArmorMaterial material) {
        super(material, 0, 1);
        setUnlocalizedName("berlit.green_jacket");
        setTextureName("berlit:green_jacket");
        setCreativeTab(Berlit.TAB);
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, int slot, String type) {
        return "berlit:textures/armor/green_jacket_1.png";
    }
}
