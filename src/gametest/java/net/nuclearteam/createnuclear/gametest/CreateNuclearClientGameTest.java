package net.nuclearteam.createnuclear.gametest;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.world.TestWorldSave;
import net.nuclearteam.createnuclear.CNEffects;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import lib.multiblock.SimpleMultiBlockAislePatternBuilder;
import lib.multiblock.impl.IMultiBlockPattern;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.nuclearteam.createnuclear.CNDataComponents;
import net.nuclearteam.createnuclear.CNItems;
import net.nuclearteam.createnuclear.content.multiblock.bluePrintItem.PatternData;
import net.nuclearteam.createnuclear.content.multiblock.bluePrintItem.ReactorBluePrintData;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.nuclearteam.createnuclear.CNBlocks;
import net.nuclearteam.createnuclear.client.CNSpriteShifts;
import com.zurrtum.create.client.foundation.block.connected.CTSpriteShiftEntry;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.nuclearteam.createnuclear.content.multiblock.ReactorAssembler;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerBlockEntity;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.nuclearteam.createnuclear.CNEntityType;
import net.nuclearteam.createnuclear.content.multiblock.alarm.ReactorAlarm;
import net.nuclearteam.createnuclear.content.multiblock.output.ReactorOutputEntity;
import net.nuclearteam.createnuclear.infrastructure.config.CNConfigs;

import java.util.List;

/**
 * Smoke test in a real client: builds a small scene with the mod's content and takes a screenshot
 * of each part (build/run/clientGameTest/screenshots). It fails on crashes and missing registry
 * entries; what it shows (textures, models, animation poses) has to be looked at.
 */
public class CreateNuclearClientGameTest implements FabricClientGameTest {
    private static final String NS = "createnuclear";

    private static final List<String> BLOCKS = List.of(
        "reactor_casing", "reactor_frame", "reactor_controller", "reactor_core", "reactor_cooler",
        "reactor_rod_input", "reactor_fluid_input", "reactor_output", "reactor_alarm",
        "uranium_ore", "thorium_ore", "lead_ore", "nitrate_ore", "autunite", "steel_block",
        "lead_block", "reinforced_glass", "enriched_soul_soil", "enriching_campfire");

