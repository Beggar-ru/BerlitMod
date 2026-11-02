package ru.beggar.berlit.spawner;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.ItemStack;

public final class UpgradeRegistry {

    public interface ISpawnerUpgrade {

        UpgradeStats getSpawnerUpgrade(ItemStack stack);
    }

    private static final Map<String, UpgradeStats> BY_NAME = new HashMap<String, UpgradeStats>();

    private UpgradeRegistry() {}

    public static void init() {
        // Draconic Evolution cores (matched by unlocalized name, no hard dependency on DE).
        register("draconicevolution:draconicCore", 0.25F, 1);
        register("draconicevolution:wyvernCore", 0.5F, 2);
        register("draconicevolution:awakenedCore", 1.0F, 3);
        register("draconicevolution:chaoticCore", 2.0F, 5);
    }

    public static void register(String unlocalizedName, float speedBonus, int amountBonus) {
        BY_NAME.put(unlocalizedName, new UpgradeStats(speedBonus, amountBonus));
    }

    public static UpgradeStats get(ItemStack stack) {
        if (stack == null) return null;

        if (stack.getItem() instanceof ISpawnerUpgrade) {
            return ((ISpawnerUpgrade) stack.getItem()).getSpawnerUpgrade(stack);
        }

        return BY_NAME.get(
            stack.getItem()
                .getUnlocalizedName());
    }
}
