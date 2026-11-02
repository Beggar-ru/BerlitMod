package ru.beggar.berlit.spawner;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public final class EntityRegistry {

    private static final Map<String, EntityDefinition> ENTITIES = new LinkedHashMap<String, EntityDefinition>();

    private EntityRegistry() {}

    public static void init() {
        if (!ENTITIES.isEmpty()) return;

        EntityDefinition ironGolem = register(new EntityDefinition("iron_golem", "Iron Golem", 200));
        ironGolem.addDrop(new EntityDrop(new ItemStack(Items.iron_ingot), 1.0F, 1, 3));
        ironGolem.addDrop(new EntityDrop(new ItemStack(Blocks.red_flower, 1, 0), 0.2F, 1, 1));
    }

    public static EntityDefinition register(EntityDefinition definition) {
        ENTITIES.put(definition.getId(), definition);
        return definition;
    }

    public static EntityDefinition remove(String id) {
        return id == null ? null : ENTITIES.remove(id);
    }

    public static EntityDefinition get(String id) {
        return id == null ? null : ENTITIES.get(id);
    }

    public static Collection<EntityDefinition> all() {
        return ENTITIES.values();
    }
}
