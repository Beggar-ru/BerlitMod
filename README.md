# Berlit

Мод для **Minecraft 1.7.10 (Forge)** под сборку **GTNH** (GTNHGradle, RetroFuturaGradle, Java 25 для сборки, Java 8 для запуска).

## Возможности

- **Молекулярный трансформер** — 6 входов / 6 выходов / 4 улучшения, переработка предметов за EU.
- **Репликатор** — сканирование предмета прямо в слоте (без кристаллов памяти), память шаблонов,
  жидкая UU-материя IC2 и выпуск целым стаком; стоимость материи берётся из IC2, включая
  стандартные крафты. Поддерживаются NEI и CraftTweaker.
- **Спавнер сущностей** (Tier 1 и Tier 2) — карты мобов, дроп с шансами, 7 слотов ядер.
  Tier 2: 10 слотов по 10 сущностей. 4 ядра Draconic Evolution и свои Spawner Core I–III,
  по одному ядру каждого вида.
- **Обрабатывающие механизмы** — дробитель, компрессор, экструдер (выдавливание), прокатный
  стан (прокатка), электропечь. Импортируют рецепты IC2, вход принимает только валидные ресурсы;
  у каждого металлического механизма своя операция и своя страница рецептов в NEI.
- **Улучшения** — IC2 Overclocker/Ejector/Pulling/Energy Storage/Transformer (принимаются в механизмах
  Berlit), свои Accelerator MK II/III (работают и в IC2-машинах) и Stack Upgrade (стак за операцию).
- **CraftTweaker** — рецепты трансформера, машин, репликатора, дропы и сущности спавнера.
- **NEI** — страницы рецептов машин, репликатора и дропов сущностей.

## Зависимости

- Обязательно: **IC2** (2.2.828 experimental).
- Опционально: CraftTweaker (MineTweaker3), NotEnoughItems, Draconic Evolution, Botania, Alfheim.
- **ExtraCells** нужен только для «Бесконечной ячейки жидкостей» Berlit; без него этот предмет
  не регистрируется. В dev-среду ExtraCells добавлен, чтобы ячейку было видно в креативе.
  Для репликатора ExtraCells не требуется — используется IC2-ячейка UU-материи или труба.

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
Machines.addRecipe("metal_extruder", <minecraft:iron_ingot>, <minecraft:iron_ore>, 300);
Machines.addRecipe("metal_roller", <minecraft:iron_plate>, <minecraft:iron_ore>, 300);

import mods.berlit.Spawner;
Spawner.addDrop("iron_golem", <minecraft:gold_ingot>, 0.5, 1, 2);
Spawner.addEntity("zombie", "Zombie", 200);

import mods.berlit.Replicator;
Replicator.addScan(<minecraft:diamond>, 45);   // mB жидкой материи на 1 предмет
Replicator.addPattern(<minecraft:emerald>, 120);
Replicator.removePattern(<minecraft:emerald>);
```

## Репликатор

`berlit:replicator` — механизм на основе репликатора IC2, но без «кристаллов памяти».

- **Сканирование**: положите предмет в слот сканирования — он сканируется в том же тике и
  запоминается в NBT машины. Предмет не расходуется. Отдельных кристаллов/дисков нет.
- **Память шаблонов**: машина хранит до 1024 отсканированных предметов; кнопки `<` и `>`
  переключают выбранный шаблон. Сканирование нового предмета сразу выбирает его.
- **Жидкая материя**: внутренний бак на 16 000 mB. Заливается IC2-ячейками UU-материи в слот
  жидкости (пустые ячейки возвращаются) или через любой `IFluidHandler`/трубу. «Бесконечная
  ячейка жидкостей» Berlit — это AE2-ячейка ExtraCells, а не контейнер UU-материи; для
  репликатора она не подходит.
- **Стоимость**: по умолчанию `UuIndex.getInBuckets(item) * 1000` mB за предмет — то есть
  стандартные значения IC2, включая ванильные/Forge/IC2-крафты и машинные рецепты. Если предмета
  нет в UU-графе, его можно добавить через CraftTweaker.
- **Выход**: одна операция всегда создаёт целый стак (`min(64, maxStackSize)`); затраты материи
  считаются как `стоимость за предмет × размер стака`. Если выходной слот заполнен, машина ждёт.
- **Энергия**: 32 EU/t базово, буфер 100 000 EU, tier 4; улучшения Overclocker/Accelerator
  ускоряют процесс, Ejector выталкивает готовый стак в соседние инвентари.
- **NEI**: нажмите R или U на предмете — страница покажет стоимость предмета и целого стака.
- **CraftTweaker**: `Replicator.addScan(item, mB)`, `addPattern`, `addRecipe`, `removePattern`,
  `removeRecipe`, `getMatter`. Аргумент — mB на один предмет, не на стак.

## Лицензия

MIT, см. [LICENSE](LICENSE).
