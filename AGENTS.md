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
  network/               — SimpleNetworkWrapper-пакеты (синхронизация GUI)
  compat/                — CraftTweaker, NEI
tools/                   — генераторы текстур/GUI и превью (Python + Pillow)
```

Соглашения:

- Вся текстовая локализация — `assets/berlit/lang/en_US.lang` **и** `ru_RU.lang`.
- Текстуры: блоки — `textures/blocks`, GUI — `textures/gui`, предметы — `textures/items`.
  Генераторы: `tools/gen_*.py`; после изменения координат слотов синхронизируйте GUI-класс,
  `Container` и PNG. Фон слота рисуется на `slotX-1/slotY-1`.
- Сетевые пакеты: `PacketMachineSync` (server→client), `PacketSpawnerSync`.
- Энергия: IC2 `IEnergySink`, tier 4, буфер 8000 (+ energy storage upgrades), приём без лимита EU/t.
- Улучшения: `UpgradeHelper.get(stack)` — IC2-модули + Accelerator MK II/III + Stack Upgrade;
  `UpgradeRegistry` — ядра спавнера. Скорость только у обработчиков; спавнер — только ядра.

## Что реализовано

- Креативная вкладка, молекулярный трансформер (6+6+4 слота, пакетная переработка,
  энергия, ejector/pulling/overclocker/accelerators/stack upgrade).
- Спавнер T1 (1 сущность) и T2 (10 слотов ×10), дропы с шансами, ядра DE + свои I–III,
  NEI-страница дропов, CraftTweaker `mods.berlit.Spawner`.
- Обработчики: дробитель, компрессор, металоформовочный (продавливание), прокатный стан,
  электропечь. 6+6+4 слота, импорт рецептов IC2, NEI, CraftTweaker `mods.berlit.Machines`.
- Вход обработчиков принимает только предметы с рецептом в текущем режиме.
- Ускорители Berlit реализуют IC2 `IUpgradeItem` — работают в IC2-машинах.
- **Super Chest** 27×9 слотов по 1024 предмета (`BlockSuperChest`, кастомный NBT `Count` int,
  широкий GUI 502×266, `ContainerSuperChest`/`GuiSuperChest`).

## Что делаем дальше (AE2 + Avaritia)

Задача: ME-сборщик под 9×9-рецепты Avaritia, кодировщик шаблонов, кодировщик
сингулярностей нейтронного компрессора.

Важные факты (проверено по исходникам):

- Avaritia 1.7.10: рецепты 9×9 — `fox.spiteful.avaritia.crafting.ExtremeCraftingManager`
  (`findMatchingRecipe(InventoryCrafting 9×9, World)`); нейтронный компрессор —
  `CompressorManager.getRecipes()`: `CompressorRecipe(input, cost, output)`.
- AE2 rv3 жёстко зашивает 3×3: `PatternHelper` и `CraftingTreeProcess` создают
  `new InventoryCrafting(..., 3, 3)`. Стандартный AE2-шаблон не может нести 9×9-сетку.

Варианты:

1. **Mixin в AE2** (`CraftingTreeProcess`) — разрешить N×N `InventoryCrafting` для нашего
   `ICraftingPatternDetails` с 81 входом; ME-сборщик на `IGridHost` + `ICraftingProvider`.
   Единственный путь к настоящему AE2-автокрафту 9×9, но правит чужой мод.
2. **Standalone-сборщик** — свой шаблон с 81 входом, 9×9-сетка, без AE2-автокрафта.
3. **Гибрид** — standalone сейчас, mixin позже.

Статус: **реализовано** (mixin выбран и внедрён):

- `mixins.berlit.json` + `MixinCraftingTreeProcess` / `MixinCraftingCPUCluster` — N×N инвентарь.
- `ItemAvaritiaPattern` / `AvaritiaPatternDetails` — 9×9 AE2-шаблон; кодировщик `berlit.pattern_encoder`.
- `TileEntityAvaritiaAssembler` — `IGridHost` + `ICraftingProvider` + `pushPattern` (`berlit.avaritia_assembler`).
- `ItemSingularityPattern` / `SingularityPatternDetails` + `berlit.singularity_encoder` — шаблоны компрессора.
- `TileEntityNeutronCompressor` — `CompressorManager`, слот шаблона, cost→singularity.

Проверено selftest'ом на сервере: 9×9-шаблон даёт 81 вход / 80 в конденсированном и алмаз на выходе;
шаблон сингулярности — cost 2900. Осталось прогнать полный цикл в игровом мире (GUI/ME-сеть).

## Полезные ссылки

- Avaritia: https://github.com/SpitefulFox/Avaritia/tree/1.7.10
- AE2 (1.7.10): https://github.com/AppliedEnergistics/Applied-Energistics-2/tree/rv3-1.7.10
- GTNH dev wiki: https://gtnh.miraheze.org/wiki/Development

- AE2 CPU получает конденсированные входы (≤9 типов; кодировщик отказывает при большем числе), а сборщик восстанавливает полную сетку 9×9 из шаблона.

### Автотест машин

Запуск: $env:BERLIT_SELFTEST='1'; $env:JAVA_TOOL_OPTIONS='-Dfml.queryResult=confirm'; ./gradlew runServer запускает Selftest: добавляет тестовый 9×9-рецепт (81 железо → алмаз), прогоняет `pushPattern` и компрессор. Ожидаемые строки в логе:
`
BERLIT_SELFTEST assembler: pushed=true result=1xitem.diamond@0 inputs=1 condensed=1
BERLIT_SELFTEST compressor: cost=900 out=1xitem.avaritia_singularity@0 inputLeft=null
`
