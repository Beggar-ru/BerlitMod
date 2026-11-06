# AGENTS.md — заметки для ИИ-разработчиков

Файл для второго соавтора (и любого агента), который продолжит работу над Berlit.
Здесь: как собрать, что уже есть, где что лежит и что осталось.

## Сборка

- JDK 25 — Gradle-демон (`.java-version` = 25), Java 8 — целевой тулчейн, ставится RFG.
- `./gradlew build` → `build/libs/berlit-<версия>.jar`. Версия берётся из git-тега.
- Запуск: `./gradlew runClient` / `runServer`. В dev-клиенте есть IC2, CraftTweaker, NEI
  (CodeChickenCore/Lib), **ExtraCells** (для `berlit:infinity_fluid_cell`) — см. `dependencies.gradle`, `libs/`.
- После правок Java: `./gradlew spotlessApply` (иначе `build` упадёт на spotlessJavaCheck).
- Окружение этой машины и восстановление с нуля — `DEV_ENVIRONMENT.md`. Коротко:
  `python tools\env\fetch_jdks.py`, `python tools\env\mirror.py`, затем
  `.\tools\dev-env.ps1 build`. Сборка требует полного доступа к файлам: jabel
  подключается к javac через именованный канал Windows, который песочница блокирует.

## Структура

