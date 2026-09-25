package net.nuclearteam.createnuclear.content.kinetics.fan.processing;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import com.zurrtum.create.foundation.recipe.CreateSingleStackRollableRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.nuclearteam.createnuclear.CNRecipeTypes;

import java.util.List;

/**
 * Fan processing recipe, in the record shape Create Fly gives its own ({@code HauntingRecipe}):
 * one ingredient, up to 12 rollable results. Upstream extended {@code StandardProcessingRecipe}.
 */
public record EnrichedRecipe(List<ProcessingOutput> results, Ingredient ingredient) implements CreateSingleStackRollableRecipe {
    public static final MapCodec<EnrichedRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        ProcessingOutput.CODEC.listOf(1, 12).fieldOf("results").forGetter(EnrichedRecipe::results),
        Ingredient.CODEC.fieldOf("ingredient").forGetter(EnrichedRecipe::ingredient)
    ).apply(instance, EnrichedRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, EnrichedRecipe> STREAM_CODEC = StreamCodec.composite(
        ProcessingOutput.STREAM_CODEC.apply(ByteBufCodecs.list()),
        EnrichedRecipe::results,
        Ingredient.CONTENTS_STREAM_CODEC,
        EnrichedRecipe::ingredient,
        EnrichedRecipe::new
    );
    public static final RecipeSerializer<EnrichedRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    @Override
    public RecipeSerializer<EnrichedRecipe> getSerializer() {
        return CNRecipeTypes.ENRICHED_SERIALIZER;
    }

    @Override
    public RecipeType<EnrichedRecipe> getType() {
        return CNRecipeTypes.ENRICHED;
    }
}
