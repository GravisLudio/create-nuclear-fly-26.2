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

import java.util.List;

/**
 * Server game tests (`gradlew runGameTest`): run on a dedicated server, so they also catch client
 * classes reached from common code. The same 5x5 reactor as the client test's step 6, without screenshots.
 */
public class CreateNuclearServerGameTest {

    @GameTest(maxTicks = 200)
    public void reactorRunsOnDedicatedServer(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos controller = helper.absolutePos(new BlockPos(0, 4, 0)).above(20);
        // construct() places the blocks through TickTask(3): wait before reading them back.
        ReactorPatterns.build(ReactorPatterns.REACTOR_5).construct(level, controller, (c, st) -> true);
        helper.runAfterDelay(10, () -> fuelAndRun(helper, level, controller));
    }

    private static void fuelAndRun(GameTestHelper helper, ServerLevel level, BlockPos controller) {
        List<BlockPos> inputs = Direction.Plane.HORIZONTAL.stream()
            .map(controller::relative)
            .filter(pos -> level.getBlockState(pos).is(CNBlocks.REACTOR_CASING.get()))
            .toList();
        helper.assertTrue(inputs.size() == 2, "Expected two casings beside the controller, found " + inputs);
        BlockPos rodInput = inputs.get(0);
        BlockPos fluidInput = inputs.get(1);
        level.setBlockAndUpdate(rodInput, CNBlocks.REACTOR_ROD_INPUT.getDefaultState());
        level.setBlockAndUpdate(fluidInput, CNBlocks.REACTOR_FLUID_INPUT.getDefaultState());

        ReactorAssembler.assemble(controller, level);
        ReactorControllerBlockEntity be = (ReactorControllerBlockEntity) level.getBlockEntity(controller);
        helper.assertTrue(be != null && be.isAssembled(), "The 5x5 reactor did not assemble");

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
        be.getInventoryObject().setItem(0, blueprint);
        be.setConfiguredPattern(blueprint);

        helper.runAfterDelay(100, () -> {
            boolean active = level.getBlockState(controller).getValue(ReactorControllerBlock.ACTIVE);
            int heat = be.getConfiguredPatternHeat();
            helper.assertTrue(active && heat > 0, "Reactor is not running: active=" + active + " heat=" + heat);
            helper.succeed();
        });
    }
}
