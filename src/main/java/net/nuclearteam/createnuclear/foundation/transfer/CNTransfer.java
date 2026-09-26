package net.nuclearteam.createnuclear.foundation.transfer;

import com.zurrtum.create.AllTransfer;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.CachedFluidInventoryBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.CachedInventoryBehaviour;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.nuclearteam.createnuclear.CNBlockEntityTypes;
import net.nuclearteam.createnuclear.content.multiblock.input.fluid.ReactorFluidInputEntity;
import net.nuclearteam.createnuclear.content.multiblock.input.item.ReactorRodInputEntity;

import java.util.function.Function;

/**
 * Publishes the reactor inputs to Fabric's transfer API, so pipes, hoppers and other mods can fill
 * them (upstream: NeoForge capabilities). Create's own pipes and funnels already find them through
 * {@code FluidInventoryProvider} / {@code ItemInventoryProvider} on the blocks; this is the path
 * for everything else. Same pattern as Create Fly's {@code AllTransfer} and Connected's
 * {@code CCTransfer}: the storages are cached in a behaviour, since each one carries transaction
 * snapshot state.
 */
public final class CNTransfer {
    private CNTransfer() {
    }

    public static void register() {
        // Set when fabric-transfer-api-v1 is absent; the calls below would fail on a missing class.
        if (AllTransfer.DISABLE)
            return;

        registerFluidSide(CNBlockEntityTypes.REACTOR_FLUID_INPUT.get(), ReactorFluidInputEntity::getTank);
        registerItemSide(CNBlockEntityTypes.REACTOR_INPUT.get(), be -> be.inventory);
    }

    private static <T extends SmartBlockEntity> void registerItemSide(BlockEntityType<T> type, Function<T, Container> factory) {
        BlockEntityBehaviour.add(type, be -> new CachedInventoryBehaviour<>(be, factory));
        ItemStorage.SIDED.registerForBlockEntity(CachedInventoryBehaviour::get, type);
    }

    private static <T extends SmartBlockEntity> void registerFluidSide(BlockEntityType<T> type, Function<T, FluidInventory> factory) {
        BlockEntityBehaviour.add(type, be -> new CachedFluidInventoryBehaviour<>(be, factory));
        FluidStorage.SIDED.registerForBlockEntity(CachedFluidInventoryBehaviour::get, type);
    }
}
