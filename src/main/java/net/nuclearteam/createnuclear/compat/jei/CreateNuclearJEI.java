package net.nuclearteam.createnuclear.compat.jei;

import com.zurrtum.create.AllItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.common.Internal;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.level.block.Blocks;
import net.nuclearteam.createnuclear.CNBlocks;
import net.nuclearteam.createnuclear.CNRecipeTypes;
import net.nuclearteam.createnuclear.CreateNuclear;
import net.nuclearteam.createnuclear.compat.jei.category.NuclearFanCategory;
import net.nuclearteam.createnuclear.content.kinetics.fan.processing.EnrichedRecipe;
import net.nuclearteam.createnuclear.content.kinetics.fan.processing.SnowPowderRecipe;
import org.jetbrains.annotations.NotNull;

/**
 * JEI categories for the two fan processing types this mod adds (enriching fire, powder snow).
 * <p>
 * Upstream's plugin was a copy of Create's own (toolbox colouring, potion fluids, blueprint and
 * stock keeper transfer, ghost ingredients, slot mover). On Fabric, Create Fly's
 * {@code JeiClientPlugin} registers all of those itself; registering them twice would duplicate
 * recipes and handlers, so only the nuclear categories are left here.
 * <p>
 * Registered through the {@code jei_mod_plugin} entrypoint in {@code fabric.mod.json} (JEI does not
 * scan {@code @JeiPlugin} on Fabric). Recipes come from JEI's client-synced recipe map, as in
 * Create Fly.
 */
public class CreateNuclearJEI implements IModPlugin {
    private static final Identifier ID = CreateNuclear.asResource("jei_plugin");

    public static final IRecipeType<RecipeHolder<EnrichedRecipe>> FAN_ENRICHED = createRecipeHolderType("fan_enriched");
    public static final IRecipeType<RecipeHolder<SnowPowderRecipe>> FAN_SNOW_POWDER = createRecipeHolderType("fan_snow_powder");

    public static IJeiRuntime runtime;

    @SuppressWarnings("unchecked")
    private static <T> IRecipeType<T> createRecipeHolderType(String path) {
        return (IRecipeType<T>) IRecipeType.create(CreateNuclear.asResource(path), RecipeHolder.class);
    }

    @Override
    @NotNull
    public Identifier getPluginUid() {
        return ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
            new NuclearFanCategory<>(FAN_ENRICHED, Component.translatable("createnuclear.enriched.fan.recipe"),
                CNBlocks.ENRICHING_CAMPFIRE.asItem(), () -> CNBlocks.ENRICHING_FIRE.getDefaultState(),
                EnrichedRecipe::ingredient, EnrichedRecipe::results),
            new NuclearFanCategory<>(FAN_SNOW_POWDER, Component.translatable("createnuclear.snow_powder.fan.recipe"),
                Items.POWDER_SNOW_BUCKET, Blocks.POWDER_SNOW::defaultBlockState,
                SnowPowderRecipe::ingredient, SnowPowderRecipe::results)
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        RecipeMap recipes = Internal.getClientSyncedRecipes();
        registration.addRecipes(FAN_ENRICHED, recipes.byType(CNRecipeTypes.ENRICHED).stream().toList());
        registration.addRecipes(FAN_SNOW_POWDER, recipes.byType(CNRecipeTypes.SNOW_POWDER).stream().toList());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(FAN_ENRICHED, AllItems.ENCASED_FAN);
        registration.addCraftingStation(FAN_SNOW_POWDER, AllItems.ENCASED_FAN);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        CreateNuclearJEI.runtime = runtime;
    }
}
