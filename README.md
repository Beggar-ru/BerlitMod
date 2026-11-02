# Berlit

Мод для **Minecraft 1.7.10 (Forge)** под сборку **GTNH** (GTNHGradle, RetroFuturaGradle, Java 25 для сборки, Java 8 для запуска).

## Возможности

- **Молекулярный трансформер** — 6 входов / 6 выходов / 4 улучшения, переработка предметов за EU.
- **Спавнер сущностей** (Tier 1 и Tier 2) — карты мобов, дроп с шансами, 7 слотов ядер.
  Tier 2: 10 слотов по 10 сущностей. Ядра DE и свои Spawner Core I–III.
- **Обрабатывающие механизмы** — дробитель, компрессор, металоформовочный (3 режима: продавливание,
  резка, прокатка), электропечь. Импортируют рецепты IC2, вход принимает только валидные ресурсы.
- **Улучшения** — IC2 Overclocker/Ejector/Pulling/Energy Storage/Transformer, свои Accelerator MK II/III
  (работают и в IC2-машинах) и Stack Upgrade (стак за операцию).
- **CraftTweaker** — рецепты трансформера, машин, дропы и сущности спавнера.
- **NEI** — страницы рецептов машин и дропов сущностей.

## Зависимости

- Обязательно: **IC2** (2.2.828 experimental).
- Опционально: CraftTweaker (MineTweaker3), NotEnoughItems, Draconic Evolution, Botania, Alfheim.

## Сборка

Нужен JDK 25 (Gradle-демон), Java 8 ставится тулчейном автоматически.

```bash
./gradlew build
```

Готовый jar: `build/libs/berlit-<версия>.jar`.

## CraftTweaker

```zenscript
import mods.berlit.MolecularTransformer;
MolecularTransformer.addRecipe(<minecraft:diamond>, <minecraft:emerald>, 2000);
MolecularTransformer.removeRecipe(<minecraft:diamond>);

import mods.berlit.Machines;
Machines.addRecipe("macerator", <minecraft:iron_ingot>, <minecraft:iron_ore>, 300);
Machines.addRecipe("metal_former", <minecraft:iron_ingot>, <minecraft:iron_ore>, 300, 1);

import mods.berlit.Spawner;
Spawner.addDrop("iron_golem", <minecraft:gold_ingot>, 0.5, 1, 2);
Spawner.addEntity("zombie", "Zombie", 200);
```

## Лицензия

MIT, см. [LICENSE](LICENSE).
