package ru.beggar.berlit.spawner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.item.ItemStack;

public class EntityDefinition {

    private final String id;
    private final String displayName;
    private final int baseInterval;
    private final List<EntityDrop> drops = new ArrayList<EntityDrop>();

    public EntityDefinition(String id, String displayName, int baseInterval) {
        this.id = id;
        this.displayName = displayName;
        this.baseInterval = Math.max(20, baseInterval);
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getBaseInterval() {
        return baseInterval;
    }

    public void addDrop(EntityDrop drop) {
        drops.add(drop);
    }

    public boolean removeDrop(ItemStack stack) {
        for (int i = 0; i < drops.size(); i++) {
            if (drops.get(i)
                .matches(stack)) {
                drops.remove(i);
                return true;
            }
        }
        return false;
    }

    public List<EntityDrop> getDrops() {
        return Collections.unmodifiableList(drops);
    }
}
