package net.nuclearteam.createnuclear.infrastructure.worldgen.biome;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.*;
import net.nuclearteam.createnuclear.CreateNuclear;
import org.jetbrains.annotations.NotNull;

public class CNBiomes {
    public static final class Irradiated {
        public static final ResourceKey<Biome> PLAIN = key("irradiated_land");
    }

    public static @NotNull ResourceKey<Biome> key(String id) {
        return ResourceKey.create(Registries.BIOME, CreateNuclear.asResource(id));
    }
}