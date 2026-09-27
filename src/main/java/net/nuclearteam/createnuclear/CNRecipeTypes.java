package net.nuclearteam.createnuclear;

import net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization;
import net.fabricmc.loader.api.FabricLoader;
import com.zurrtum.create.AllRecipeSets;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipePropertySet;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.nuclearteam.createnuclear.content.kinetics.fan.processing.EnrichedRecipe;
import net.nuclearteam.createnuclear.content.kinetics.fan.processing.SnowPowderRecipe;

import java.util.Optional;

/**
 * Recipe types and serializers, registered the way Create Fly registers its own
 * ({@code AllRecipeTypes} / {@code AllRecipeSerializers}); upstream was an enum over NeoForge
 * deferred registers.
 * <p>
 * Each fan type also gets a {@link RecipePropertySet}. Since 1.21.2 the client no longer holds
 * the recipes, only these ingredient sets, which the server sends; Create Fly's
 * {@code RecipeManagerMixin} builds and syncs every set listed in {@link AllRecipeSets#ALL}, so
 * adding ours there is what lets {@code canProcess} answer on both sides.
 */
public class CNRecipeTypes {
    public static final RecipeType<EnrichedRecipe> ENRICHED = registerType("enriched");
    public static final RecipeType<SnowPowderRecipe> SNOW_POWDER = registerType("snow_powder");

    public static final RecipeSerializer<EnrichedRecipe> ENRICHED_SERIALIZER = registerSerializer("enriched", EnrichedRecipe.SERIALIZER);
    public static final RecipeSerializer<SnowPowderRecipe> SNOW_POWDER_SERIALIZER = registerSerializer("snow_powder", SnowPowderRecipe.SERIALIZER);

    public static final ResourceKey<RecipePropertySet> ENRICHED_SET = propertySet("enriched");
    public static final ResourceKey<RecipePropertySet> SNOW_POWDER_SET = propertySet("snow_powder");

    private static <T extends Recipe<?>> RecipeType<T> registerType(String name) {
        Identifier id = CreateNuclear.asResource(name);
        return Registry.register(BuiltInRegistries.RECIPE_TYPE, id, new RecipeType<T>() {
            @Override
            public String toString() {
                return id.toString();
            }
        });
    }

    private static <T extends Recipe<?>> RecipeSerializer<T> registerSerializer(String name, RecipeSerializer<T> serializer) {
        return Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, CreateNuclear.asResource(name), serializer);
    }

    private static ResourceKey<RecipePropertySet> propertySet(String name) {
        return ResourceKey.create(RecipePropertySet.TYPE_KEY, CreateNuclear.asResource(name));
    }

    public static void register() {
        AllRecipeSets.ALL.put(ENRICHED_SET, recipe -> recipe instanceof EnrichedRecipe r ? Optional.of(r.ingredient()) : Optional.empty());
        AllRecipeSets.ALL.put(SNOW_POWDER_SET, recipe -> recipe instanceof SnowPowderRecipe r ? Optional.of(r.ingredient()) : Optional.empty());

        // 26.2 no longer sends every recipe to clients: recipe viewers (JEI, RRV) only see the
        // serializers registered here, through Fabric's recipe sync. Without it the JEI fan
        // categories load but stay empty. Same guard as Create Fly's CompatMod.
        FabricLoader loader = FabricLoader.getInstance();
        if (loader.isModLoaded("jei") || loader.isModLoaded("rrv")) {
            RecipeSynchronization.synchronizeRecipeSerializer(ENRICHED_SERIALIZER);
            RecipeSynchronization.synchronizeRecipeSerializer(SNOW_POWDER_SERIALIZER);
        }
    }
}
