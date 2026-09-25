package net.nuclearteam.createnuclear.client;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.TooltipBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.nuclearteam.createnuclear.CNBlockEntityTypes;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerBlockEntity;
import net.nuclearteam.createnuclear.content.multiblock.input.fluid.ReactorFluidInputEntity;
import net.nuclearteam.createnuclear.content.multiblock.output.ReactorOutputEntity;

import java.util.List;
import java.util.function.Function;

/**
 * Client-side block entity behaviours, kept by Create Fly in a registry keyed by block entity type.
 * <p>
 * Goggle tooltips live here now: 26.2's goggle overlay looks up a {@link TooltipBehaviour} at the
 * position and never asks the block entity, so a block entity that merely has
 * {@code addToGoggleTooltip} shows nothing -- silently. Each behaviour below only routes to the
 * block entity's own method, which keeps upstream's tooltip code where it was. Same approach as
 * the Connected port's {@code CCBlockEntityBehaviours}.
 */
@Environment(EnvType.CLIENT)
public final class CNBlockEntityBehaviours {
    private CNBlockEntityBehaviours() {
    }

    public static void register() {
        goggles(CNBlockEntityTypes.REACTOR_CONTROLLER.get(), ReactorControllerBlockEntity::addToGoggleTooltip);
        goggles(CNBlockEntityTypes.REACTOR_FLUID_INPUT.get(), ReactorFluidInputEntity::addToGoggleTooltip);
        goggles(CNBlockEntityTypes.REACTOR_OUTPUT.get(), ReactorOutputEntity::addToGoggleTooltip);
    }

    @FunctionalInterface
    private interface GoggleTooltip<T> {
        boolean add(T blockEntity, List<Component> tooltip, boolean isPlayerSneaking);
    }

    @SuppressWarnings("unchecked")
    private static <T extends SmartBlockEntity> void goggles(BlockEntityType<T> type, GoggleTooltip<T> tooltip) {
        Function<T, BlockEntityBehaviour<?>> factory = be -> new GoggleTooltipBehaviour<>(be, tooltip);
        BlockEntityBehaviour.CLIENT_REGISTRY.add(type, (Function<SmartBlockEntity, BlockEntityBehaviour<?>>) (Function<?, ?>) factory);
    }

    private static class GoggleTooltipBehaviour<T extends SmartBlockEntity> extends TooltipBehaviour<T> implements IHaveGoggleInformation {
        private final GoggleTooltip<T> tooltip;

        GoggleTooltipBehaviour(T be, GoggleTooltip<T> tooltip) {
            super(be);
            this.tooltip = tooltip;
        }

        @Override
        public boolean addToGoggleTooltip(List<Component> list, boolean isPlayerSneaking) {
            return tooltip.add(blockEntity, list, isPlayerSneaking);
        }
    }
}
