package ru.beggar.berlit.upgrade;

import net.minecraft.item.ItemStack;

import ru.beggar.berlit.item.ItemAccelerator;
import ru.beggar.berlit.item.ItemStackUpgrade;

/**
 * Handles IC2 upgrade modules and the Berlit upgrades for the machines.
 */
public final class UpgradeHelper {

    public static final class Stats {

        public float speed = 1.0F;
        public float energyCost = 1.0F;
        public boolean ejector;
        public boolean puller;
        public boolean batch;
        public int storage;
        public int tierBonus;

        public boolean hasAny() {
            return speed != 1.0F || energyCost != 1.0F || ejector || puller || batch || storage != 0 || tierBonus != 0;
        }
    }

    private UpgradeHelper() {}

    public static Stats get(ItemStack stack) {
        if (stack == null) return null;

        if (stack.getItem() instanceof ItemStackUpgrade) {
            Stats stats = new Stats();
            stats.batch = true;
            return stats;
        }

        String name = stack.getItem()
            .getUnlocalizedName();

        if (name.contains("overclockerUpgrade")) {
            Stats stats = new Stats();
            stats.speed = 1.6F;
            stats.energyCost = 1.6F;
            return stats;
        }
        if (name.contains("ejectorUpgrade") || name.contains("fluidEjectorUpgrade")) {
            Stats stats = new Stats();
            stats.ejector = true;
            return stats;
        }
        if (name.contains("pullingUpgrade")) {
            Stats stats = new Stats();
            stats.puller = true;
            return stats;
        }
        if (name.contains("energyStorageUpgrade")) {
            Stats stats = new Stats();
            stats.storage = 10000;
            return stats;
        }
        if (name.contains("transformerUpgrade")) {
            Stats stats = new Stats();
            stats.tierBonus = 1;
            return stats;
        }

        if (stack.getItem() instanceof ItemAccelerator) {
            ItemAccelerator accelerator = (ItemAccelerator) stack.getItem();
            Stats stats = new Stats();
            stats.speed = accelerator.getSpeedMultiplier();
            stats.energyCost = accelerator.getEnergyMultiplier();
            return stats;
        }

        return null;
    }

    public static boolean isMachineUpgrade(ItemStack stack) {
        return get(stack) != null;
    }

    /** True for overclockers and Berlit accelerators. */
    public static boolean isSpeedUpgrade(ItemStack stack) {
        Stats stats = get(stack);
        return stats != null && stats.speed != 1.0F;
    }

    public static Stats combine(Iterable<ItemStack> stacks) {
        Stats total = new Stats();
        for (ItemStack stack : stacks) {
            Stats stats = get(stack);
            if (stats == null) continue;
            total.speed *= stats.speed;
            total.energyCost *= stats.energyCost;
            total.ejector |= stats.ejector;
            total.puller |= stats.puller;
            total.batch |= stats.batch;
            total.storage += stats.storage;
            total.tierBonus += stats.tierBonus;
        }
        return total;
    }
}
