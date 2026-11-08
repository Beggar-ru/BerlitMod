package ru.beggar.berlit.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import ru.beggar.berlit.Berlit;
import ru.beggar.berlit.matter.MatterType;

/** One of the eight solid matter kinds; the texture is per kind, so no metadata is involved. */
public class ItemMatter extends Item {

    private final MatterType type;

    public ItemMatter(MatterType type) {
        this.type = type;
        setUnlocalizedName("berlit.matter_" + type.getId());
        setTextureName("berlit:matter_" + type.getId());
        setCreativeTab(Berlit.TAB);
    }

    public MatterType getMatterType() {
        return type;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return "item.berlit.matter_" + type.getId();
    }
}
