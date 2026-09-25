package net.nuclearteam.createnuclear.infrastructure.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.placement.*;
import net.nuclearteam.createnuclear.CreateNuclear;



public class CNPlacedFeatures {
    public static final ResourceKey<PlacedFeature>
        URANIUM_ORE = key("uranium_ore"),
        LEAD_ORE = key("lead_ore"),
        THORIUM_ORE = key("thorium_ore"),
        NITRATE_ORE = key("nitrate_ore"),
        STRIATED_ORES_OVERWORLD = key("striated_ores_overworld")
    ;

    private static ResourceKey<PlacedFeature> key(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, CreateNuclear.asResource(name));
    }
}
