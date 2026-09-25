package net.nuclearteam.createnuclear.infrastructure.worldgen;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/**
 * Ore placement. Upstream generated NeoForge {@code add_features} biome modifiers targeting
 * {@code #minecraft:is_overworld}; Fabric does the same from code through {@link BiomeModifications},
 * and the placed features themselves stay data-driven under {@code worldgen/placed_feature}.
 */
public class CNBiomeModifiers {
    public static void register() {
        addOre(CNPlacedFeatures.URANIUM_ORE);
        addOre(CNPlacedFeatures.LEAD_ORE);
        addOre(CNPlacedFeatures.THORIUM_ORE);
        addOre(CNPlacedFeatures.NITRATE_ORE);
        addOre(CNPlacedFeatures.STRIATED_ORES_OVERWORLD);
    }

    private static void addOre(ResourceKey<PlacedFeature> feature) {
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Decoration.UNDERGROUND_ORES, feature);
    }
}
