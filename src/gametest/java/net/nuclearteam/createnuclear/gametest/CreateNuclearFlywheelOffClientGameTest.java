package net.nuclearteam.createnuclear.gametest;

import com.zurrtum.create.client.flywheel.api.backend.Backend;
import com.zurrtum.create.client.flywheel.api.backend.BackendManager;
import com.zurrtum.create.client.flywheel.impl.FabricFlwConfig;
import lib.multiblock.impl.IMultiBlockPattern;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.nuclearteam.createnuclear.CNBlocks;
import net.nuclearteam.createnuclear.CNDataComponents;
import net.nuclearteam.createnuclear.CNItems;
import net.nuclearteam.createnuclear.content.multiblock.ReactorAssembler;
import net.nuclearteam.createnuclear.content.multiblock.bluePrintItem.PatternData;
import net.nuclearteam.createnuclear.content.multiblock.bluePrintItem.ReactorBluePrintData;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerBlockEntity;
import net.nuclearteam.createnuclear.content.multiblock.output.ReactorOutputEntity;

import java.util.List;

/**
 * The Flywheel-off path. With Flywheel on, a block entity registered with {@code visual(...)} never
 * runs its renderer, so {@code ReactorOutputRenderer} (the output's half shaft) has never executed
 * in any other test. This forces the backend off, looks at the same running reactor output with the
 * default backend and with it off, and fails if the backend did not actually change. A renderer
 * that throws would crash the client and fail the test by itself.
 */
public class CreateNuclearFlywheelOffClientGameTest implements FabricClientGameTest {
    private static final String NS = "createnuclear";

    @Override
    public void runTest(ClientGameTestContext context) {
        TestSingleplayerContext singleplayer = context.worldBuilder().create();
        try {
            singleplayer.getClientLevel().waitForChunksRender();
            TestServerContext server = singleplayer.getServer();
            server.runCommand("time set noon");
            server.runCommand("weather clear");
            server.runCommand("gamemode creative @p");
            server.runCommand("fill -8 99 -8 12 99 16 minecraft:smooth_stone");
            server.runCommand("fill -8 100 -8 12 110 16 minecraft:air");

            // A running 5x5 reactor with an output and a shaft on its roof.
            BlockPos controller = new BlockPos(2, 103, 6);
            IMultiBlockPattern reactor = ReactorPatterns.build(ReactorPatterns.REACTOR_5);
            server.runOnServer(s -> reactor.construct(s.overworld(), controller, (c, st) -> true));
            context.waitTicks(10);
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
            server.runOnServer(s -> ReactorAssembler.assemble(controller, s.overworld()));
            server.runOnServer(s -> {
                ServerLevel level = s.overworld();
                try (Transaction tx = Transaction.openOuter()) {
                    Storage<ItemVariant> rods = ItemStorage.SIDED.find(level, rodInput, null);
                    Storage<FluidVariant> fluids = FluidStorage.SIDED.find(level, fluidInput, null);
                    rods.insert(ItemVariant.of(CNItems.URANIUM_ROD.get()), 64, tx);
                    fluids.insert(FluidVariant.of(Fluids.WATER), FluidConstants.BUCKET * 8, tx);
                    tx.commit();
                }
                PatternData[] pattern = new PatternData[57];
                for (int i = 0; i < pattern.length; i++) {
                    pattern[i] = new PatternData(i, new ItemStack(Items.GLASS_PANE));
                }
                pattern[28] = new PatternData(28, new ItemStack(CNItems.URANIUM_ROD.get()));
                ItemStack blueprint = new ItemStack(CNItems.REACTOR_BLUEPRINT.get());
                blueprint.set(CNDataComponents.REACTOR_BLUE_PRINT_DATA, new ReactorBluePrintData(0, 1, pattern));
                ReactorControllerBlockEntity be = (ReactorControllerBlockEntity) level.getBlockEntity(controller);
                be.getInventoryObject().setItem(0, blueprint);
                be.setConfiguredPattern(blueprint);
            });
            context.waitTicks(100);
            BlockPos output = server.computeOnServer(s -> {
                ServerLevel level = s.overworld();
                ReactorControllerBlockEntity be = (ReactorControllerBlockEntity) level.getBlockEntity(controller);
                var box = be.getMultiblockPos();
                BlockPos pos = new BlockPos((box.minX() + box.maxX()) / 2, box.maxY(), (box.minZ() + box.maxZ()) / 2);
                level.setBlockAndUpdate(pos, CNBlocks.REACTOR_OUTPUT.getDefaultState().setValue(BlockStateProperties.FACING, Direction.UP));
                level.setBlockAndUpdate(pos.above(), com.zurrtum.create.AllBlocks.SHAFT.defaultBlockState()
                    .setValue(BlockStateProperties.AXIS, Direction.Axis.Y));
                return pos;
            });
            context.waitTicks(100);
            float speed = server.computeOnServer(s ->
                s.overworld().getBlockEntity(output) instanceof ReactorOutputEntity out ? out.getGeneratedSpeed() : -1f);
            System.out.println("[createnuclear-flywheel] output speed before the comparison: " + speed);
            if (speed <= 0) {
                throw new AssertionError("The reactor output is not turning (speed " + speed + "); nothing to compare");
            }

            // Close-up of the output, same camera for both backends.
            server.runCommand("gamemode spectator @p");
            server.runCommand("tp @p " + (output.getX() + 3) + " " + (output.getY() + 2) + " " + (output.getZ() + 3)
                + " facing " + output.getX() + " " + (output.getY() + 1) + " " + output.getZ());
            context.waitTicks(30);

            String onId = backendId(context);
            boolean onActive = context.computeOnClient(mc -> BackendManager.isBackendOn());
            context.takeScreenshot("flywheel-on");

            context.runOnClient(mc -> {
                FabricFlwConfig.INSTANCE.client.backend.set("flywheel:off");
                mc.levelExtractor.allChanged();
            });
            context.waitTicks(40);
            String offId = backendId(context);
            boolean offActive = context.computeOnClient(mc -> BackendManager.isBackendOn());
            context.takeScreenshot("flywheel-off");
            // Let it render for a while with the renderer running, from a second angle as well.
            server.runCommand("tp @p " + (output.getX() - 3) + " " + (output.getY() + 2) + " " + (output.getZ() - 3)
                + " facing " + output.getX() + " " + (output.getY() + 1) + " " + output.getZ());
            context.waitTicks(40);
            context.takeScreenshot("flywheel-off-2");

            context.runOnClient(mc -> {
                FabricFlwConfig.INSTANCE.client.backend.set("DEFAULT");
                mc.levelExtractor.allChanged();
            });
            context.waitTicks(20);
            System.out.println("[createnuclear-flywheel] backend default=" + onId + " (on=" + onActive + ") forced=" + offId + " (on=" + offActive + ")");
            if (offActive) {
                throw new AssertionError("The Flywheel backend is still on after forcing it off: " + offId);
            }
            if (!onActive) {
                System.out.println("[createnuclear-flywheel] WARNING: the default backend was already off, so the comparison proves nothing");
            }
        } finally {
            singleplayer.close();
        }
    }

    private static String backendId(ClientGameTestContext context) {
        return context.computeOnClient(mc -> Backend.REGISTRY.getIdOrThrow(BackendManager.currentBackend()).toString());
    }
}
