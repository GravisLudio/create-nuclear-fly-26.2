# Porting notes — Create Nuclear → Fabric 26.2 (Create Fly)

Working document for the port of **Create Nuclear** from NeoForge 1.21.1 to **Fabric / Minecraft
26.2**, on [Create Fly](https://github.com/ZurrTum/Create-Fly). Written to be picked up cold on
another machine. Read *State* and *Next steps* first; *Traps* before touching anything unfamiliar.

The method is the one proven on the Connected port (`C:\dev\create connected\PORTING.md`, repo
`GravisLudio/create-connected-fly`). That document is the playbook — most traps below were already
paid for there. Read its *Traps* and *The launch phase* sections before the first launch.

---

## State (2026-09-26, second session)

**Compiles and runs.** 246 javac errors → 0. The client launches, a world loads, and a Fabric
client game test (`gradlew runClientGameTest`, see *Testing*) builds a scene and screenshots it:
the four irradiated mobs (adults and babies), the mod's blocks, the anti-radiation suit on the
player (dyed), item icons, and a 5x5 reactor built from its own pattern, which **assembles**
("Reactor has been assembled") and **runs**: with uranium rods and water inserted through Fabric's
transfer API and a one-rod blueprint, the controller turns `ACTIVE` and reaches heat 67 after
100 ticks (all asserted in the test). A summoned `nuclear_explosion` carves its crater and draws
the mushroom cloud and the flash. The headless datapack check
(`gradlew runGametest`) loads 3716 recipes and 3044 advancements with **zero errors**.

Third session (2026-09-26): the test now also covers **rod consumption** (lifetime set to 100 ticks:
64 → 62 rods in 250 ticks), a **reactor output** placed on the running reactor (registers itself,
turns at heat / 32 = 2 RPM, shaft on top), a **reactor alarm** (powers when heat passes the danger
threshold) and the **meltdown**: with `size5Danger` lowered below the heat, the 300-tick countdown
ends in the controller destroying itself, a `nuclear_explosion`, the mushroom cloud and the biome
around turning `createnuclear:irradiated_land`. Configs are restored by the test.

Not yet exercised in game: the 7x7 and 9x9 reactors, the JEI categories, the Flywheel-off path, a dedicated server.

What this session changed, beyond the compile fixes listed below:

- **Mixins**: `SmithingTransformRecipeMixin` would have crashed at launch (26.2 `assemble` lost
  its `HolderLookup.Provider` argument; `result` is an `ItemStackTemplate`). All other mixin
  targets checked against the 26.2 sources. New: `client.AvatarRendererMixin` hides the skin's
  hat/jacket/sleeves/pants under the suit (was `RenderPlayerEvent.Pre`).
- **Fluid models**: registering the flowing fluid in `AllFluidConfigs.MODEL` crashed model
  baking — Create Fly's mixin adds `fluid.getFlowing()` itself. Only the source is registered.
- **Item model definitions** for all 95 items (`tools/gen-item-models.py`): the suit selects on
  the `cloth_color` component, the biome extractor uses a registered `createnuclear:biome_restore`
  range property (`client/CNItemModelProperties`). Upstream wrote predicates for both but never
  registered the properties, so neither ever worked there.
- **Recipes** migrated to 26.2 / Create Fly shapes (Connected's `migrate-recipes.js` +
  `tools/migrate-recipes-cn.py`), fluids in droplets.
- **Worldgen**: biome `carvers` is a list; the noise router's `initial_density_without_jaggedness`
  became `preliminary_surface_level` (rebuilt from vanilla's formula; `irradiated_noise` is used by
  no dimension, upstream included).
- **Advancements**: entity predicate `"type"` → `"entity_type"`.
- **Connected textures**: Create Fly reads one sprite per connection state
  (`<name>_connected/<i>.png`), not the 1.21.1 sheet. Split with `tools/split-ct.py` (verified
  pixel for pixel against Create Fly's own split). Before this, every assembled reactor rendered
  as missingno.
- **Armour**: Fabric `ArmorRenderer` (`AntiRadiationArmorRenderer`), one model instance per slot
  (draws are deferred, so upstream's per-render `currentSlot` field no longer works). The dyed
  suit item models use `textures/models/armor/`, added to the item atlas
  (`assets/minecraft/atlases/items.json`).
- **Transfer API**: reactor fluid and rod inputs published to `FluidStorage.SIDED` /
  `ItemStorage.SIDED` (`foundation/transfer/CNTransfer`).
- **JEI**: only the two fan categories (enriched, snow powder). Upstream's plugin was a copy of
  Create's; Create Fly's plugin already registers all of that.
- **Spawn eggs**: 26.2 has no two-colour egg template. The egg models reuse the vanilla egg of the
  same animal with a constant green tint — placeholder until the mod has its own egg textures.
- **Mobs**: babies now render at baby size (upstream's `renderToBuffer` overrides skipped the
  `AgeableListModel` scaling); wolf wet darkening works (upstream cast the shade to `int`, 0).
- `controller_panel.png` is missing from NeoForge V2; taken from upstream Forge.

### Known, left as is

- `createnuclear:alarm/reactor_alarm` sound has no `.ogg` — missing upstream too.
- A few models lack a `particle` texture (log warning only).
- Closing the client game test ends in a "Client shutdown from post-main" watchdog crash report
  (Flywheel worker threads). It happens after the test finished and every screenshot was taken.

### Done (first session)

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
- **JDK 25** is pinned in `gradle.properties` (`org.gradle.java.home`), currently
  `C:/Program Files/Eclipse Adoptium/jdk-25.0.4.7-hotspot` (the second machine; the first had
  `jdk-25.0.4.101-hotspot`). **Change it on a new machine.** `tools/jp.sh` uses `javap` from PATH.
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
  - Connected port: `C:\dev\create connected` (clone of GravisLudio/create-connected-fly).
  - **Decompiled Minecraft 26.2**: `C:\dev\_upstream\mc-26.2-src` (~7,000 classes). On the second
    machine `gradlew genSources` worked; unzip the resulting
    `.gradle/loom-cache/minecraftMaven/net/minecraft/minecraft-merged-*/26.2-rc-2/*-sources.jar`
    there. On the first, `libraries.minecraft.net` did not resolve (DNS), so it was made by running
    a cached Vineflower directly:

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
| `gen-item-models.py [--write]` | Writes `assets/createnuclear/items/<id>.json` for every top-level item model, with the suit's `cloth_color` select, the extractor's `biome_restore` range and the spawn egg tint. Re-run after adding an item. |
| `migrate-recipes-cn.py <dir> [--write]` | Recipe types Connected's `migrate-recipes.js` does not cover: single-input processing (`ingredient`), fluids (`fluid_ingredients` / `fluid_results`, x81), mechanical crafting keys, smelting, smithing. Already applied. |
| `split-ct.py <sheet> <omni/rectangle/kryppers> [--write]` | Splits a 1.21.1 connected-texture sheet into Create Fly's `<name>_connected/<i>.png` tiles. `--verify` checks a mapping against Create Fly's own split. Already applied to all five sheets. |
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
  inventory directly. For everything else they are published to Fabric's `FluidStorage.SIDED` /
  `ItemStorage.SIDED` by `foundation/transfer/CNTransfer` (cached storages, as Connected's `CCTransfer`).
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
- **Armour rendering**: Fabric `ArmorRenderer` (`AntiRadiationArmorRenderer`), texture chosen by
  `ClothTagHelper.getArmorTexturePath` (was a mixin on NeoForge's `getArmorTexture`). One model per
  slot: 26.2 poses models when the deferred submission is drawn, so per-draw mutable state on a
  shared model is wrong by then.
- **Mobs**: models are `EntityModel<...RenderState>` posed from vanilla's render states
  (`ChickenRenderState`, `WolfRenderState`, `CatRenderState`, `LivingEntityRenderState`); renderers
  are `AgeableMobRenderer` with a baby layer baked through `BabyModelTransform` (same numbers as the
  old `AgeableListModel` defaults). Sounds come from vanilla's classic sound sets.
- **Config**: catnip `Builder.create` (client/common/server). The entity blacklist is a
  comma-separated string (catnip has no list values).

## Deliberate divergences from upstream

- `CNAdvancementBehaviour.award` uses the **Forge** condition `!advancement.isAlreadyAwardedTo(player)`.
  NeoForge V2 had `!advancements.contains(advancement)`, which never awards any registered advancement.
- Fluid world tint omitted (textures are already coloured; Create Fly resolves world tint via
  Fabric fluid variant rendering). Fluid physics (viscosity, swim, drown) have no vanilla equivalent.
- Spawn eggs: vanilla egg of the same animal, tinted green in the item definition — **own textures
  still needed** (26.2 eggs are one texture per entity, no two-colour template).
- JEI: only the two fan categories (upstream's plugin duplicated Create's own).
- Irradiated babies render at baby size, and the wolf darkens when wet (both broken upstream).
- Suit colour and extractor charge item models work (upstream never registered their properties).
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
- **Connected textures**: Create Fly wants `<name>_connected/<i>.png`, not a sheet. A sheet gives
  missingno on every face that has a neighbour; a lone block looks fine, so it hides until a
  multiblock is built. The horizontal-kryppers tile order is not the one the index functions
  suggest — trust `split-ct.py --verify`, not the code.
- **Item atlas**: an item model texture outside `textures/item/` (the suit's `textures/models/armor`)
  is missingno until a source is added to `assets/minecraft/atlases/items.json`.
- **Fluid model registry**: register only the source fluid in `AllFluidConfigs.MODEL`.
- Removed/renamed in 26.2, met this session: `ResourceKey.location()` -> `identifier()`;
  `Inventory.items/offhand`, `getArmorSlots()` -> `getNonEquipmentItems()` + `getItemBySlot`;
  `Explosion` is abstract (`ServerExplosion(level, source, damageSource, calculator, center, r, fire, interaction)`);
  `BlockState.onBlockExploded` -> `onExplosionHit(level, pos, explosion, onHit)`; `hurt` -> `hurtServer`;
  `MobSpawnType` -> `EntitySpawnReason`; `convertTo(type, bool)` -> `convertTo(type, ConversionParams, after)`;
  `MobEffects.DAMAGE_BOOST/CONFUSION` -> `STRENGTH/NAUSEA`; `NeutralMob` keeps an anger *end time*
  (`long`) and an `EntityReference` target; `ChunkPos` is a record (`x()`, `z()`);
  `MinecraftServer.tell` -> `schedule`; `Options.hideGui` -> `mc.gui.hud.isHidden()`;
  `getMaxStackSize(ItemStack)` -> the `MAX_STACK_SIZE` component; `EntityType.create(level)` ->
  `create(level, EntitySpawnReason)`; `Level.getTimeOfDay` -> environment attributes
  (`CAT_WAKING_UP_GIFT_CHANCE`); `TemptGoal` takes a `Predicate<ItemStack>`.
- Data formats met this session: biome `carvers` is a list; noise router
  `preliminary_surface_level`; entity predicates are keyed by sub-predicate type
  (`"type"` -> `"entity_type"`); ingredient alternatives are a list of id strings.

---

## Testing

- `gradlew runGametest`: headless server (`build/gametest`) that loads every datapack and exits.
  Grep its log for `ERROR` / `Couldn't parse`. No EULA needed.
- `gradlew runClientGameTest`: Fabric client game test in `src/gametest` (own source set, not
  packaged). Opens a window, creates a world, builds the scene, writes screenshots to
  `build/run/clientGameTest/screenshots/`, and fails on: the 5x5 reactor not assembling, a
  connected-texture sprite missing from the atlas, the transfer API refusing rods or water, or the
  fuelled reactor not turning `ACTIVE`. The last screenshot does not frame the controller yet (its
  open side depends on the pattern's orientation). Look at the screenshots: models, textures and
  poses are not asserted. It ends with a harmless shutdown-watchdog crash report (see *Known*).
- `gradlew runClient`: the normal dev client (`run/`).

## Next steps, in order

1. **Play-test the reactor further**: the 7x7 and 9x9 sizes; the blueprint menu by hand. (Rod
   consumption, output, alarm and meltdown are covered by steps 6b and 8 of `src/gametest`.)
   Note for the test: the controller reads the blueprint from **both** inventory slot 0 and
   `configuredPattern` (same stack, as `ReactorControllerBlock.useItemOn` does); setting only the
   pattern crashes the server in `DefaultHeatCalculator.computeHeat`.
2. JEI categories (enriched / snow powder) with JEI installed; Ponder scenes; goggles tooltips;
   display link sources; blueprint menu.
3. Dedicated server launch (`runServer` needs `eula.txt` accepted by you): check no client class
   is reached server-side.
4. Flywheel-off path (Connected's *Testing*), the recipe auditor
   (`C:\dev\recipe-auditor\audit-modpack.ps1`).
5. Own spawn egg textures; `reactor_alarm.ogg` (missing upstream).
