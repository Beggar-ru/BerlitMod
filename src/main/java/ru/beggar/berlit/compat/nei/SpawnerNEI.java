package ru.beggar.berlit.compat.nei;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import codechicken.nei.PositionedStack;
import codechicken.nei.api.API;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.TemplateRecipeHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ru.beggar.berlit.item.ItemEntityCard;
import ru.beggar.berlit.spawner.EntityDefinition;
import ru.beggar.berlit.spawner.EntityDrop;
import ru.beggar.berlit.spawner.EntityRegistry;

@SideOnly(Side.CLIENT)
public class SpawnerNEI extends TemplateRecipeHandler {

    public static void register() {
        API.registerRecipeHandler(new SpawnerNEI());
        API.registerUsageHandler(new SpawnerNEI());
    }

    public class CachedSpawnerRecipe extends CachedRecipe {

        private final PositionedStack input;
        private final List<PositionedStack> outputs = new ArrayList<PositionedStack>();
        private final List<EntityDrop> drops = new ArrayList<EntityDrop>();

        public CachedSpawnerRecipe(EntityDefinition definition) {
            this.input = new PositionedStack(ItemEntityCard.create(definition.getId()), 30, 20);

            int index = 0;
            for (EntityDrop drop : definition.getDrops()) {
                PositionedStack stack = new PositionedStack(
                    drop.getStack(),
                    62 + (index % 5) * 20,
                    20 + (index / 5) * 22);

                outputs.add(stack);
                drops.add(drop);
                index++;
            }
        }

        @Override
        public PositionedStack getResult() {
            return outputs.isEmpty() ? input : outputs.get(0);
        }

        @Override
        public List<PositionedStack> getIngredients() {
            return Collections.singletonList(input);
        }

        @Override
        public List<PositionedStack> getOtherStacks() {
            return outputs;
        }
    }

    @Override
    public String getRecipeName() {
        return StatCollector.translateToLocal("nei.berlit.spawner");
    }

    @Override
    public String getGuiTexture() {
        return "berlit:textures/gui/nei_spawner.png";
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if ("item".equals(outputId) && results.length > 0 && results[0] instanceof ItemStack) {
            loadCraftingRecipes((ItemStack) results[0]);
        }
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        if (result == null) return;

        for (EntityDefinition definition : EntityRegistry.all()) {
            for (EntityDrop drop : definition.getDrops()) {
                if (drop.getStack()
                    .getItem() == result.getItem()
                    && drop.getStack()
                        .getItemDamage() == result.getItemDamage()) {
                    arecipes.add(new CachedSpawnerRecipe(definition));
                    break;
                }
            }
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        if (ingredient == null || !(ingredient.getItem() instanceof ItemEntityCard)) return;

        EntityDefinition definition = EntityRegistry.get(ItemEntityCard.getEntityId(ingredient));
        if (definition != null) {
            arecipes.add(new CachedSpawnerRecipe(definition));
        }
    }

    @Override
    public List<String> handleItemTooltip(GuiRecipe<?> gui, ItemStack stack, List<String> currentTip, int recipe) {
        if (stack != null && recipe >= 0 && recipe < arecipes.size()) {
            CachedSpawnerRecipe cached = (CachedSpawnerRecipe) arecipes.get(recipe);
            for (EntityDrop drop : cached.drops) {
                if (drop.getStack()
                    .isItemEqual(stack)) {
                    currentTip.add(
                        StatCollector.translateToLocalFormatted(
                            "nei.berlit.spawner.chance",
                            Math.round(drop.getChance() * 100.0F)));
                    break;
                }
            }
        }
        return currentTip;
    }
}
