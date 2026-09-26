# Porting notes — Create Nuclear → Fabric 26.2 (Create Fly)

Working document for the port of **Create Nuclear** from NeoForge 1.21.1 to **Fabric / Minecraft
26.2**, on [Create Fly](https://github.com/ZurrTum/Create-Fly). Written to be picked up cold on
another machine. Read *State* and *Next steps* first; *Traps* before touching anything unfamiliar.

The method is the one proven on the Connected port (`C:\dev\create connected\PORTING.md`, repo
`GravisLudio/create-connected-fly`). That document is the playbook — most traps below were already
paid for there. Read its *Traps* and *The launch phase* sections before the first launch.

---

## State (2026-09-26)

**Does not compile yet: 246 javac errors**, down from 1,977 at the first build. Everything outside the
list below compiles. Nothing has been launched; data (recipes, models, loot) has not been migrated.

Remaining errors by file (`javac` counts, from `build/compile.txt`):

| Errors | File | What it needs |
|---:|---|---|
| 46 + 22 | `compat/jei/*` | Port the JEI plugin to JEI 26.2 on Fabric (Connected has a working one: `compat/CreateConnectedJEI`). Create's JEI helpers (`CreateRecipeCategory`, `ProcessingViaFanCategory`, `AnimatedKinetics`) are in Create Fly's `client.compat.jei` — check names with `tools/jp.sh`. |
| ~130 | `content/contraptions/irradiated/**` | The four irradiated mobs (cat, wolf, chicken, cow): entities, models, renderers, goals. They are **copies of 1.21.1 vanilla** (`Cat`, `Wolf`, `Chicken`, `Cow` + `AgeableListModel`). Re-derive each from the 26.2 vanilla sources (see *Environment*: decompiled tree), keeping upstream's changes. 26.2 renderers use render states (`LivingEntityRenderState` subclasses), models take a state, not an entity. |
| 8 | `content/explosion/NuclearExplosionEntity.java` | `Explosion` is abstract now (`ServerExplosion`), `BlockState.onBlockExploded` is gone, save data is `ValueInput/ValueOutput` (`readAdditionalSaveData`/`addAdditionalSaveData`), `EntityType.is(tag)` → `getType().builtInRegistryHolder().is(tag)` or `type.is(tag)` via holder. |
| 8 | `content/equipment/armor/AntiRadiationArmor{Model,ClientExtensions}.java` | NeoForge `IClientItemExtensions` → Fabric `ArmorRenderer` (fabric-rendering-v1, already a dependency). Also needs `assets/createnuclear/equipment/anti_radiation_suit.json`. Upstream also hid the player's hat/jacket/sleeves/pants layers under the suit (`RenderPlayerEvent.Pre`, removed from `ClientEvents`) — redo via render-state mixin or `LivingEntityFeatureRendererRegistrationCallback`. |
| ~30 | small ones | See *Small fixes left* below. |

### Small fixes left (each 1–4 errors)

- `api/multiblock/fluid/ReactorFluidType`, `api/multiblock/rods/RodType`: `ResourceKey.location()` → `identifier()`; the `Object cannot be converted to String` follows from it.
- `foundation/utility/InventoryHashUtil`: `inventory.items`/`offhand`/`getArmorSlots()` → `getNonEquipmentItems()` + `getItemBySlot(EquipmentSlot...)` (same fix already done in `RadiationCapability`).
- `content/biome/BiomeIrradiationExtractorItem`: `appendHoverText` takes `(stack, context, TooltipDisplay, Consumer<Component>, flag)`; `getMaxStackSize(ItemStack)` no longer exists — stack size is the `MAX_STACK_SIZE` component, so set it on the stack when charged, or drop the override.
- `content/effects/VicinityEffect`: `applyEffectTick(ServerLevel, LivingEntity, int)`.
- `content/multiblock/bluePrintItem/ReactorBluePrintMenu`: `playerInventory.getSelected()` → `getSelectedItem()`.
- `BigFluidStack`, `CreateNuclearLang`, `ReactorGoggleTooltipRenderer`: Create Fly's `FluidStack.getHoverName()` is `getName()`.
- `content/radiation/CNRadiationValues`: `AllItems.CRUSHED_URANIUM` — Create Fly's id is `crushed_raw_uranium`; find the constant in `AllItems`.
- `foundation/events/overlay/RadiationOverlay`: `options.hideGui` moved; look it up in the decompiled `Options`/`Minecraft`.
- `foundation/utility/NotifyUtil`: `displayClientMessage` is gone — `sendOverlayMessage` on `ServerPlayer` (Create Fly uses it in `MenuProvider`).
- `infrastructure/worldgen/ConfigPlacementFilter`: a `.get()` on something that is no longer a supplier.
- `content/multiblock/controller/consumable/ConsumptionCycleManager`: `CompoundTag.getAllKeys()` → `keySet()`.
- `lib/multiblock/SimpleMultiBlockPattern`: `server.tell(new TickTask(...))` → `server.execute(...)`.
- `content/multiblock/output/ReactorOutputRenderer` / `client/CNBlockEntityRenders`: generic mismatch on `AllBlockEntityRenders.visual(...)` with `OrientedRotatingVisual.of(SHAFT_HALF)`; mirror how Create Fly registers `AllBlockEntityTypes.MOTOR`.
- `CNItems`: one leftover (check with `tools/errs.sh CNItems.java`).

### Done

Build and entrypoints; every registry (blocks, items, fluids, entities, block entities, menus,
particles, potions + brewing, effects, attributes, recipe types/serializers, data components,
attachments, display sources, sounds, creative tab, placement modifier, biome modifiers, dynamic
registries); reactor controller/input/output/frame block entities and blocks; fluid input with its
lock; rod input with menu and screen; blueprint item/menu/screen; saved data; advancements and
triggers; HUD overlays and nuke flash; camera shake; radiation heart sprite; particles including the
mushroom cloud; palettes; connected textures; goggle tooltips; display link sources and their
widgets; config.

---

## Environment

- **Repository**: `https://github.com/GravisLudio/create-nuclear-fly-26.2`, branch `main`.
  Remote `upstream` = `Create-Nuclear-Team/CreateNuclearNeoForge`; the port is based on its **`V2`
  branch at `cfc42ed`** (the `V2` branch is also pushed to origin for reference).
- **JDK 25** is pinned in `gradle.properties` (`org.gradle.java.home`) to
  `C:/Program Files/Eclipse Adoptium/jdk-25.0.4.101-hotspot`. **Change it on a new machine.**
- Gradle 9.4.1, Loom 1.16-SNAPSHOT, MC `26.2-rc-2`, Fabric API `0.152.0+26.2`,
  Create Fly `26.2-rc-2-6.0.9-1`, JEI Modrinth version id `x6nG9OT2` (compileOnly).
- **Run Gradle from PowerShell**, not Git Bash (Git Bash gives *Unable to establish loopback
  connection*). The error worklist:

  ```powershell
  .\gradlew.bat compileJava --continue --offline 2>&1 | Out-File -Encoding utf8 build\compile.txt
  Select-String -Path build\compile.txt -Pattern '^\s+[0-9,]+ errors' | Select-Object -Last 1
  ```

  javac prints each error twice in this log (streamed and in the failure summary); its own
  `N errors` total is exact. `--offline` works once dependencies are cached; adding a
  `fabricApi.module(...)` needs one online run.
- **Reference sources, outside the repo** (regenerate on the new machine):
  - Create Fly source: `C:\dev\create assistance\Create-Fly` (clone of ZurrTum/Create-Fly at the
    commit matching 6.0.9 / 26.2-rc-2). The single most useful thing — copy its answers.
  - Upstream clones: `C:\dev\_upstream\CN\{neo-v2, forge-main}` (NeoForge V2 = base, Forge `main`
    = the 2.0.1 release upstream keeps parity with).
  - **Decompiled Minecraft 26.2**: `C:\dev\_upstream\mc-26.2-src` (~7,000 classes). `gradlew
    genSources` failed here because `libraries.minecraft.net` did not resolve (DNS), so it was made
    by running a cached Vineflower directly:

    ```bash
    java -Xmx4G -jar vineflower-1.10.1.jar -dgs=1 -rsy=1 -rbr=1 -lit=1 -mpm=60 -log=WARN \
      "<repo>/.gradle/loom-cache/minecraftMaven/net/minecraft/minecraft-merged-*/26.2-rc-2/minecraft-merged-*.jar" \
      C:\dev\_upstream\mc-26.2-src
    ```

    On a machine with working DNS, `gradlew genSources` is simpler.

## Tools (`tools/`)

| Tool | Use |
|---|---|
| `map-imports.py <create-fly.jar> <src> [--write]` | Rewrites `com.simibubi.create` / `net.createmod.{catnip,ponder}` / `dev.engine_room.flywheel` imports against the Create Fly jar's real class list. Already applied. |
| `strip-datagen.pl`, `strip-tags.pl` | Remove Registrate datagen calls (`blockstate`, `model`, `recipe`, `loot`, …) and `.tag/.lang` from builder chains, balanced-paren aware. From the Connected port. Already applied. |
| `sweep.py <src> [--write]` | Mechanical 1.21.1→26.2 renames (isClientSide(), getRandom(), lookupOrThrow, ChunkPos.containing, heights…). Reports counts. |
| `nbt-sweep.py <src> [--write]` | `CompoundTag.getX("k")` → `getXOr("k", default)` etc., only for literal/constant keys in files importing `CompoundTag`. |
| `imp.py FILE... -d REGEX -a FQCN` | Drop/add imports. |
| `errs.sh <File.java>` | Errors of one file from `build/compile.txt`. |
| `jp.sh [-p] [-c] <class>` | `javap` against the **exact** compile classpath (`build/cp.txt`). Regenerate with `gradlew writeClasspath` (task in `build.gradle`) after dependency changes. Always check an API here before writing against it. |

---

## Architecture decisions

- **Registrate shim** `foundation/registrate/` — copied from the Connected port (`CCRegistrate` →
  `CNRegistrate`) plus `EntityEntry`. Eager registration against vanilla. Datagen methods are absent
  on purpose; tags/lang are no-ops (the generated JSON is committed in `src/generated/resources`).
- **Client wiring lives in `client/`** and runs from `CreateNuclearClient`: entity renderers + model
  layers, block entity renders/visuals, connected textures + casings, fluid models + fog, item
  tooltips (upstream's `setTooltipModifierFactory`), particles, menu screens, display source
  renders, goggle-tooltip behaviours. Registrate used to chain these; stripping them leaves no
  compiler error, so **if a client feature is silent, check it is registered here first**.
- **Fluids**: `content/fluids/NuclearFluidEntry` (still/flowing/block/bucket with upstream's per-fluid
  drop-off, tick rate, slope). Client model/fog in `client/CNFluidRenders` via Create Fly's
  `AllFluidConfigs`; fluid textures added to the block atlas in `assets/minecraft/atlases/blocks.json`.
  Lava/water + uranium → autunite through Create Fly's `FluidInteractionRegistry`.
- **Fluid units**: Create Fly tanks count **droplets (81/mB)**. The reactor's own logic stays in
  **millibuckets** as upstream wrote it; conversion happens only at the tank boundary
  (`FluidUnits`, `ReactorInputFluidManager.extractFluids`, `VirtualReactorInputFluid.addFluid`,
  `ReactorInputSnapshotBuilder`, tank capacities in `ReactorFluidInputEntity`). Keep it that way.
- **Capabilities → providers**: blocks implement Create Fly's `FluidInventoryProvider` /
  `ItemInventoryProvider` (`ReactorFluidInput`, `ReactorRodInput`); managers read the block entity's
  inventory directly. **Not yet done:** publishing them to Fabric's `FluidStorage.SIDED` /
  `ItemStorage.SIDED` for non-Create mods (Connected's `CCTransfer` shows how).
- **Fluid input lock**: upstream's `FilteredFluidHandler` became `ReactorFluidInputEntity.InputTank`
  (`isValid` = fill guard, `markDirty` = take/release the lock).
- **Menus** use Create Fly's own system (`foundation.gui.menu.MenuType<H>`, `MenuProvider.openHandledScreen`,
  client `AllMenuScreens` factories reading the holder from the packet).
- **Per-entity tick**: `foundation/mixin/LivingEntityMixin` (radiation at `tick` HEAD = upstream's
  `EntityTickEvent.Pre`; fluid effects at `baseTick` TAIL = `LivingVisibilityEvent`).
- **Irradiation resistance attribute on every living entity**: `LivingEntityAttributesMixin` on
  `createLivingAttributes` (was `EntityAttributeModificationEvent`).
- **Radiation data**: Fabric data attachment (`CNAttachmentTypes`), synced `targetOnly`; Fabric syncs
  on `setAttached`, so the player's copy is re-set each tick like upstream's `syncData`.
- **Block entity NBT**: `foundation/utility/NbtViews` merges a `CompoundTag` into / reads it from a
  `ValueOutput`/`ValueInput` at top level (`MapCodec.assumeMapUnsafe`), so the controller's managers
  keep their `CompoundTag` code and upstream's key layout.
- **Removal**: `onRemove` split in 26.2. Controller notification → `affectNeighborsAfterRemoval`
  (block entity already gone); dropping inventories → block entity `preRemoveSideEffects`.
- **Saved data**: codec-driven `SavedDataType` (`PersistentFluidLocks`, `PersistentIrradiatedZones`),
  same field layout as upstream.
- **Advancements**: runtime only (JSON committed); `<id>_builtin` triggers still registered — they
  are load-bearing. Trigger base copied from the Connected port.
- **Mushroom cloud**: custom `ParticleGroup` (`MushroomCloudParticleGroup`) submitting custom geometry;
  added to the engine by `foundation/mixin/client/ParticleEngineMixin` (same technique as Create
  Fly). The Tabula-style model classes no longer extend vanilla `EntityModel`.
- **Nuke flash** is a HUD element (upstream drew it with raw `RenderSystem` from a `GameRenderer`
  mixin that has no 26.2 equivalent). Camera shake: `CameraMixin` on `Camera.update`.
- **Palettes**: explicit registrations in `CNPaletteBlocks` (19 autunite blocks, names match the
  committed blockstates); the `PaletteBlockPattern` machinery was datagen and is deleted.
- **Worldgen**: ores via Fabric `BiomeModifications` (`CNBiomeModifiers`); the NeoForge
  `biome_modifier` JSONs were deleted. Worldgen bootstrap classes are excluded (JSON committed).
  `BiomeTagRule` excluded — no committed JSON uses it.
- **Config**: catnip `Builder.create` (client/common/server). The entity blacklist is a
  comma-separated string (catnip has no list values).

## Deliberate divergences from upstream

- `CNAdvancementBehaviour.award` uses the **Forge** condition `!advancement.isAlreadyAwardedTo(player)`.
  NeoForge V2 had `!advancements.contains(advancement)`, which never awards any registered advancement.
- Fluid world tint omitted (textures are already coloured; Create Fly resolves world tint via
  Fabric fluid variant rendering). Fluid physics (viscosity, swim, drown) have no vanilla equivalent.
- Spawn eggs lose their colours (26.2 eggs have per-entity textures — **textures still needed**).
- Creative tab order: items, buckets, blocks (upstream's 3D-model split read baked models).
- `withTabsBefore` gone; tab placed with `(null, -1)` like Create Fly.

## Traps met here (in addition to Connected's)

- Bash heredocs containing Java sometimes fail to parse in this Git Bash (*unexpected EOF while
  looking for matching `''*) and **nothing runs**. Use the Write tool or a Python script file.
- `EntityType.CHICKEN` etc. moved to **`EntityTypes`**.
- `ItemInteractionResult` → `InteractionResult` (`PASS_TO_DEFAULT_BLOCK_INTERACTION` →
  `TRY_WITH_EMPTY_HAND`); `InteractionResultHolder` gone.
- `Gui.renderHeart` → `Hud.extractHeart`, blit gained a leading `RenderPipeline`.
- `GameRenderer.darkenWorldAmount` → `bossOverlayWorldDarkening`; `getMainCamera()` → `mainCamera()`.
- Game rules: `level.getGameRules().get(GameRules.MOB_GRIEFING)` from `net.minecraft.world.level.gamerules`.
- `Potion` takes an explicit name now; passing the registry path keeps the lang keys valid.
- GUI colours are full ARGB — a colour without alpha draws nothing.

---

## Next steps, in order

1. **Finish compilation**: the small fixes, then armour client, explosion entity, JEI, the four mobs
   (use the decompiled vanilla classes side by side).
2. **Mixin audit before launching** (Connected's *The launch phase*): check every `@Mixin` target and
   method against the jar with `tools/jp.sh`; `C:\dev\_tools\mcbytecode\mixinverify.py` exists.
   Mixins today: `BaseFireBlockMixin`, `SmithingTransformRecipeMixin` (both unaudited),
   `LivingEntityMixin`, `LivingEntityAttributesMixin`, client `RadiationHeartMixin`,
   `CameraAccessor`, `GameRendererMixin`, `CameraMixin`, `ParticleEngineMixin`.
3. **Data migration** (`src/generated/resources` + `src/main/resources`): recipes to 26.2 shapes
   (Connected's `tools/migrate-recipes.js`; fan recipes `enriched`/`snow_powder` now take
   `"ingredient"` + `"results"`), loot tables, **item model definitions** (`assets/createnuclear/items/*.json`
   for every item; `cloth_color` and biome extractor `charge` overrides → `select`/`range_dispatch`),
   spawn egg textures, `equipment/anti_radiation_suit.json`, tag
   `createnuclear:repairs_anti_radiation_armor` (must list `createnuclear:lead_ingot`).
4. **Publish inventories to Fabric transfer API** (see *Architecture*).
5. `runClient` / `runServer`; then run the recipe auditor (`C:\dev\recipe-auditor\audit-modpack.ps1`).
6. Test the Flywheel-off path (Connected's *Testing*), the reactor at all three sizes, meltdown.
