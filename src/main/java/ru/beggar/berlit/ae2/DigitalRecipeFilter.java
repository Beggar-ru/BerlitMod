package ru.beggar.berlit.ae2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;

import ru.beggar.berlit.machine.MachineRecipe;
import ru.beggar.berlit.machine.MachineRecipes;
import ru.beggar.berlit.machine.MachineType;

/**
 * Global (per-world) enable/disable list for the patterns exported by digital machines.
 *
 * <p>
 * Patterns are identified by their machine mode, input stack (including count and NBT) and all
 * output stacks. The list is persisted in {@link DigitalRecipeFilterData} and mirrored on the
 * client for the recipe manager screen.
 */
public final class DigitalRecipeFilter {

    private static final Map<String, Set<String>> CLIENT_DISABLED = new HashMap<String, Set<String>>();
    private static int revision;

    private DigitalRecipeFilter() {}

    public static final class Entry {

        private final String key;
        private final ItemStack input;
        private final List<ItemStack> outputs;
        private final int energy;
        private final int mode;

        private Entry(String key, ItemStack input, List<ItemStack> outputs, int energy, int mode) {
            this.key = key;
            this.input = input.copy();
            this.outputs = new ArrayList<ItemStack>(outputs.size());
            for (ItemStack output : outputs) {
                if (output != null) this.outputs.add(output.copy());
            }
            this.energy = energy;
            this.mode = mode;
        }

        public String getKey() {
            return key;
        }

        public ItemStack getInput() {
            return input.copy();
        }

        public List<ItemStack> getOutputs() {
            List<ItemStack> copy = new ArrayList<ItemStack>(outputs.size());
            for (ItemStack output : outputs) copy.add(output.copy());
            return copy;
        }

        public int getEnergy() {
            return energy;
        }

        public int getMode() {
            return mode;
        }
    }

    /** Every unique exported pattern of this machine, before the enable/disable filter. */
    public static List<Entry> getEntries(MachineType type, int mode) {
        List<Entry> result = new ArrayList<Entry>();
        Set<String> seen = new HashSet<String>();

        for (MachineRecipe recipe : MachineRecipes.getRecipes(type, mode)) {
            if (recipe.getOutputs()
                .isEmpty()) continue;

            for (ItemStack input : recipe.getInputs()) {
                if (input == null || input.stackSize <= 0) continue;
                if (MachineRecipes.isSmallDust(input) || containsSmallDust(recipe.getOutputs())) continue;

                String key = key(mode, input, recipe.getOutputs());
                if (!seen.add(key)) continue;

                result.add(new Entry(key, input, recipe.getOutputs(), recipe.getEnergy(), mode));
            }
        }

        return result;
    }

    public static boolean isEnabled(World world, MachineType type, Entry entry) {
        return !getDisabled(world, type).contains(entry.key);
    }

    public static Set<String> getDisabled(World world, MachineType type) {
        DigitalRecipeFilterData data = getData(world);
        if (data != null) return data.getDisabled(type.getId());
        return clientDisabled(type);
    }

    public static String[] getDisabledKeys(World world, MachineType type) {
        Set<String> disabled = getDisabled(world, type);
        return disabled.toArray(new String[0]);
    }

    public static void setEnabled(World world, MachineType type, String key, boolean enabled) {
        DigitalRecipeFilterData data = getData(world);
        if (data != null) {
            data.setEnabled(type.getId(), key, enabled);
        } else {
            Set<String> set = clientDisabled(type);
            if (enabled) {
                set.remove(key);
            } else {
                set.add(key);
            }
        }
        revision++;
    }

    /** Client mirror updated by {@code PacketDigitalFilterSync}. */
    public static void setClientDisabled(MachineType type, String[] keys) {
        Set<String> set = new HashSet<String>();
        if (keys != null) set.addAll(Arrays.asList(keys));
        CLIENT_DISABLED.put(type.getId(), set);
        revision++;
    }

    public static int getRevision() {
        return revision;
    }

    private static Set<String> clientDisabled(MachineType type) {
        Set<String> set = CLIENT_DISABLED.get(type.getId());
        if (set == null) {
            set = new HashSet<String>();
            CLIENT_DISABLED.put(type.getId(), set);
        }
        return set;
    }

    private static DigitalRecipeFilterData getData(World world) {
        if (world == null || world.isRemote) return null;

        MapStorage storage = world.mapStorage;
        if (storage == null) return null;

        DigitalRecipeFilterData data = (DigitalRecipeFilterData) storage
            .loadData(DigitalRecipeFilterData.class, DigitalRecipeFilterData.NAME);
        if (data == null) {
            data = new DigitalRecipeFilterData(DigitalRecipeFilterData.NAME);
            storage.setData(DigitalRecipeFilterData.NAME, data);
        }
        return data;
    }

    public static String key(int mode, ItemStack input, List<ItemStack> outputs) {
        StringBuilder result = new StringBuilder();
        result.append(mode)
            .append('|')
            .append(stackKey(input))
            .append("->");

        boolean first = true;
        for (ItemStack output : outputs) {
            if (output == null) continue;
            if (!first) result.append('+');
            result.append(stackKey(output));
            first = false;
        }
        return result.toString();
    }

    private static String stackKey(ItemStack stack) {
        if (stack == null || stack.getItem() == null) return "null";

        NBTTagCompound tag = stack.getTagCompound();
        return Item.getIdFromItem(stack.getItem()) + ":"
            + stack.getItemDamage()
            + ":"
            + stack.stackSize
            + ":"
            + (tag == null ? "" : tag.toString());
    }

    private static boolean containsSmallDust(List<ItemStack> stacks) {
        if (stacks == null) return false;
        for (ItemStack stack : stacks) {
            if (MachineRecipes.isSmallDust(stack)) return true;
        }
        return false;
    }

    /** Never used, kept so the list of disabled keys can be exposed to diagnostics if needed. */
    public static List<String> snapshotDisabled(World world, MachineType type) {
        List<String> result = new ArrayList<String>(getDisabled(world, type));
        Collections.sort(result);
        return result;
    }
}
