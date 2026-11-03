# AGENTS.md — заметки для ИИ-разработчиков

Файл для второго соавтора (и любого агента), который продолжит работу над Berlit.
Здесь: как собрать, что уже есть, где что лежит и что осталось.

## Сборка

- JDK 25 — Gradle-демон (`.java-version` = 25), Java 8 — целевой тулчейн, ставится RFG.
- `./gradlew build` → `build/libs/berlit-<версия>.jar`. Версия берётся из git-тега.
- Запуск: `./gradlew runClient` / `runServer`. В dev-клиенте есть IC2, CraftTweaker, NEI
  (CodeChickenCore/Lib) — см. `dependencies.gradle`, `libs/`.
- После правок Java: `./gradlew spotlessApply` (иначе `build` упадёт на spotlessJavaCheck).

## Структура

```
src/main/java/ru/beggar/berlit/
  Berlit.java            — регистрация блоков/предметов, рецепты, CT/NEI-хуки
  block/                 — блоки (трансформер, спавнеры, обработчики, сундук)
  tileentity/            — тайлы (энергия, инвентари, прогресс, синхронизация)
  gui/                   — контейнеры и GUI (тёмная тема: BerlitGui/GuiTheme)
  machine/               — MachineType, MachineRecipe(s), импорт рецептов IC2
  recipe/                — рецепты молекулярного трансформера
  spawner/               — сущности, дропы, ядра
  item/                  — карты сущностей, ускорители, ядра, stack upgrade
  upgrade/               — UpgradeHelper (IC2-модули + свои ускорители)
  network/               — SimpleNetworkWrapper-пакеты (синхронизация GUI, режим)
  compat/                — CraftTweaker, NEI
tools/                   — генераторы текстур/GUI и превью (Python + Pillow)
```

Соглашения:

- Вся текстовая локализация — `assets/berlit/lang/en_US.lang` **и** `ru_RU.lang`.
- Текстуры блоков — `assets/berlit/textures/blocks`, GUI — `.../gui`, предметы — `.../items`.
  Генераторы: `tools/gen_*.py`; после изменения координат слотов синхронизируйте
  GUI-класс, `Container` и PNG.
- Слоты в контейнере и на текстуре должны совпадать: фон слота рисуется на `slotX-1/slotY-1`.
- Сетевые пакеты: `PacketMachineSync` (server→client), `PacketSpawnerSync`, `PacketCycleMode`.
- Энергия: IC2 `IEnergySink`, tier 4, буфер 8000 (+ energy storage upgrades), приём без лимита EU/t.
- Улучшения: `UpgradeHelper.get(stack)` — IC2-модули + Accelerator MK II/III + Stack Upgrade;
  `UpgradeRegistry` — ядра спавнера. Скорость только у обработчиков; спавнер — только ядра.

## Что реализовано

- Креативная вкладка, молекулярный трансформер (6+6+4 слота, пакетная переработка,
  энергия, ejector/pulling/overclocker/accelerators/stack upgrade).
- Спавнер T1 (1 сущность) и T2 (10 слотов ×10), дропы с шансами, ядра DE + свои I–III,
  NEI-страница дропов, CraftTweaker `mods.berlit.Spawner`.
- Обработчики: дробитель, компрессор, металоформовочный (продавливание) и прокатный стан,
  электропечь. 6+6+4 слота, импорт рецептов IC2 (`Recipes.macerator` и т.д.), NEI,
  CraftTweaker `mods.berlit.Machines`.
- Вход обработчиков принимает только предметы с рецептом в текущем режиме.
- Ускорители Berlit реализуют IC2 `IUpgradeItem` — работают в IC2-машинах.

## Что делаем дальше (AE2 + Avaritia)

Задача: ME-сборщик под 9×9-рецепты Avaritia, кодировщик шаблонов, кодировщик
сингулярностей нейтронного компрессора, сундук 27×9×1024.

Важные факты (проверено по исходникам):

- Avaritia 1.7.10: рецепты 9×9 — `fox.spiteful.avaritia.crafting.ExtremeCraftingManager`
  (`findMatchingRecipe(InventoryCrafting 9×9, World)`), сингулярности — `addSingularityRecipe`
  (80× ресурс + 1 блок нейтрония в центре). Нейтронный компрессор —
  `CompressorManager.getRecipes()`: `CompressorRecipe(input, cost, output)`, cost — количество
  предметов (балансируется `Gregorizer.balanceCost`).
- AE2 rv3 жёстко зашивает 3×3: `PatternHelper` создаёт `new InventoryCrafting(..., 3, 3)`,
  `CraftingTreeProcess` тоже. Стандартный AE2-шаблон физически не может нести 9×9-сетку.

Варианты реализации (нужно решение):

1. **Mixin в AE2** (`CraftingTreeProcess`, `PatternHelper`) — разрешить N×N `InventoryCrafting`
   и наш `ICraftingPatternDetails` с 81 входом; ME-сборщик реализует `IGridHost` +
   `ICraftingProvider`, шаблоны кодируются нашим кодировщиком в формат AE2 (`in`/`out` NBT).
   Это единственный путь к настоящему AE2-автокрафту 9×9, но правит чужой мод.
2. **Standalone-сборщик** (без ME-автокрафта): свой шаблон-предмет с 81 входом, машина
   с 9×9-сеткой, ME-интерфейс только для ввода/вывода предметов.
3. **Гибрид**: standalone сейчас, mixin-путь позже.

Текущее решение: ждём подтверждения на mixin (вариант 1/3). После этого:

- `AvaritiaPattern` (ICraftingPatternItem, 9×9 details) + кодировщик.
- `TileEntityAvaritiaAssembler` (grid node, ICraftingProvider, pushPattern).
- `TileEntityNeutronCompressor` (cost→singularity, рецепты из `CompressorManager`).
- `TileEntitySuperChest` 27×9 по 1024 предмета (широкий GUI, кастомный NBT `Count` int,
  т.к. `ItemStack.writeToNBT` пишет Count байтом и 1024 не влезает).

## Полезные ссылки

- Avaritia: https://github.com/SpitefulFox/Avaritia/tree/1.7.10
- AE2 (ветка 1.7.10): https://github.com/AppliedEnergistics/Applied-Energistics-2/tree/rv3-1.7.10
- GTNH dev wiki: https://gtnh.miraheze.org/wiki/Development
