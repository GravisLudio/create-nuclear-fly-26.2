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
public record SnowPowderRecipe(List<ProcessingOutput> results, Ingredient ingredient) implements CreateSingleStackRollableRecipe {
    public static final MapCodec<SnowPowderRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        ProcessingOutput.CODEC.listOf(1, 12).fieldOf("results").forGetter(SnowPowderRecipe::results),
        Ingredient.CODEC.fieldOf("ingredient").forGetter(SnowPowderRecipe::ingredient)
    ).apply(instance, SnowPowderRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, SnowPowderRecipe> STREAM_CODEC = StreamCodec.composite(
        ProcessingOutput.STREAM_CODEC.apply(ByteBufCodecs.list()),
        SnowPowderRecipe::results,
        Ingredient.CONTENTS_STREAM_CODEC,
        SnowPowderRecipe::ingredient,
        SnowPowderRecipe::new
    );
    public static final RecipeSerializer<SnowPowderRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    @Override
    public RecipeSerializer<SnowPowderRecipe> getSerializer() {
        return CNRecipeTypes.SNOW_POWDER_SERIALIZER;
    }

    @Override
    public RecipeType<SnowPowderRecipe> getType() {
        return CNRecipeTypes.SNOW_POWDER;
    }
}