```
src/main/java/ru/beggar/berlit/
  Berlit.java            — регистрация блоков/предметов, рецепты, CT/NEI-хуки
  block/                 — блоки (трансформер, спавнеры, обработчики, сундук)
  tileentity/            — тайлы (энергия, инвентари, прогресс, синхронизация)
  gui/                   — контейнеры и GUI (тёмная тема: BerlitGui/GuiTheme)
  machine/               — MachineType, MachineRecipe(s), импорт рецептов IC2
  ae2/                   — AE2-интеграция: шаблоны, сборщики, цифровые машины
  recipe/                — рецепты молекулярного трансформера и репликатора
  spawner/               — сущности, дропы, ядра
  item/                  — карты сущностей, ускорители, ядра, stack upgrade
  upgrade/               — UpgradeHelper (IC2-модули + свои ускорители)
  network/               — SimpleNetworkWrapper-пакеты (синхронизация GUI)
  compat/                — CraftTweaker, NEI
  util/                  — InventoryUtils, ReplicatorFluid
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
- Спавнер T1 (1 сущность) и T2 (10 слотов ×10), дропы с шансами, 7 слотов ядер:
  4 ядра Draconic Evolution + свои I–III (`ItemSpawnerCore`). DE-ядра ищутся по unlocalized
  имени стека (`item.draconicevolution:draconicCore`), поэтому DE остаётся опциональным.
  Действует правило «одно ядро каждого вида» (`TileEntitySpawnerBase.canAcceptUpgrade`),
  NEI-страница дропов, CraftTweaker `mods.berlit.Spawner`.
- Обработчики: дробитель, компрессор, **экструдер** (`metal_extruder`, выдавливание),
  **прокатный стан** (`metal_roller`, прокатка), электропечь. 6+6+4 слота, импорт рецептов
  IC2, NEI, CraftTweaker `mods.berlit.Machines`. У металлических машин операция задана типом
  (`MachineType.getDefaultMode()`), переключения режима нет; NEI показывает рецепты IC2
  отдельной страницей для экструдера и для прокатки.
- Вход обработчиков принимает только предметы с рецептом у этой машины.
- **Репликатор** (`berlit.replicator`, `TileEntityReplicator`): слот мгновенного сканирования
  без кристаллов, память до 1024 шаблонов, бак UU-жидкости 16 000 mB, выпуск целого стака
  (`matterPerItem × stackSize`), стоимость из IC2 `UuIndex` (стандартные крафты), NEI-страница
  и CraftTweaker `mods.berlit.Replicator`. Энергия: 32 EU/t, буфер 100 000 EU, tier 4.
  Для заливки — IC2-ячейка UU-материи или `IFluidHandler`; `infinity_fluid_cell` — это
  AE2-ячейка ExtraCells, к репликатору отношения не имеет.
- **Цифровые машины** (`berlit.digital_macerator/compressor/metal_extruder/metal_roller/
  electric_furnace`): `TileEntityDigitalMachine` реализует AE2 `ICraftingProvider` и при
  подключении к ME-сети сам публикует все рецепты своего `MachineType` как
  `DigitalPatternDetails` — без шаблонов и кодировщика. `MachineRecipe` хранит все варианты
  входов (ore dictionary) и все выходы; CT-рецепты имеют приоритет. Машина обрабатывает
  очередь (до 64), питается EU 50 000 или AE, поддерживает IC2/Berlit-улучшения, возвращает
  результат в ME-сеть. GUI — ME-статус, рецепты, очередь, прогресс, 4 слота улучшений.
- IC2-модули (overclocker/ejector/pulling/energy storage/transformer) кладутся в слоты
  улучшений: вид модуля определяется по unlocalized имени **стека** (`stack.getUnlocalizedName()`),
  потому что IC2 держит все модули в одном предмете и различает их только стеком.
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

Проверено: автокрафт 9×9 и создание сингулярностей работают в игре (подтверждено вручную —
терминал, ME Interface, шаблоны, нейтронный компрессор). Selftest на сервере страхует от
регрессий: 9×9-шаблон конденсируется, находится в `ExtremeCraftingManager` и выдаёт алмаз;
шаблон сингулярности забирает `cost` из `CompressorManager` и отдаёт сингулярность.

## Полезные ссылки

- Avaritia: https://github.com/SpitefulFox/Avaritia/tree/1.7.10
- AE2 (1.7.10): https://github.com/AppliedEnergistics/Applied-Energistics-2/tree/rv3-1.7.10
- GTNH dev wiki: https://gtnh.miraheze.org/wiki/Development

- AE2 CPU получает конденсированные входы (≤9 типов; кодировщик отказывает при большем числе), а сборщик восстанавливает полную сетку 9×9 из шаблона.

### Автотест машин

Запуск: $env:BERLIT_SELFTEST='1'; $env:JAVA_TOOL_OPTIONS='-Dfml.queryResult=confirm'; ./gradlew runServer запускает Selftest: добавляет тестовый 9×9-рецепт (81 железо → алмаз), прогоняет `pushPattern` и компрессор. Ожидаемые строки в логе:
`
BERLIT_SELFTEST upgrades: overclocker=true storage=true draconic=true chaotic=true
BERLIT_SELFTEST spawner cores: first=true duplicate=false other=true
BERLIT_SELFTEST replicator: matter=45 stack=64 perStack=2880 fluid=true override=7 restored=45 ic2=115
BERLIT_SELFTEST replicator machine: patterns=1 matter=45 tank=0 output=1xitem.diamond@0 progress=0
BERLIT_SELFTEST digital macerator: patterns=75 recipes=75
BERLIT_SELFTEST digital compressor: patterns=49 recipes=49
BERLIT_SELFTEST digital metal_extruder: patterns=10 recipes=10
BERLIT_SELFTEST digital metal_roller: patterns=14 recipes=14
BERLIT_SELFTEST digital electric_furnace: patterns=55 recipes=55
BERLIT_SELFTEST digital total: 203
BERLIT_SELFTEST digital processing: pending=1 output=2xitem.ingotIron@0 reloaded=true
BERLIT_SELFTEST assembler: pushed=true result=1xitem.diamond@0 inputs=1 condensed=1
BERLIT_SELFTEST compressor: cost=2400 out=1xitem.avaritia_singularity@0 inputLeft=null
BERLIT_SELFTEST optional items: infinityFluidCell=true
`

Тестовая площадка сборщика стоит на `(1000, 64, 1000)`; чанк там приходится грузить явно
(`world.getChunkFromBlockCoords`), иначе `setTileEntity` молча ничего не делает и `pushPattern`
рапортует ложный отказ. Сервер работает в `run/server`, поэтому `eula.txt` и `server.properties`
нужны именно там.

### Как кодировать 9×9 и запускать автокрафт

- GUI `berlit.pattern_encoder` — это **сетка 9×9 (81 слот)**, а не 3×3. Кнопка `E` кодирует
  текущую раскладку в `Avaritia Pattern` (81 вход + 1 выход). Предметы из сетки не расходуются.
- 3×3 в `berlit.avaritia_assembler` — это **слоты для готовых шаблонов**, а не сетка крафта.
- Автокрафт как в AE2: кодировщик → шаблон → **ME Interface** (положить шаблон туда) →
  `Avaritia Assembler` вплотную к интерфейсу → заказ в терминале. Сборщик можно использовать
  и автономно, положив шаблон в его собственные слоты.
- CraftTweaker: `mods.berlit.Avaritia.addCompressor(<out>, cost, <in>)` /
  `removeCompressor(<in>)`. NEI: страница нейтронного компрессора (input × cost → singularity),
  рецепты 9×9 показывает штатный NEI-хендлер Avaritia.
- Сборщик и компрессор реализуют `ICraftingMachine` — именно этот интерфейс AE2 ME Interface ищет у соседнего блока и вызывает при pushPattern. Поэтому шаблон кладётся в ME Interface, а машина должна стоять вплотную к нему.

- В GUI сборщика и компрессора есть кнопка `C`: машина сама вытягивает ингредиенты из ME-сети по шаблону и крафтит (без AE2 CPU/Interface). Машина должна быть подключена кабелем/каналом; шаблон — в её собственных слотах.
