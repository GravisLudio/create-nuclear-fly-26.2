package net.nuclearteam.createnuclear;

import net.nuclearteam.createnuclear.foundation.registrate.BlockEntityEntry;
import net.nuclearteam.createnuclear.content.enriching.campfire.EnrichingCampfireBlockEntity;
import net.nuclearteam.createnuclear.content.multiblock.alarm.ReactorAlarmEntity;
import net.nuclearteam.createnuclear.content.multiblock.casing.ReactorCasingEntity;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerBlockEntity;
import net.nuclearteam.createnuclear.content.multiblock.core.ReactorCoreEntity;
import net.nuclearteam.createnuclear.content.multiblock.frame.ReactorFrameEntity;
import net.nuclearteam.createnuclear.content.multiblock.input.fluid.ReactorFluidInputEntity;
import net.nuclearteam.createnuclear.content.multiblock.input.item.ReactorRodInputEntity;
import net.nuclearteam.createnuclear.content.multiblock.output.ReactorOutputEntity;

/**
 * Renderers and visuals chained here upstream are client-side, in {@code client.CNBlockEntityRenders}.
 */
public class CNBlockEntityTypes {
    public static final BlockEntityEntry<EnrichingCampfireBlockEntity> ENRICHING_CAMPFIRE_BLOCK =
            CreateNuclear.REGISTRATE.blockEntity("enriching_campfire_block", EnrichingCampfireBlockEntity::new)
                    .validBlock(CNBlocks.ENRICHING_CAMPFIRE)
                    .register();

    public static final BlockEntityEntry<ReactorCasingEntity> REACTOR_CASING =
            CreateNuclear.REGISTRATE.blockEntity("reactor_casing", ReactorCasingEntity::new)
                    .validBlocks(CNBlocks.REACTOR_CASING)
                    .register();

    public static final BlockEntityEntry<ReactorCoreEntity> REACTOR_CORE =
            CreateNuclear.REGISTRATE.blockEntity("reactor_core", ReactorCoreEntity::new)
                    .validBlocks(CNBlocks.REACTOR_CORE)
                    .register();

    public static final BlockEntityEntry<ReactorFrameEntity> REACTOR_FRAME =
            CreateNuclear.REGISTRATE.blockEntity("reactor_frame", ReactorFrameEntity::new)
                    .validBlocks(CNBlocks.REACTOR_FRAME)
                    .register();

    public static final BlockEntityEntry<ReactorRodInputEntity> REACTOR_INPUT =
            CreateNuclear.REGISTRATE.blockEntity("reactor_input", ReactorRodInputEntity::new)
                    .validBlocks(CNBlocks.REACTOR_ROD_INPUT)
                    .register();

    public static final BlockEntityEntry<ReactorFluidInputEntity> REACTOR_FLUID_INPUT =
            CreateNuclear.REGISTRATE.blockEntity("reactor_fluid_input", ReactorFluidInputEntity::new)
                    .validBlocks(CNBlocks.REACTOR_FLUID_INPUT)
                    .register();

    public static final BlockEntityEntry<ReactorOutputEntity> REACTOR_OUTPUT =
            CreateNuclear.REGISTRATE.blockEntity("reactor_output", ReactorOutputEntity::new)
                    .validBlocks(CNBlocks.REACTOR_OUTPUT)
                    .register();

    public static final BlockEntityEntry<ReactorControllerBlockEntity> REACTOR_CONTROLLER =
            CreateNuclear.REGISTRATE.blockEntity("reactor_controller", ReactorControllerBlockEntity::new)
                    .validBlocks(CNBlocks.REACTOR_CONTROLLER)
                    .register();

    public static final BlockEntityEntry<ReactorAlarmEntity> REACTOR_ALARM =
            CreateNuclear.REGISTRATE.blockEntity("reactor_alarm", ReactorAlarmEntity::new)
                    .validBlocks(CNBlocks.REACTOR_ALARM)
                    .register();

    public static void register() {}
}
