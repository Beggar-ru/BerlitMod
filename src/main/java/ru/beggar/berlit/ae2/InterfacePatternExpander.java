package ru.beggar.berlit.ae2;

import java.lang.reflect.Field;

import appeng.helpers.DualityInterface;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.IAEAppEngInventory;

/**
 * Expands the pattern inventory of AE2's standard DualityInterface. The parent class only uses
 * the inventory through its private field, so replacing that field is enough for provideCrafting,
 * NBT persistence and the custom container to see all slots.
 */
public final class InterfacePatternExpander {

    private InterfacePatternExpander() {}

    public static boolean expand(Object host, int patterns) {
        try {
            Field dualityField = null;
            Class<?> current = host.getClass();
            while (current != null && dualityField == null) {
                try {
                    dualityField = current.getDeclaredField("duality");
                } catch (NoSuchFieldException ignored) {
                    current = current.getSuperclass();
                }
            }
            if (dualityField == null) return false;
            dualityField.setAccessible(true);

            Object duality = dualityField.get(host);
            if (!(duality instanceof DualityInterface)) return false;

            Field patternsField = DualityInterface.class.getDeclaredField("patterns");
            patternsField.setAccessible(true);
            patternsField.set(duality, new AppEngInternalInventory((IAEAppEngInventory) duality, patterns));
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
