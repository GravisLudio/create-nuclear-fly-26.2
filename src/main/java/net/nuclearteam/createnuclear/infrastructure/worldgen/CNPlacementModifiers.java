package net.nuclearteam.createnuclear.infrastructure.worldgen;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.nuclearteam.createnuclear.CreateNuclear;

public class CNPlacementModifiers {
    public static final PlacementModifierType<ConfigPlacementFilter> CONFIG_FILTER = Registry.register(
        BuiltInRegistries.PLACEMENT_MODIFIER_TYPE,
        CreateNuclear.asResource("config_filter"),
        () -> ConfigPlacementFilter.CODEC
    );

    public static void register() {
    }
}