    @Override
    public void runTest(ClientGameTestContext context) {
        // Not try-with-resources: step 6c closes the world and reopens it from its save.
        TestSingleplayerContext singleplayer = context.worldBuilder().create();
        try {
            singleplayer.getClientLevel().waitForChunksRender();
            TestServerContext server = singleplayer.getServer();

            server.runCommand("time set noon");
            server.runCommand("weather clear");
            server.runCommand("gamemode creative @p");
            server.runCommand("fill -8 99 -8 12 99 16 minecraft:smooth_stone");
            server.runCommand("fill -8 100 -8 12 110 16 minecraft:air");

            // 1. The four irradiated mobs, adults and a baby of each, frozen in front of the player.
            server.runCommand("tp @p 2 100 -4 0 15");
            String[] mobs = {"irradiated_cow", "irradiated_chicken", "irradiated_wolf", "irradiated_cat"};
            for (int i = 0; i < mobs.length; i++) {
                int x = -2 + i * 2;
                server.runCommand("summon " + NS + ":" + mobs[i] + " " + x + " 100 3 {NoAI:1b,Rotation:[180f,0f]}");
                server.runCommand("summon " + NS + ":" + mobs[i] + " " + x + " 100 6 {NoAI:1b,Age:-24000,Rotation:[180f,0f]}");
            }
            context.waitTicks(20);
            context.takeScreenshot("createnuclear-mobs");

            // 2. A row of the mod's blocks.
            for (int i = 0; i < BLOCKS.size(); i++) {
                server.runCommand("setblock " + (-8 + i) + " 100 12 " + NS + ":" + BLOCKS.get(i));
            }
            server.runCommand("kill @e[type=!minecraft:player]");
            server.runCommand("tp @p 1 101 4 0 15");
            context.waitTicks(20);
            context.takeScreenshot("createnuclear-blocks");

            // 3. The suit on the player, seen from the front (dyed chestplate to check the cloth texture).
            server.runCommand("item replace entity @p armor.head with " + NS + ":default_anti_radiation_helmet");
            server.runCommand("item replace entity @p armor.chest with " + NS + ":default_anti_radiation_chestplate[" + NS + ":cloth_color=\"red\"]");
            server.runCommand("item replace entity @p armor.legs with " + NS + ":default_anti_radiation_leggings");
            server.runCommand("item replace entity @p armor.feet with " + NS + ":default_anti_radiation_boots");
            context.runOnClient(mc -> mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT));
            context.waitTicks(20);
            context.takeScreenshot("createnuclear-suit");
            context.runOnClient(mc -> mc.options.setCameraType(CameraType.FIRST_PERSON));

            // 4. Item icons: the first 36 of the mod's items in the inventory.
            server.runCommand("clear @p");
            // Dyed suit pieces first: they select a different model on the cloth_color component.
            server.runCommand("give @p " + NS + ":default_anti_radiation_helmet[" + NS + ":cloth_color=\"blue\"]");
            server.runCommand("give @p " + NS + ":default_anti_radiation_chestplate[" + NS + ":cloth_color=\"lime\"]");
            List<Identifier> items = context.computeOnClient(mc -> BuiltInRegistries.ITEM.keySet().stream()
                .filter(id -> id.getNamespace().equals(NS)).sorted().limit(34).toList());
            for (Identifier id : items) {
                server.runCommand("give @p " + id);
            }
            server.runCommand("gamemode survival @p");
            context.waitTicks(5);
            context.setScreen(() -> new InventoryScreen(Minecraft.getInstance().player)); // supplier runs on the client thread
            context.waitTicks(10);
            context.takeScreenshot("createnuclear-items");
            context.setScreen(() -> null);

            // 4b. The mod's own spawn egg textures (tools/gen-spawn-eggs.py), alone in the inventory.
            server.runCommand("clear @p");
            for (String egg : new String[] {"wolf", "cat", "chicken"}) {
                server.runCommand("give @p " + NS + ":" + egg + "_irradiated_spawn_egg");
            }
            context.waitTicks(5);
            context.setScreen(() -> new InventoryScreen(Minecraft.getInstance().player));
            context.waitTicks(10);
            context.takeScreenshot("createnuclear-spawn-eggs");
            context.setScreen(() -> null);

            // 5. The 5x5 reactor: the mod's own pattern, built block by block, then assembled the way
            //    placing the controller by hand does (ReactorControllerBlock.setPlacedBy).
            server.runCommand("clear @p");
            server.runCommand("gamemode creative @p");
            server.runCommand("fill -8 100 -8 12 110 16 minecraft:air");
            BlockPos controller = new BlockPos(2, 103, 6);
            IMultiBlockPattern reactor = ReactorPatterns.build(ReactorPatterns.REACTOR_5);
            server.runOnServer(s -> reactor.construct(s.overworld(), controller, (c, st) -> true));
            context.waitTicks(10);
            // The two casings beside the controller ('A' in "OA*AO") become a rod input and a fluid
            // input, so the assembled reactor registers them (findAndRegisterSpecialBlocks).
            List<BlockPos> inputs = server.computeOnServer(s -> Direction.Plane.HORIZONTAL.stream()
                .map(controller::relative)
                .filter(pos -> s.overworld().getBlockState(pos).is(CNBlocks.REACTOR_CASING.get()))
                .toList());
            if (inputs.size() != 2) {
                throw new AssertionError("Expected two casings beside the controller, found " + inputs);
            }
            BlockPos rodInput = inputs.get(0);
            BlockPos fluidInput = inputs.get(1);
            server.runOnServer(s -> {
                s.overworld().setBlockAndUpdate(rodInput, CNBlocks.REACTOR_ROD_INPUT.getDefaultState());
                s.overworld().setBlockAndUpdate(fluidInput, CNBlocks.REACTOR_FLUID_INPUT.getDefaultState());
            });
            context.waitTicks(2);
            boolean assembled = server.computeOnServer(s -> {
                ReactorAssembler.assemble(controller, s.overworld());
                return s.overworld().getBlockEntity(controller) instanceof ReactorControllerBlockEntity be && be.isAssembled();
            });
            server.runCommand("gamemode spectator @p");
            server.runCommand("tp @p -9 108 -8 facing 2 103 6");
            context.waitTicks(20);
            context.takeScreenshot("createnuclear-reactor");
            // Connected-texture sprites: a missing one renders the whole casing as missingno.
            //    Create Fly reads one sprite per connection state (<name>_connected/<i>.png, tools/split-ct.py).
            String missingCt = context.computeOnClient(mc -> {
                StringBuilder missing = new StringBuilder();
                for (CTSpriteShiftEntry entry : List.of(CNSpriteShifts.REACTOR_CASING, CNSpriteShifts.REACTOR_GLASS,
                    CNSpriteShifts.AUTUNITE_CAP, CNSpriteShifts.AUTUNITE_LAYERED, CNSpriteShifts.AUTUNITE_PILLAR)) {
                    for (int i = 0; i < entry.getType().getSpriteSize(); i++) {
                        if (entry.getType().replaceOriginal() || i > 0) {
                            Identifier name = entry.getTarget(i).contents().name();
                            if (name.equals(MissingTextureAtlasSprite.getLocation())) {
                                missing.append(entry.getType().getId()).append('/').append(i).append(' ');
                            }
                        }
                    }
                }
                return missing.toString();
            });
            server.runCommand("fill -6 100 12 -4 102 12 " + NS + ":reactor_casing");
            server.runCommand("tp @p -5 101 7 facing -5 101 12");
            context.waitTicks(10);
            context.takeScreenshot("createnuclear-casing-wall");
            if (!missingCt.isEmpty()) {
                throw new AssertionError("Connected-texture sprites missing from the atlas: " + missingCt);
            }
            if (!assembled) {
                throw new AssertionError("The 5x5 reactor built from its own pattern did not assemble");
            }

            // 5b. The 7x7 and 9x9 reactors, away from the rest: they must assemble with the right size.
            for (int size : new int[] {7, 9}) {
                BlockPos big = new BlockPos(-30, 110, size == 7 ? -25 : 25);
                IMultiBlockPattern bigPattern = ReactorPatterns.build(size == 7 ? ReactorPatterns.REACTOR_7 : ReactorPatterns.REACTOR_9);
                server.runOnServer(s -> bigPattern.construct(s.overworld(), big, (c, st) -> true));
                context.waitTicks(10);
                int bigSize = server.computeOnServer(s -> {
                    ReactorAssembler.assemble(big, s.overworld());
                    return s.overworld().getBlockEntity(big) instanceof ReactorControllerBlockEntity be && be.isAssembled()
                        ? be.getMultiblockSize() : -1;
                });
                System.out.println("[createnuclear-gametest] " + size + "x" + size + " reactor assembled with size " + bigSize);
                server.runCommand("tp @p " + (big.getX() + 16) + " " + (big.getY() + 10) + " " + (big.getZ() - 16)
                    + " facing " + big.getX() + " " + big.getY() + " " + big.getZ());
                context.waitTicks(20);
                context.takeScreenshot("createnuclear-reactor-" + size);
                if (bigSize != size) {
                    throw new AssertionError("The " + size + "x" + size + " reactor did not assemble (size " + bigSize + ")");
                }
            }

            // 6. Run it: uranium rods and water go in through Fabric's transfer API (CNTransfer), the
            //    controller gets a blueprint with one fuel rod, and it must turn ACTIVE and heat up.
            //    Rod lifetime at its minimum (100 ticks) so consumption shows within the test.
            int rodLifetime = server.computeOnServer(s -> {
                int old = CNConfigs.server().rods.uraniumRodLifetime.get();
                CNConfigs.server().rods.uraniumRodLifetime.set(100);
                return old;
            });
            long[] inserted = server.computeOnServer(s -> {
                ServerLevel level = s.overworld();
                long rods, water;
                try (Transaction tx = Transaction.openOuter()) {
                    Storage<ItemVariant> rodStorage = ItemStorage.SIDED.find(level, rodInput, null);
                    Storage<FluidVariant> fluids = FluidStorage.SIDED.find(level, fluidInput, null);
                    rods = rodStorage == null ? -1 : rodStorage.insert(ItemVariant.of(CNItems.URANIUM_ROD.get()), 64, tx);
                    water = fluids == null ? -1 : fluids.insert(FluidVariant.of(Fluids.WATER), FluidConstants.BUCKET * 8, tx);
                    tx.commit();
                }
                PatternData[] pattern = new PatternData[57];
                for (int i = 0; i < pattern.length; i++) {
                    pattern[i] = new PatternData(i, new ItemStack(Items.GLASS_PANE));
                }
                pattern[28] = new PatternData(28, new ItemStack(CNItems.URANIUM_ROD.get()));
                ItemStack blueprint = new ItemStack(CNItems.REACTOR_BLUEPRINT.get());
                blueprint.set(CNDataComponents.REACTOR_BLUE_PRINT_DATA, new ReactorBluePrintData(0, 1, pattern));
                // As ReactorControllerBlock.useItemOn: the same stack goes in inventory slot 0 and the pattern.
                ReactorControllerBlockEntity be = (ReactorControllerBlockEntity) level.getBlockEntity(controller);
                be.getInventoryObject().setItem(0, blueprint);
                be.setConfiguredPattern(blueprint);
                return new long[] {rods, water};
            });
            System.out.println("[createnuclear-gametest] inserted rods=" + inserted[0] + " water(droplets)=" + inserted[1]);
            context.waitTicks(100);
            String running = server.computeOnServer(s -> {
                ServerLevel level = s.overworld();
                ReactorControllerBlockEntity be = (ReactorControllerBlockEntity) level.getBlockEntity(controller);
                return "active=" + level.getBlockState(controller).getValue(ReactorControllerBlock.ACTIVE)
                    + " heat=" + be.getConfiguredPatternHeat();
            });
            System.out.println("[createnuclear-gametest] reactor after 100 ticks: " + running);

            server.runCommand("gamemode creative @p");
            server.runCommand("item replace entity @p armor.head with " + NS + ":default_anti_radiation_helmet");
            // Stand on the open side of the controller, looking at it: the helmet works as goggles.
            Direction outside = server.computeOnServer(sv -> java.util.Arrays.stream(Direction.values())
                .filter(d -> sv.overworld().getBlockState(controller.relative(d)).isAir())
                .findFirst().orElse(Direction.NORTH));
            BlockPos eye = controller.relative(outside, 3);
            server.runCommand("tp @p " + eye.getX() + " " + (eye.getY() - 1) + " " + eye.getZ()
                + " facing " + controller.getX() + " " + controller.getY() + " " + controller.getZ());
            context.waitTicks(20);
            context.takeScreenshot("createnuclear-reactor-running");
            if (inserted[0] <= 0 || inserted[1] <= 0) {
                throw new AssertionError("Transfer API refused the inputs: rods=" + inserted[0] + " water=" + inserted[1]);
            }
            if (!running.startsWith("active=true")) {
                throw new AssertionError("Reactor with fuel, water and a blueprint is not running: " + running);
            }

            // 6b. An output and an alarm on the top face ('A' cells of the first aisle), placed on the
            //     running reactor: they must register themselves (MultiblockHelpers.handleOnPlace), the
            //     output must turn at heat / RPM_DIVIDER, and the rod input must lose rods over time.
            BlockPos[] topParts = server.computeOnServer(s -> {
                ServerLevel level = s.overworld();
                ReactorControllerBlockEntity be = (ReactorControllerBlockEntity) level.getBlockEntity(controller);
                var box = be.getMultiblockPos();
                BlockPos output = new BlockPos((box.minX() + box.maxX()) / 2, box.maxY(), (box.minZ() + box.maxZ()) / 2);
                BlockPos alarm = output.east().getX() < box.maxX() ? output.east() : output.north();
                level.setBlockAndUpdate(output, CNBlocks.REACTOR_OUTPUT.getDefaultState().setValue(BlockStateProperties.FACING, Direction.UP));
                level.setBlockAndUpdate(output.above(), com.zurrtum.create.AllBlocks.SHAFT.defaultBlockState()
                    .setValue(BlockStateProperties.AXIS, Direction.Axis.Y));
                level.setBlockAndUpdate(alarm, CNBlocks.REACTOR_ALARM.getDefaultState());
                return new BlockPos[] {output, alarm};
            });
            context.waitTicks(150);
            String outputState = server.computeOnServer(s -> {
                ServerLevel level = s.overworld();
                ReactorControllerBlockEntity be = (ReactorControllerBlockEntity) level.getBlockEntity(controller);
                boolean registered = be.getOutputManager().getBlocksPosition(level).contains(topParts[0]);
                float speed = level.getBlockEntity(topParts[0]) instanceof ReactorOutputEntity out ? out.getGeneratedSpeed() : -1;
                long rods = 0;
                Storage<ItemVariant> rodStorage = ItemStorage.SIDED.find(level, rodInput, null);
                if (rodStorage != null) {
                    for (StorageView<ItemVariant> view : rodStorage) {
                        if (view.getResource().isOf(CNItems.URANIUM_ROD.get())) rods += view.getAmount();
                    }
                }
                return registered + " " + speed + " " + rods + " heat=" + be.getConfiguredPatternHeat();
            });
            System.out.println("[createnuclear-gametest] output registered/speed, rods left: " + outputState);
            server.runCommand("tp @p " + (topParts[0].getX() + 4) + " " + (topParts[0].getY() + 2) + " " + (topParts[0].getZ() + 4)
                + " facing " + topParts[0].getX() + " " + topParts[0].getY() + " " + topParts[0].getZ());
            context.waitTicks(10);
            context.takeScreenshot("createnuclear-reactor-output");
            String[] out = outputState.split(" ");
            if (!out[0].equals("true") || Float.parseFloat(out[1]) <= 0) {
                throw new AssertionError("Reactor output not registered or not turning: " + outputState);
            }
            if (Long.parseLong(out[2]) >= inserted[0]) {
                throw new AssertionError("No rod consumed after 250 ticks at a 100-tick lifetime: " + outputState);
            }

            // 6c. Save and quit with the reactor running, reopen the save: it must come back assembled,
            //     with its blueprint and rods, and keep running.
            String beforeSave = server.computeOnServer(s -> reactorState(s.overworld(), controller, rodInput));
            TestWorldSave save = singleplayer.getWorldSave();
            singleplayer.close();
            singleplayer = null;
            singleplayer = save.open();
            server = singleplayer.getServer();
            singleplayer.getClientLevel().waitForChunksRender();
            server.runOnServer(s -> CNConfigs.server().rods.uraniumRodLifetime.set(100));
            context.waitTicks(60);
            String afterLoad = server.computeOnServer(s -> reactorState(s.overworld(), controller, rodInput));
            System.out.println("[createnuclear-gametest] reactor before save: " + beforeSave + " | after reload: " + afterLoad);
            context.takeScreenshot("createnuclear-reactor-reloaded");
            if (!afterLoad.startsWith("assembled=true active=true blueprint=true") || afterLoad.contains("heat=0 ")
                || afterLoad.endsWith("rods=0")) {
                throw new AssertionError("Reactor did not survive a save and reload: " + beforeSave + " -> " + afterLoad);
            }

            // 6d. Radiation: uranium rods in the inventory give the radiation effect; the full suit
            //     (IRRADIATED_RESISTANCE 0.25 per piece) must keep it off entirely.
            server.runCommand("tp @p -6 100 -6");
            server.runCommand("clear @p");
            server.runCommand("effect clear @p");
            server.runCommand("gamemode survival @p");
            server.runCommand("give @p " + NS + ":uranium_rod 16");
            context.waitTicks(40);
            boolean irradiated = server.computeOnServer(s -> s.getPlayerList().getPlayers().getFirst().hasEffect(CNEffects.RADIATION));
            context.takeScreenshot("createnuclear-radiation");
            for (String piece : new String[] {"head:helmet", "chest:chestplate", "legs:leggings", "feet:boots"}) {
                String[] p = piece.split(":");
                server.runCommand("item replace entity @p armor." + p[0] + " with " + NS + ":default_anti_radiation_" + p[1]);
            }
            // Cleared only once the whole suit is on: with part of it the effect is still (re)applied.
            context.waitTicks(5);
            server.runCommand("effect clear @p");
            context.waitTicks(40);
            boolean irradiatedInSuit = server.computeOnServer(s -> s.getPlayerList().getPlayers().getFirst().hasEffect(CNEffects.RADIATION));
            System.out.println("[createnuclear-gametest] radiation without suit=" + irradiated + " with full suit=" + irradiatedInSuit);
            server.runCommand("clear @p");
            server.runCommand("effect clear @p");
            if (!irradiated || irradiatedInSuit) {
                throw new AssertionError("Radiation: without suit=" + irradiated + " (expected true), with suit=" + irradiatedInSuit + " (expected false)");
            }

            // 7. The nuclear explosion (ServerExplosion + onExplosionHit in 26.2) and its mushroom cloud,
            //    away from the reactor. A failure here crashes the integrated server and the test.
            server.runCommand("item replace entity @p armor.head with minecraft:air");
            server.runCommand("gamemode spectator @p");
            server.runCommand("fill 40 90 40 60 99 60 minecraft:stone");
            server.runCommand("tp @p 50 118 20 facing 50 100 50");
            context.waitTicks(10);
            server.runCommand("summon " + NS + ":nuclear_explosion 50 100 50");
            context.waitTicks(40);
            context.takeScreenshot("createnuclear-explosion");
            int craterAir = server.computeOnServer(sv -> {
                int air = 0;
                for (int x = 45; x <= 55; x++)
                    for (int z = 45; z <= 55; z++)
                        if (sv.overworld().getBlockState(new BlockPos(x, 97, z)).isAir()) air++;
                return air;
            });
            System.out.println("[createnuclear-gametest] explosion crater: " + craterAir + "/121 air blocks at y=97");
            if (craterAir == 0) {
                throw new AssertionError("The nuclear explosion destroyed nothing");
            }

            // 8. Meltdown of the running reactor: the danger threshold is dropped below its heat, so the
            //    alarm must power, and after the 300-tick countdown the controller must blow itself up.
            int danger = server.computeOnServer(s -> {
                int old = CNConfigs.server().reactorHeat.size5Danger.get();
                CNConfigs.server().reactorHeat.size5Danger.set(10);
                CNConfigs.server().rods.uraniumRodLifetime.set(rodLifetime);
                return old;
            });
            server.runCommand("tp @p " + (controller.getX() - 30) + " " + (controller.getY() + 20) + " " + (controller.getZ() - 30)
                + " facing " + controller.getX() + " " + controller.getY() + " " + controller.getZ());
            context.waitTicks(20);
            boolean alarmOn = server.computeOnServer(s -> {
                var state = s.overworld().getBlockState(topParts[1]);
                return state.is(CNBlocks.REACTOR_ALARM.get()) && state.getValue(ReactorAlarm.POWERED);
            });
            context.takeScreenshot("createnuclear-meltdown-alarm");
            context.waitTicks(300);
            context.takeScreenshot("createnuclear-meltdown");
            String meltdown = server.computeOnServer(s -> {
                ServerLevel level = s.overworld();
                CNConfigs.server().reactorHeat.size5Danger.set(danger);
                boolean controllerGone = !level.getBlockState(controller).is(CNBlocks.REACTOR_CONTROLLER.get());
                long explosions = level.getEntities(CNEntityType.NUCLEAR_EXPLOSION.get(), e -> true).size();
                return controllerGone + " " + explosions + " biome=" + level.getBiome(controller.above(5)).getRegisteredName();
            });
            System.out.println("[createnuclear-gametest] alarm=" + alarmOn + " meltdown (controller gone, explosions, biome): " + meltdown);
            context.waitTicks(60);
            context.takeScreenshot("createnuclear-meltdown-after");
            if (!alarmOn) {
                throw new AssertionError("Reactor alarm not powered while the reactor is in danger");
            }
            if (!meltdown.startsWith("true")) {
                throw new AssertionError("Reactor in danger for 300 ticks did not melt down: " + meltdown);
            }
        } finally {
            if (singleplayer != null) singleplayer.close();
        }
    }

    private static String reactorState(ServerLevel level, BlockPos controller, BlockPos rodInput) {
        if (!(level.getBlockEntity(controller) instanceof ReactorControllerBlockEntity be)) return "assembled=false (no controller)";
        long rods = 0;
        Storage<ItemVariant> rodStorage = ItemStorage.SIDED.find(level, rodInput, null);
        if (rodStorage != null) {
            for (StorageView<ItemVariant> view : rodStorage) {
                if (view.getResource().isOf(CNItems.URANIUM_ROD.get())) rods += view.getAmount();
            }
        }
        return "assembled=" + be.isAssembled()
            + " active=" + level.getBlockState(controller).getValue(ReactorControllerBlock.ACTIVE)
            + " blueprint=" + be.getInventoryObject().getItem(0).is(CNItems.REACTOR_BLUEPRINT.get())
            + " heat=" + be.getConfiguredPatternHeat() + " rods=" + rods;
    }
}
