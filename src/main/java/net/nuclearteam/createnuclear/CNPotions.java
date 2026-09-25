package net.nuclearteam.createnuclear;

import net.fabricmc.fabric.api.registry.FabricPotionBrewingBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;

public class CNPotions {

    public static final Holder<Potion> POTION_1 = register("potion_of_radiation_1",
        new MobEffectInstance(CNEffects.RADIATION, 900));
    public static final Holder<Potion> POTION_AUGMENT_1 = register("potion_of_radiation_augment_1",
        new MobEffectInstance(CNEffects.RADIATION, 1800));
    public static final Holder<Potion> POTION_2 = register("potion_of_radiation_2",
        new MobEffectInstance(CNEffects.RADIATION, 410, 1));

    public static final Holder<Potion> POTION_1_IODINE = register("potion_of_iodine",
        new MobEffectInstance(CNEffects.IODINE, 900));
    public static final Holder<Potion> POTION_AUGMENT_1_IODINE = register("potion_of_iodine_augment",
        new MobEffectInstance(CNEffects.IODINE, 1800));

    /**
     * 26.2's {@code Potion} takes its translation name explicitly; 1.21.1 fell back to the
     * registry path. Passing the path keeps the committed {@code item.minecraft.potion.effect.*}
     * lang keys valid.
     */
    private static Holder<Potion> register(String name, MobEffectInstance effect) {
        return Registry.registerForHolder(BuiltInRegistries.POTION, CreateNuclear.asResource(name), new Potion(name, effect));
    }

    /** Was a {@code RegisterBrewingRecipesEvent} listener. */
    public static void register() {
        FabricPotionBrewingBuilder.BUILD.register(CNPotions::registerPotionsRecipes);
    }

    private static void registerPotionsRecipes(PotionBrewing.Builder builder) {
        builder.addMix(Potions.AWKWARD, CNItems.ENRICHED_YELLOWCAKE.get(), POTION_1);
        builder.addMix(POTION_1, Items.REDSTONE, POTION_AUGMENT_1);
        builder.addMix(POTION_1, Items.GLOWSTONE_DUST, POTION_2);

        builder.addMix(Potions.AWKWARD, Items.DRIED_KELP, POTION_1_IODINE);
        builder.addMix(POTION_1_IODINE, Items.REDSTONE, POTION_AUGMENT_1_IODINE);
    }
}
