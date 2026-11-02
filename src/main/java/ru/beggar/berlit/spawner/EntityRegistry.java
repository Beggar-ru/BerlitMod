package ru.beggar.berlit.spawner;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;

public final class EntityRegistry {

    private static final Map<String, EntityDefinition> ENTITIES = new LinkedHashMap<String, EntityDefinition>();

    private EntityRegistry() {}

    public static void init() {
        if (!ENTITIES.isEmpty()) return;

        EntityDefinition skeleton = mob("skeleton", "Skeleton");
        skeleton.addDrop(drop(Items.bone, 1.0F, 1, 2));
        skeleton.addDrop(drop(Items.arrow, 1.0F, 0, 2));
        skeleton.addDrop(drop(new ItemStack(Items.skull, 1, 0), 0.05F, 1, 1));

        EntityDefinition witherSkeleton = mob("wither_skeleton", "Wither Skeleton");
        witherSkeleton.addDrop(drop(Items.bone, 1.0F, 0, 2));
        witherSkeleton.addDrop(drop(Items.coal, 0.33F, 0, 1));
        witherSkeleton.addDrop(drop(new ItemStack(Items.skull, 1, 1), 0.025F, 1, 1));

        EntityDefinition zombie = mob("zombie", "Zombie");
        zombie.addDrop(drop(Items.rotten_flesh, 1.0F, 0, 2));
        zombie.addDrop(drop(Items.iron_ingot, 0.025F, 1, 1));
        zombie.addDrop(drop(new ItemStack(Items.skull, 1, 2), 0.025F, 1, 1));

        EntityDefinition creeper = mob("creeper", "Creeper");
        creeper.addDrop(drop(Items.gunpowder, 1.0F, 0, 2));
        creeper.addDrop(drop(new ItemStack(Items.skull, 1, 4), 0.025F, 1, 1));

        EntityDefinition spider = mob("spider", "Spider");
        spider.addDrop(drop(Items.string, 1.0F, 0, 2));
        spider.addDrop(drop(Items.spider_eye, 0.33F, 0, 1));

        EntityDefinition caveSpider = mob("cave_spider", "Cave Spider");
        caveSpider.addDrop(drop(Items.string, 1.0F, 0, 2));
        caveSpider.addDrop(drop(Items.spider_eye, 0.33F, 0, 1));

        EntityDefinition silverfish = mob("silverfish", "Silverfish");
        silverfish.addDrop(drop(Items.iron_ingot, 0.05F, 1, 1));

        EntityDefinition slime = mob("slime", "Slime");
        slime.addDrop(drop(Items.slime_ball, 1.0F, 0, 2));

        EntityDefinition magmaCube = mob("magma_cube", "Magma Cube");
        magmaCube.addDrop(drop(Items.magma_cream, 0.25F, 0, 1));

        EntityDefinition blaze = mob("blaze", "Blaze");
        blaze.addDrop(drop(Items.blaze_rod, 1.0F, 0, 1));

        EntityDefinition ghast = mob("ghast", "Ghast");
        ghast.addDrop(drop(Items.ghast_tear, 1.0F, 0, 1));
        ghast.addDrop(drop(Items.gunpowder, 1.0F, 0, 2));

        EntityDefinition witch = mob("witch", "Witch");
        witch.addDrop(drop(Items.glowstone_dust, 0.125F, 0, 2));
        witch.addDrop(drop(Items.redstone, 0.125F, 0, 2));
        witch.addDrop(drop(Items.spider_eye, 0.125F, 0, 2));
        witch.addDrop(drop(Items.sugar, 0.125F, 0, 2));
        witch.addDrop(drop(Items.stick, 0.125F, 0, 2));
        witch.addDrop(drop(Items.gunpowder, 0.125F, 0, 2));
        witch.addDrop(drop(Items.glass_bottle, 0.125F, 0, 2));

        EntityDefinition enderman = mob("enderman", "Enderman");
        enderman.addDrop(drop(Items.ender_pearl, 1.0F, 0, 1));

        EntityDefinition zombiePigman = mob("zombie_pigman", "Zombie Pigman");
        zombiePigman.addDrop(drop(Items.rotten_flesh, 1.0F, 0, 2));
        zombiePigman.addDrop(drop(Items.gold_nugget, 0.25F, 0, 1));
        zombiePigman.addDrop(drop(Items.gold_ingot, 0.025F, 0, 1));

        EntityDefinition ironGolem = mob("iron_golem", "Iron Golem");
        ironGolem.addDrop(drop(Items.iron_ingot, 1.0F, 3, 5));
        ironGolem.addDrop(drop(Blocks.red_flower, 0.2F, 0, 2));

        EntityDefinition snowGolem = mob("snow_golem", "Snow Golem");
        snowGolem.addDrop(drop(Items.snowball, 1.0F, 0, 15));

        EntityDefinition wither = mob("wither", "Wither");
        wither.addDrop(drop(Items.nether_star, 1.0F, 1, 1));

        EntityDefinition dragon = mob("ender_dragon", "Ender Dragon");
        dragon.addDrop(drop(new ItemStack(Blocks.dragon_egg), 1.0F, 1, 1));
        dragon.addDrop(drop(Items.ender_pearl, 1.0F, 4, 8));

        EntityDefinition sheep = mob("sheep", "Sheep");
        sheep.addDrop(drop(Blocks.wool, 1.0F, 1, 1));

        EntityDefinition cow = mob("cow", "Cow");
        cow.addDrop(drop(Items.leather, 1.0F, 0, 2));
        cow.addDrop(drop(Items.beef, 1.0F, 1, 3));

        EntityDefinition mooshroom = mob("mooshroom", "Mooshroom");
        mooshroom.addDrop(drop(Items.leather, 1.0F, 0, 2));
        mooshroom.addDrop(drop(Items.beef, 1.0F, 1, 3));
        mooshroom.addDrop(drop(Blocks.red_mushroom, 0.3F, 0, 2));

        EntityDefinition pig = mob("pig", "Pig");
        pig.addDrop(drop(Items.porkchop, 1.0F, 1, 3));

        EntityDefinition chicken = mob("chicken", "Chicken");
        chicken.addDrop(drop(Items.feather, 1.0F, 0, 2));
        chicken.addDrop(drop(Items.chicken, 1.0F, 1, 1));

        EntityDefinition squid = mob("squid", "Squid");
        squid.addDrop(drop(Items.dye, 1.0F, 1, 3));

        EntityDefinition horse = mob("horse", "Horse");
        horse.addDrop(drop(Items.leather, 1.0F, 0, 2));

        EntityDefinition villager = mob("villager", "Villager");
        villager.addDrop(drop(Items.emerald, 0.2F, 0, 2));
    }

