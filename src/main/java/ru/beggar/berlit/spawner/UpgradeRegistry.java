package ru.beggar.berlit.spawner;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.item.ItemStack;

/**
 * Recognises the cores that may be installed into a spawner.
 *
 * <p>
 * Two sources: Berlit's own {@link ItemSpawnerCore} line (through
 * {@link ISpawnerUpgrade}) and Draconic Evolution's four cores. DE is optional, so it is
 * matched by unlocalized name rather than by class.
 *
 * <p>
 * A DE core is named {@code item.draconicevolution:draconicCore}: {@code ItemDE}
 * overrides {@code getUnlocalizedName()} to prepend {@code item.<modid>:}. The previous
 * lookup used {@code draconicevolution:draconicCore}, which never matched any stack, so
 * no DE core was ever accepted. Matching here ignores the prefix, so a different
 * namespace in another pack build still works.
 */
public final class UpgradeRegistry {

    public interface ISpawnerUpgrade {

        UpgradeStats getSpawnerUpgrade(ItemStack stack);
    }

    /** Draconic Evolution core names, in the order they are documented. */
    public static final String[] DRACONIC_CORES = { "draconicCore", "wyvernCore", "awakenedCore", "chaoticCore" };

    /** Keyed by lower-case unlocalized name. */
    private static final Map<String, UpgradeStats> BY_NAME = new LinkedHashMap<String, UpgradeStats>();

    /** Keyed by lower-case bare core name, for the namespace-independent fallback. */
    private static final Map<String, UpgradeStats> BY_CORE = new LinkedHashMap<String, UpgradeStats>();

    private UpgradeRegistry() {}

    public static void init() {
        if (!BY_NAME.isEmpty()) return;

        addCore("draconicCore", 0.25F, 1);
        addCore("wyvernCore", 0.5F, 2);
        addCore("awakenedCore", 1.0F, 3);
        addCore("chaoticCore", 2.0F, 5);
    }

    /** Registers a core by its unlocalized name, for other mods and CraftTweaker. */
    public static void register(String unlocalizedName, float speedBonus, int amountBonus) {
        if (unlocalizedName == null) return;
        BY_NAME.put(unlocalizedName.toLowerCase(), new UpgradeStats(speedBonus, amountBonus));
    }

    private static void addCore(String coreName, float speedBonus, int amountBonus) {
        UpgradeStats stats = new UpgradeStats(speedBonus, amountBonus);
        String key = coreName.toLowerCase();

        BY_CORE.put(key, stats);
        BY_NAME.put("item.draconicevolution:" + key, stats);
        BY_NAME.put("draconicevolution:" + key, stats);
        BY_NAME.put(key, stats);
    }

    public static UpgradeStats get(ItemStack stack) {
        if (stack == null) return null;

        if (stack.getItem() instanceof ISpawnerUpgrade) {
            return ((ISpawnerUpgrade) stack.getItem()).getSpawnerUpgrade(stack);
        }

        String name = stack.getUnlocalizedName();
        if (name == null) return null;
        String key = name.toLowerCase();

        UpgradeStats exact = BY_NAME.get(key);
        if (exact != null) return exact;

        // Whatever namespace the core ended up under, "…:draconicCore" still counts.
        for (Map.Entry<String, UpgradeStats> entry : BY_CORE.entrySet()) {
            if (key.endsWith(":" + entry.getKey()) || key.endsWith("." + entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    /** True when both stacks are the same core kind, ignoring stack size. */
    public static boolean isSameType(ItemStack first, ItemStack second) {
        if (first == null || second == null) return false;
        return first.getItem() == second.getItem() && first.getItemDamage() == second.getItemDamage();
    }
}
