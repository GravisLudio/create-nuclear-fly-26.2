package net.nuclearteam.createnuclear.impl.registry;

import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.nuclearteam.createnuclear.api.CreateNuclearRegistries;
import net.nuclearteam.createnuclear.api.multiblock.fluid.ReactorFluidType;
import net.nuclearteam.createnuclear.api.multiblock.rods.RodType;
import org.jetbrains.annotations.ApiStatus.Internal;

/**
 * Declares the mod's custom datapack registries, synced to clients. Upstream did this from
 * NeoForge's {@code DataPackRegistryEvent.NewRegistry}; Fabric's {@link DynamicRegistries} is the
 * equivalent and has to be called during mod initialisation.
 */
public class CreateNuclearRegistriesImpl {
    private CreateNuclearRegistriesImpl() {}

    @Internal
    public static void register() {
        DynamicRegistries.registerSynced(CreateNuclearRegistries.ROD_TYPE, RodType.CODEC, RodType.CODEC);
        DynamicRegistries.registerSynced(CreateNuclearRegistries.FLUID_TYPE, ReactorFluidType.CODEC, ReactorFluidType.CODEC);
    }
}
