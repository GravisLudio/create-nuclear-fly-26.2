package net.nuclearteam.createnuclear.gametest;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
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
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
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

            // 5. The 5x5 reactor: the mod's own pattern, built block by block, then assembled the way
            //    placing the controller by hand does (ReactorControllerBlock.setPlacedBy).
            server.runCommand("clear @p");
            server.runCommand("gamemode creative @p");
            server.runCommand("fill -8 100 -8 12 110 16 minecraft:air");
            BlockPos controller = new BlockPos(2, 103, 6);
            IMultiBlockPattern reactor = SimpleMultiBlockAislePatternBuilder.start()
                .aisle("OOOOO", "OAAAO", "OAAAO", "OAAAO", "OOOOO")
                .aisle("OABAO", "ADDDA", "BDCDB", "ADDDA", "OABAO")
                .aisle("OABAO", "ADDDA", "BDCDB", "ADDDA", "OABAO")
                .aisle("OABAO", "ADDDA", "BDCDB", "ADDDA", "OA*AO")
                .aisle("OABAO", "ADDDA", "BDCDB", "ADDDA", "OABAO")
                .aisle("OABAO", "ADDDA", "BDCDB", "ADDDA", "OABAO")
                .aisle("OOOOO", "OAAAO", "OAAAO", "OAAAO", "OOOOO")
                .where('A', b -> true).where('B', b -> true).where('C', b -> true)
                .where('D', b -> true).where('O', b -> true).where('*', b -> true)
                .block('A', () -> CNBlocks.REACTOR_CASING.getDefaultState())
                .block('B', () -> CNBlocks.REACTOR_FRAME.getDefaultState())
                .block('C', () -> CNBlocks.REACTOR_CORE.getDefaultState())
                .block('D', () -> CNBlocks.REACTOR_COOLER.getDefaultState())
                .block('O', () -> CNBlocks.REACTOR_CASING.getDefaultState())
                .block('*', () -> CNBlocks.REACTOR_CONTROLLER.getDefaultState())
                .build();
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

            // 6. Run it: uranium rods and water go in through Fabric's transfer API (CNTransfer), the
            //    controller gets a blueprint with one fuel rod, and it must turn ACTIVE and heat up.
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
        }
    }
}
