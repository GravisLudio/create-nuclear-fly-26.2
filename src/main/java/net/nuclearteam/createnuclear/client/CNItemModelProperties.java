package net.nuclearteam.createnuclear.client;

import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import net.nuclearteam.createnuclear.CreateNuclear;
import net.nuclearteam.createnuclear.content.biome.BiomeIrradiationExtractorItem;
import org.jspecify.annotations.Nullable;

/**
 * Item model properties used by assets/createnuclear/items/*.json (see tools/gen-item-models.py).
 * <p>
 * Upstream's datagen wrote override predicates for {@code createnuclear:biome_restore} but never
 * registered the property, so the extractor always showed its empty model. 26.2 replaced
 * predicates with typed properties; this one reports charge / max charge in [0, 1].
 * The max charge is a server config value: on a remote server the client reads its local copy.
 */
@Environment(EnvType.CLIENT)
public final class CNItemModelProperties {
    private CNItemModelProperties() {
    }

    public static void register() {
        RangeSelectItemModelProperties.ID_MAPPER.put(CreateNuclear.asResource(BiomeIrradiationExtractorItem.TAG), BiomeRestoreCharge.MAP_CODEC);
    }

    public record BiomeRestoreCharge() implements RangeSelectItemModelProperty {
        public static final MapCodec<BiomeRestoreCharge> MAP_CODEC = MapCodec.unit(new BiomeRestoreCharge());

        @Override
        public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
            return Mth.clamp(BiomeIrradiationExtractorItem.getCharge(stack) / (float) BiomeIrradiationExtractorItem.getMaxCharge(), 0.0F, 1.0F);
        }

        @Override
        public MapCodec<BiomeRestoreCharge> type() {
            return MAP_CODEC;
        }
    }
}
