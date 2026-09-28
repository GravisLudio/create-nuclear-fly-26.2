package net.nuclearteam.createnuclear.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.nuclearteam.createnuclear.CNBlocks;
import net.nuclearteam.createnuclear.CNDataComponents;
import net.nuclearteam.createnuclear.CNItems;
import net.nuclearteam.createnuclear.content.multiblock.ReactorAssembler;
import net.nuclearteam.createnuclear.content.multiblock.bluePrintItem.PatternData;
import net.nuclearteam.createnuclear.content.multiblock.bluePrintItem.ReactorBluePrintData;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerBlock;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerBlockEntity;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.content.kinetics.fan.processing.FanProcessing;
import com.zurrtum.create.content.kinetics.fan.processing.FanProcessingType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.nuclearteam.createnuclear.content.kinetics.fan.processing.CNFanProcessingTypes;

import java.util.ArrayList;
import java.util.List;

/**
 * Server game tests (`gradlew runGameTest`): run on a dedicated server, so they also catch client
 * classes reached from common code. Each reactor size is built from its own pattern, gets a rod input
 * and a fluid input on the controller's face, is fuelled through Fabric's transfer API and given a
 * one-rod blueprint, and must turn ACTIVE and produce heat.
 */
public class CreateNuclearServerGameTest {

    /** Both fan types on a dropped item, through Create's own FanProcessing path (as an air current does). */
    @GameTest(maxTicks = 40)
    public void fanTypesProcessItems(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        StringBuilder report = new StringBuilder();
        fanCase(level, pos, CNFanProcessingTypes.SNOW_POWDER, CNItems.NITROGEN_CONCENTRATE.get(), 4, report);
        fanCase(level, pos, CNFanProcessingTypes.ENRICHED, CNItems.YELLOWCAKE.get(), 4, report);
        System.out.println("[createnuclear-gametest] fan processing: " + report);
        helper.assertTrue(!report.toString().contains("FAIL"), "Fan processing: " + report);
        helper.succeed();
    }

    private static void fanCase(ServerLevel level, BlockPos pos, FanProcessingType type, net.minecraft.world.item.Item input, int count, StringBuilder report) {
        ItemStack stack = new ItemStack(input, count);
        boolean canProcess = type.canProcess(stack, level);
        java.util.List<ItemStack> direct = type.process(stack.copy(), level);
        ItemEntity entity = new ItemEntity(level, pos.getX() + .5, pos.getY(), pos.getZ() + .5, stack);
        level.addFreshEntity(entity);
        int calls = 0;
        boolean done = false;
        while (calls < 2000 && !done) {
            calls++;
            done = FanProcessing.applyProcessing(entity, type);
        }
        String result = entity.getItem().getCount() + "x" + BuiltInRegistries.ITEM.getKey(entity.getItem().getItem());
        boolean ok = canProcess && direct != null && done && !entity.getItem().is(input);
        report.append(ok ? "ok " : "FAIL ").append(input).append(": canProcess=").append(canProcess)
            .append(" process=").append(direct).append(" done=").append(done).append(" after ").append(calls)
            .append(" calls -> ").append(result).append("; ");
        entity.discard();
    }

    /**
     * The same through the real air current (creative motor -> encased fan -> catalyst), next to Create's own
     * splashing as a control. Each lane runs along the test's +X on a stone floor, walled on both sides and
     * closed at x=5 so the pushed items stay inside the stream:
     * x=0 motor (16 RPM) | x=1 fan facing +X | x=2 catalyst | x=3,4 air, items dropped at x=3.5 | x=5 stone.
     * Without the stopper a pushed item can come to rest just past the stream's end, where its processing
     * timer freezes while the particles keep showing.
     */
    @GameTest(maxTicks = 400)
    public void fanInWorldSnowPowderVsSplashing(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ItemEntity snow = buildFanLane(helper, level, 2, Blocks.POWDER_SNOW.defaultBlockState(), CNItems.NITROGEN_CONCENTRATE.get());
        ItemEntity water = buildFanLane(helper, level, 5, Blocks.WATER.defaultBlockState(), Items.ICE);
        helper.runAfterDelay(350, () -> {
            System.out.println("[createnuclear-gametest] fan in world: powder snow -> " + snow.getItem() + ", water -> " + water.getItem());
            helper.assertTrue(water.getItem().is(Items.PACKED_ICE), "Control lane (Create's splashing) did not convert: " + water.getItem());
            helper.assertTrue(snow.getItem().is(CNItems.COOLED_NITROGEN_CONCENTRATE.get()), "Powder snow lane did not convert: " + snow.getItem());
            helper.succeed();
        });
    }

    private static ItemEntity buildFanLane(GameTestHelper helper, ServerLevel level, int z, BlockState catalyst, net.minecraft.world.item.Item input) {
        Direction east = Direction.getApproximateNearest(Vec3.atLowerCornerOf(
            helper.absolutePos(new BlockPos(1, 0, 0)).subtract(helper.absolutePos(BlockPos.ZERO))));
        BlockState stone = Blocks.STONE.defaultBlockState();
        for (int x = 0; x <= 5; x++) {
            for (int dz = -1; dz <= 1; dz++) {
                level.setBlock(helper.absolutePos(new BlockPos(x, 1, z + dz)), stone, 3);
            }
        }
        for (int x = 2; x <= 5; x++) {
            level.setBlock(helper.absolutePos(new BlockPos(x, 2, z - 1)), stone, 3);
            level.setBlock(helper.absolutePos(new BlockPos(x, 2, z + 1)), stone, 3);
        }
        level.setBlock(helper.absolutePos(new BlockPos(5, 2, z)), stone, 3);
        level.setBlock(helper.absolutePos(new BlockPos(2, 2, z)), catalyst, 3);
        level.setBlock(helper.absolutePos(new BlockPos(1, 2, z)), AllBlocks.ENCASED_FAN.defaultBlockState().setValue(BlockStateProperties.FACING, east), 3);
        level.setBlock(helper.absolutePos(new BlockPos(0, 2, z)), AllBlocks.CREATIVE_MOTOR.defaultBlockState().setValue(BlockStateProperties.FACING, east), 3);
        Vec3 spawn = helper.absoluteVec(new Vec3(3.5, 2.0, z + .5));
        ItemEntity item = new ItemEntity(level, spawn.x, spawn.y, spawn.z, new ItemStack(input, 4));
        item.setDeltaMovement(Vec3.ZERO);
        item.setNeverPickUp();
        item.setUnlimitedLifetime();
        level.addFreshEntity(item);
        return item;
    }

