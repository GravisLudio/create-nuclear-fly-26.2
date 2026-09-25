package net.nuclearteam.createnuclear;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.nuclearteam.createnuclear.content.contraptions.irradiated.cat.IrradiatedCat;
import net.nuclearteam.createnuclear.content.contraptions.irradiated.chicken.IrradiatedChicken;
import net.nuclearteam.createnuclear.content.contraptions.irradiated.cow.IrradiatedCow;
import net.nuclearteam.createnuclear.content.contraptions.irradiated.wolf.IrradiatedWolf;
import net.nuclearteam.createnuclear.content.explosion.NuclearExplosionEntity;
import net.nuclearteam.createnuclear.foundation.registrate.EntityEntry;

import java.util.function.UnaryOperator;

/**
 * Entity types, registered straight through vanilla. What Registrate chained on here moved:
 * renderers and model layers to {@code client.CNEntityRenderers}, attributes to
 * {@link FabricDefaultAttributeRegistry} in {@link #register()}, tags and lang to the committed
 * JSON under {@code src/generated/resources}.
 */
public class CNEntityType {

    public static final EntityEntry<NuclearExplosionEntity> NUCLEAR_EXPLOSION = register(
        "nuclear_explosion", NuclearExplosionEntity::new, MobCategory.MISC,
        b -> b.sized(1.0f, 1.0f));

    public static final EntityEntry<IrradiatedCat> IRRADIATED_CAT = register(
        "irradiated_cat", IrradiatedCat::new, MobCategory.CREATURE,
        b -> b.sized(0.6f, 0.7f));

    public static final EntityEntry<IrradiatedChicken> IRRADIATED_CHICKEN = register(
        "irradiated_chicken", IrradiatedChicken::new, MobCategory.CREATURE,
        b -> b.sized(0.6f, 0.7f));

    public static final EntityEntry<IrradiatedWolf> IRRADIATED_WOLF = register(
        "irradiated_wolf", IrradiatedWolf::new, MobCategory.CREATURE,
        b -> b.sized(0.6f, 0.85f).eyeHeight(0.68f));

    public static final EntityEntry<IrradiatedCow> IRRADIATED_COW = register(
        "irradiated_cow", IrradiatedCow::new, MobCategory.CREATURE,
        b -> b.sized(0.6f, 0.85f));

    private static <T extends Entity> EntityEntry<T> register(String name, EntityType.EntityFactory<T> factory,
                                                             MobCategory category,
                                                             UnaryOperator<EntityType.Builder<T>> properties) {
        Identifier id = CreateNuclear.asResource(name);
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, id);
        EntityType<T> type = Registry.register(BuiltInRegistries.ENTITY_TYPE, id,
            properties.apply(EntityType.Builder.of(factory, category)).build(key));
        return new EntityEntry<>(id, type);
    }

    public static void register() {
        FabricDefaultAttributeRegistry.register(IRRADIATED_CAT.get(), IrradiatedCat.createAttributes());
        FabricDefaultAttributeRegistry.register(IRRADIATED_CHICKEN.get(), IrradiatedChicken.createAttributes());
        FabricDefaultAttributeRegistry.register(IRRADIATED_WOLF.get(), IrradiatedWolf.createAttributes());
        FabricDefaultAttributeRegistry.register(IRRADIATED_COW.get(), IrradiatedCow.createAttributes());
    }
}
