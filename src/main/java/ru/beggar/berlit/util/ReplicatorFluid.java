package ru.beggar.berlit.util;

import java.util.Locale;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

/**
 * Locates IC2's liquid UU matter and works with containers of it.
 *
 * <p>
 * IC2 registers the fluid under the name {@code ic2uumatter} (the internal name is
 * {@code fluidUuMatter}); the cell is registered in Forge's {@link FluidContainerRegistry}. The
 * lookup is name based so Berlit does not have to link against IC2 internals at class load time.
 */
public final class ReplicatorFluid {

    private static final String[] FLUID_NAMES = { "ic2uumatter", "uu_matter", "uumatter" };

    private static Fluid cached;

    private ReplicatorFluid() {}

    public static synchronized Fluid get() {
        if (cached != null) return cached;

        for (String name : FLUID_NAMES) {
            Fluid fluid = FluidRegistry.getFluid(name);
            if (fluid != null) {
                cached = fluid;
                return cached;
            }
        }

        // Be tolerant of forks/packs that register the same fluid under a decorated name.
        for (java.util.Map.Entry<String, Fluid> entry : FluidRegistry.getRegisteredFluids()
            .entrySet()) {
            String name = entry.getKey()
                .toLowerCase(Locale.ENGLISH);
            if (name.contains("uumatter") || name.contains("uu_matter")) {
                cached = entry.getValue();
                return cached;
            }
        }

        return null;
    }

    public static boolean isUuMatter(Fluid fluid) {
        Fluid uu = get();
        return uu != null && fluid == uu;
    }

    public static boolean isUuMatter(FluidStack stack) {
        return stack != null && stack.getFluid() != null && isUuMatter(stack.getFluid());
    }

    /** The UU matter contained in a filled cell, or {@code null} for any other item. */
    public static FluidStack getContainerFluid(ItemStack stack) {
        if (stack == null) return null;

        try {
            FluidStack fluid = FluidContainerRegistry.getFluidForFilledItem(stack);
            return isUuMatter(fluid) ? fluid : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static boolean isUuMatterContainer(ItemStack stack) {
        return getContainerFluid(stack) != null;
    }

    /** The empty container left after draining a UU matter cell, or {@code null}. */
    public static ItemStack drainContainer(ItemStack stack) {
        try {
            return FluidContainerRegistry.drainFluidContainer(stack);
        } catch (Throwable ignored) {
            return null;
        }
    }

    public static FluidStack createStack(int millibuckets) {
        Fluid fluid = get();
        return fluid == null || millibuckets <= 0 ? null : new FluidStack(fluid, millibuckets);
    }
}