    @GameTest(maxTicks = 200)
    public void reactorRunsOnDedicatedServer(GameTestHelper helper) {
        runReactor(helper, ReactorPatterns.REACTOR_5, 5);
    }

    @GameTest(maxTicks = 200)
    public void reactor7x7Runs(GameTestHelper helper) {
        runReactor(helper, ReactorPatterns.REACTOR_7, 7);
    }

    @GameTest(maxTicks = 200)
    public void reactor9x9Runs(GameTestHelper helper) {
        runReactor(helper, ReactorPatterns.REACTOR_9, 9);
    }

    private static void runReactor(GameTestHelper helper, String[][] aisles, int size) {
        ServerLevel level = helper.getLevel();
        BlockPos controller = helper.absolutePos(new BlockPos(0, 4, 0)).above(20);
        // construct() places the blocks through TickTask(3): wait before reading them back.
        ReactorPatterns.build(aisles).construct(level, controller, (c, st) -> true);
        helper.runAfterDelay(10, () -> fuelAndRun(helper, level, controller, size));
    }

    /**
     * The nearest casing in each horizontal direction along the controller's row: right beside it on
     * the 5x5 and 9x9 ("OA*AO", "OBAA*AABO"), two blocks away on the 7x7 ("OAB*BAO", frames between).
     */
    private static List<BlockPos> inputSlots(ServerLevel level, BlockPos controller) {
        List<BlockPos> slots = new ArrayList<>();
        for (Direction d : Direction.Plane.HORIZONTAL) {
            for (int k = 1; k <= 3; k++) {
                BlockPos pos = controller.relative(d, k);
                if (level.getBlockState(pos).is(CNBlocks.REACTOR_CASING.get())) {
                    slots.add(pos);
                    break;
                }
                if (!level.getBlockState(pos).is(CNBlocks.REACTOR_FRAME.get())) break;
            }
        }
        return slots;
    }

    private static void fuelAndRun(GameTestHelper helper, ServerLevel level, BlockPos controller, int size) {
        List<BlockPos> inputs = inputSlots(level, controller);
        helper.assertTrue(inputs.size() >= 2, size + "x" + size + ": expected two casings along the controller's row, found " + inputs);
        BlockPos rodInput = inputs.get(0);
        BlockPos fluidInput = inputs.get(1);
        level.setBlockAndUpdate(rodInput, CNBlocks.REACTOR_ROD_INPUT.getDefaultState());
        level.setBlockAndUpdate(fluidInput, CNBlocks.REACTOR_FLUID_INPUT.getDefaultState());

        ReactorAssembler.assemble(controller, level);
        ReactorControllerBlockEntity be = (ReactorControllerBlockEntity) level.getBlockEntity(controller);
        helper.assertTrue(be != null && be.isAssembled(), "The " + size + "x" + size + " reactor did not assemble");
        helper.assertTrue(be.getMultiblockSize() == size, "Assembled with size " + be.getMultiblockSize() + ", expected " + size);

        long rods, water;
        try (Transaction tx = Transaction.openOuter()) {
            Storage<ItemVariant> rodStorage = ItemStorage.SIDED.find(level, rodInput, null);
            Storage<FluidVariant> fluids = FluidStorage.SIDED.find(level, fluidInput, null);
            rods = rodStorage == null ? -1 : rodStorage.insert(ItemVariant.of(CNItems.URANIUM_ROD.get()), 64, tx);
            water = fluids == null ? -1 : fluids.insert(FluidVariant.of(Fluids.WATER), FluidConstants.BUCKET * 8, tx);
            tx.commit();
        }
        helper.assertTrue(rods > 0 && water > 0, "Transfer API refused the inputs: rods=" + rods + " water=" + water);

        PatternData[] pattern = new PatternData[57];
        for (int i = 0; i < pattern.length; i++) {
            pattern[i] = new PatternData(i, new ItemStack(Items.GLASS_PANE));
        }
        pattern[28] = new PatternData(28, new ItemStack(CNItems.URANIUM_ROD.get()));
        ItemStack blueprint = new ItemStack(CNItems.REACTOR_BLUEPRINT.get());
        blueprint.set(CNDataComponents.REACTOR_BLUE_PRINT_DATA, new ReactorBluePrintData(0, 1, pattern));
        // As ReactorControllerBlock.useItemOn: the same stack goes in inventory slot 0 and the pattern.
        be.getInventoryObject().setItem(0, blueprint);
        be.setConfiguredPattern(blueprint);

        helper.runAfterDelay(100, () -> {
            boolean active = level.getBlockState(controller).getValue(ReactorControllerBlock.ACTIVE);
            int heat = be.getConfiguredPatternHeat();
            helper.assertTrue(active && heat > 0, size + "x" + size + " reactor is not running: active=" + active + " heat=" + heat);
            System.out.println("[createnuclear-gametest] " + size + "x" + size + " reactor running: heat=" + heat);
            helper.succeed();
        });
    }
}
