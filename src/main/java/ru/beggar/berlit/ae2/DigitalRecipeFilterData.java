package ru.beggar.berlit.ae2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.WorldSavedData;

/** Per-world list of digital-machine patterns disabled by the player. */
public class DigitalRecipeFilterData extends WorldSavedData {

    public static final String NAME = "berlit_digital_filters";

    private final Map<String, Set<String>> disabled = new HashMap<String, Set<String>>();

    public DigitalRecipeFilterData() {
        super(NAME);
    }

    public DigitalRecipeFilterData(String name) {
        super(name);
    }

    public Set<String> getDisabled(String machineType) {
        Set<String> set = disabled.get(machineType);
        if (set == null) {
            set = new HashSet<String>();
            disabled.put(machineType, set);
        }
        return set;
    }

    public boolean isDisabled(String machineType, String key) {
        return getDisabled(machineType).contains(key);
    }

    public void setEnabled(String machineType, String key, boolean enabled) {
        Set<String> set = getDisabled(machineType);
        if (enabled) {
            set.remove(key);
        } else {
            set.add(key);
        }
        markDirty();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        disabled.clear();

        NBTTagList list = nbt.getTagList("Filters", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound entry = list.getCompoundTagAt(i);
            String type = entry.getString("Type");
            String key = entry.getString("Key");
            if (type != null && !type.isEmpty() && key != null && !key.isEmpty()) {
                getDisabled(type).add(key);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        NBTTagList list = new NBTTagList();
        for (Map.Entry<String, Set<String>> entry : disabled.entrySet()) {
            for (String key : entry.getValue()) {
                NBTTagCompound tag = new NBTTagCompound();
                tag.setString("Type", entry.getKey());
                tag.setString("Key", key);
                list.appendTag(tag);
            }
        }
        nbt.setTag("Filters", list);
    }
}