    /** Modded entities, registered only when the corresponding mod is present. */
    public static void registerOptional() {
        if (Loader.isModLoaded("Botania")) {
            Item resource = GameRegistry.findItem("Botania", "manaResource");
            if (resource != null) {
                EntityDefinition gaia = mob("gaia", "Gaia");
                gaia.addDrop(drop(new ItemStack(resource, 1, 5), 1.0F, 1, 2));
                gaia.addDrop(drop(new ItemStack(resource, 1, 4), 0.5F, 1, 1));
            }
        }

        if (Loader.isModLoaded("alfheim")) {
            Item head = GameRegistry.findItem("alfheim", "FlugelHead");
            if (head != null) {
                EntityDefinition flugel = mob("flugel", "Flügel");
                flugel.addDrop(drop(new ItemStack(head), 1.0F, 1, 1));
            }
        }

        if (Loader.isModLoaded("DraconicEvolution")) {
            Item shard = GameRegistry.findItem("DraconicEvolution", "chaosShard");
            Item dust = GameRegistry.findItem("DraconicEvolution", "draconiumDust");
            if (shard != null || dust != null) {
                EntityDefinition chaosDragon = mob("chaos_dragon", "Chaos Dragon");
                if (shard != null) chaosDragon.addDrop(drop(new ItemStack(shard), 1.0F, 1, 2));
                if (dust != null) chaosDragon.addDrop(drop(new ItemStack(dust), 1.0F, 4, 8));
            }
        }
    }

    private static EntityDefinition mob(String id, String displayName) {
        return register(new EntityDefinition(id, displayName, 200));
    }

    private static EntityDrop drop(Item item, float chance, int min, int max) {
        return new EntityDrop(new ItemStack(item), chance, min, max);
    }

    private static EntityDrop drop(net.minecraft.block.Block block, float chance, int min, int max) {
        return new EntityDrop(new ItemStack(block), chance, min, max);
    }

    private static EntityDrop drop(ItemStack stack, float chance, int min, int max) {
        return new EntityDrop(stack, chance, min, max);
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
