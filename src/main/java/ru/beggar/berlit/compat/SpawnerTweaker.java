package ru.beggar.berlit.compat;

import net.minecraft.item.ItemStack;

import minetweaker.IUndoableAction;
import minetweaker.MineTweakerAPI;
import minetweaker.api.item.IItemStack;
import minetweaker.api.minecraft.MineTweakerMC;
import ru.beggar.berlit.spawner.EntityDefinition;
import ru.beggar.berlit.spawner.EntityDrop;
import ru.beggar.berlit.spawner.EntityRegistry;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

/**
 * CraftTweaker integration for the entity spawner.
 *
 * <pre>
 * import mods.berlit.Spawner;
 *
 * Spawner.addDrop("iron_golem", &lt;minecraft:gold_ingot&gt;, 0.5, 1, 2);
 * Spawner.removeDrop("iron_golem", &lt;minecraft:iron_ingot&gt;);
 * Spawner.addEntity("zombie", "Zombie", 200);
 * </pre>
 */
@ZenClass("mods.berlit.Spawner")
public final class SpawnerTweaker {

    private SpawnerTweaker() {}

    public static void register() {
        MineTweakerAPI.registerClass(SpawnerTweaker.class);
    }

    @ZenMethod
    public static void addDrop(String entityId, IItemStack stack, float chance, int min, int max) {
        EntityDefinition definition = EntityRegistry.get(entityId);
        if (definition == null) {
            MineTweakerAPI.logError("Spawner.addDrop: unknown entity '" + entityId + "'");
            return;
        }

        ItemStack item = MineTweakerMC.getItemStack(stack);
        if (item == null) {
            MineTweakerAPI.logError("Spawner.addDrop: item must not be null");
            return;
        }

        MineTweakerAPI.apply(new AddDropAction(definition, new EntityDrop(item, chance, min, max)));
    }

    @ZenMethod
    public static void removeDrop(String entityId, IItemStack stack) {
        EntityDefinition definition = EntityRegistry.get(entityId);
        if (definition == null) {
            MineTweakerAPI.logError("Spawner.removeDrop: unknown entity '" + entityId + "'");
            return;
        }

        ItemStack item = MineTweakerMC.getItemStack(stack);
        if (item == null) {
            MineTweakerAPI.logError("Spawner.removeDrop: item must not be null");
            return;
        }

        MineTweakerAPI.apply(new RemoveDropAction(definition, item));
    }

    @ZenMethod
    public static void addEntity(String id, String displayName, int baseInterval) {
        MineTweakerAPI.apply(new AddEntityAction(new EntityDefinition(id, displayName, baseInterval)));
    }

    @ZenMethod
    public static void removeEntity(String id) {
        MineTweakerAPI.apply(new RemoveEntityAction(id));
    }

    private static final class AddDropAction implements IUndoableAction {

        private final EntityDefinition definition;
        private final EntityDrop drop;

        AddDropAction(EntityDefinition definition, EntityDrop drop) {
            this.definition = definition;
            this.drop = drop;
        }

        @Override
        public void apply() {
            definition.addDrop(drop);
        }

        @Override
        public boolean canUndo() {
            return true;
        }

        @Override
        public void undo() {
            definition.removeDrop(drop.getStack());
        }

        @Override
        public String describe() {
            return "Adding spawner drop " + drop.getStack()
                .getDisplayName() + " to " + definition.getId();
        }

        @Override
        public String describeUndo() {
            return "Removing spawner drop " + drop.getStack()
                .getDisplayName() + " from " + definition.getId();
        }

        @Override
        public Object getOverrideKey() {
            return "berlit:spawner-drop:" + definition.getId()
                + ":"
                + drop.getStack()
                    .getUnlocalizedName();
        }
    }

    private static final class RemoveDropAction implements IUndoableAction {

        private final EntityDefinition definition;
        private final ItemStack stack;
        private EntityDrop removed;

        RemoveDropAction(EntityDefinition definition, ItemStack stack) {
            this.definition = definition;
            this.stack = stack;
        }

        @Override
        public void apply() {
            for (EntityDrop drop : definition.getDrops()) {
                if (drop.matches(stack)) {
                    removed = drop;
                    break;
                }
            }
            definition.removeDrop(stack);
        }

        @Override
        public boolean canUndo() {
            return removed != null;
        }

        @Override
        public void undo() {
            if (removed != null) definition.addDrop(removed);
        }

        @Override
        public String describe() {
            return "Removing spawner drop " + stack.getDisplayName() + " from " + definition.getId();
        }

        @Override
        public String describeUndo() {
            return "Restoring spawner drop " + stack.getDisplayName() + " for " + definition.getId();
        }

        @Override
        public Object getOverrideKey() {
            return "berlit:spawner-drop-remove:" + definition.getId() + ":" + stack.getUnlocalizedName();
        }
    }

    private static final class AddEntityAction implements IUndoableAction {

        private final EntityDefinition definition;

        AddEntityAction(EntityDefinition definition) {
            this.definition = definition;
        }

        @Override
        public void apply() {
            EntityRegistry.register(definition);
        }

        @Override
        public boolean canUndo() {
            return true;
        }

        @Override
        public void undo() {
            EntityRegistry.remove(definition.getId());
        }

        @Override
        public String describe() {
            return "Registering spawner entity " + definition.getId();
        }

        @Override
        public String describeUndo() {
            return "Unregistering spawner entity " + definition.getId();
        }

        @Override
        public Object getOverrideKey() {
            return "berlit:spawner-entity:" + definition.getId();
        }
    }

    private static final class RemoveEntityAction implements IUndoableAction {

        private final String id;
        private EntityDefinition removed;

        RemoveEntityAction(String id) {
            this.id = id;
        }

        @Override
        public void apply() {
            removed = EntityRegistry.get(id);
            EntityRegistry.remove(id);
        }

        @Override
        public boolean canUndo() {
            return removed != null;
        }

        @Override
        public void undo() {
            if (removed != null) EntityRegistry.register(removed);
        }

        @Override
        public String describe() {
            return "Removing spawner entity " + id;
        }

        @Override
        public String describeUndo() {
            return "Restoring spawner entity " + id;
        }

        @Override
        public Object getOverrideKey() {
            return "berlit:spawner-entity-remove:" + id;
        }
    }
}
