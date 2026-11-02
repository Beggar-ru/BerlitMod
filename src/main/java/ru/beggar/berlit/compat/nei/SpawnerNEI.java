package ru.beggar.berlit.compat.nei;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import codechicken.nei.PositionedStack;
import codechicken.nei.api.API;
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

        public CachedSpawnerRecipe(EntityDefinition definition) {
            this.input = new PositionedStack(ItemEntityCard.create(definition.getId()), 30, 20);

            int index = 0;
            for (EntityDrop drop : definition.getDrops()) {
                PositionedStack stack = new PositionedStack(
                    drop.getStack(),
                    75 + (index % 3) * 22,
                    10 + (index / 3) * 22);
                stack.setChance((int) (drop.getChance() * 100.0F));
                outputs.add(stack);
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
    public void drawExtras(int recipe) {
        if (recipe < 0 || recipe >= arecipes.size()) return;
        CachedSpawnerRecipe cached = (CachedSpawnerRecipe) arecipes.get(recipe);
        Minecraft minecraft = Minecraft.getMinecraft();

        int y = 62;
        for (PositionedStack stack : cached.outputs) {
            minecraft.fontRenderer.drawString(stack.getChance() + "%", 10, y, 0x404040);
            y += 10;
        }
    }
}
