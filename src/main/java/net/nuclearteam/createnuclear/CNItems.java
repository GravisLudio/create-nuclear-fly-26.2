package net.nuclearteam.createnuclear;

import static net.nuclearteam.createnuclear.CNTags.CNItemTags;
import static net.nuclearteam.createnuclear.content.equipment.armor.AntiRadiationArmorItem.*;

import net.nuclearteam.createnuclear.foundation.registrate.ItemEntry;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.*;
import net.nuclearteam.createnuclear.api.ItemRodTypesValue;
import net.nuclearteam.createnuclear.api.multiblock.rods.RodType;
import net.nuclearteam.createnuclear.content.biome.BiomeIrradiationExtractorItem;
import net.nuclearteam.createnuclear.content.equipment.armor.CNArmorMaterials;
import net.nuclearteam.createnuclear.infrastructure.config.CNConfigs;
import net.nuclearteam.createnuclear.content.equipment.cloth.ClothItem;
import net.nuclearteam.createnuclear.content.equipment.cloth.ClothItem.Cloths;
import net.nuclearteam.createnuclear.content.multiblock.bluePrintItem.ReactorBluePrintItem;
import net.nuclearteam.createnuclear.content.radiation.RadiationItem;
import net.nuclearteam.createnuclear.foundation.item.DyedItemsList;
import net.nuclearteam.createnuclear.foundation.utility.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings({"unused", "deprecation"})
public class CNItems {

    public static final ItemEntry<RadiationItem>
        YELLOWCAKE = CreateNuclear.REGISTRATE
            .item("yellowcake", p -> new RadiationItem(p, 4))
            // The effect moved from FoodProperties to the Consumable component in 1.21.2.
            .properties(p -> p.food(
                new FoodProperties.Builder()
                    .nutrition(20)
                    .saturationModifier(0.3F)
                    .alwaysEdible()
                    .build(),
                Consumables.defaultFood()
                    .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(CNEffects.RADIATION, 600, 2), 1.0F))
                    .build()
            ))
            .register(),

        ENRICHED_YELLOWCAKE = CreateNuclear.REGISTRATE
            .item("enriched_yellowcake", p -> new RadiationItem(p, 2))
            .register(),

        RAW_URANIUM = CreateNuclear.REGISTRATE
            .item("raw_uranium", p -> new RadiationItem(p, 3))

            .register(),

        URANIUM_POWDER = CreateNuclear.REGISTRATE
            .item("uranium_powder", p -> new RadiationItem(p, 2))

            .register(),

        URANIUM_ROD = CreateNuclear.REGISTRATE
            .item("uranium_rod", p -> new RadiationItem(p, 100))
            .onRegister(ItemRodTypesValue.setRodTypeInfos(new RodType.Builder()
                .baseRodHeat(() -> CNConfigs.server().rods.uraniumBaseValue.get())
                .proximityRodHeat(() -> (float) CNConfigs.server().rods.uraniumProximityBonus.get())
                .rodTimer(() -> CNConfigs.server().rods.uraniumRodLifetime.get())
                .ratio(() -> CNConfigs.server().rods.uraniumHeatRatio.get())
                .fuelRodType()))

            .register();
    public static final ItemEntry<Item>
        RAW_LEAD = CreateNuclear.REGISTRATE
            .item("raw_lead", Item::new)

            .register(),

        STEEL_INGOT = CreateNuclear.REGISTRATE
            .item("steel_ingot", Item::new)

            .register(),

        COAL_DUST = CreateNuclear.REGISTRATE
            .item("coal_dust", Item::new)

            .register(),

        GRAPHITE_ROD = CreateNuclear.REGISTRATE
            .item("graphite_rod", Item::new)
            .onRegister(ItemRodTypesValue.setRodTypeInfos(new RodType.Builder()
                .baseRodHeat(() -> CNConfigs.server().rods.graphiteBaseValue.get())
                .proximityRodHeat(() -> CNConfigs.server().rods.graphiteProximityMalus.getF())
                .rodTimer(() -> CNConfigs.server().rods.graphiteRodLifetime.get())
                .ratio(() -> CNConfigs.server().rods.graphiteHeatRatio.get())
                .coolerRodType()))

            .register(),

        LEAD_INGOT = CreateNuclear.REGISTRATE
            .item("lead_ingot", Item::new)

            .register(),

        STEEL_NUGGET = CreateNuclear.REGISTRATE
            .item("steel_nugget", Item::new)

            .register(),

        LEAD_NUGGET = CreateNuclear.REGISTRATE
            .item("lead_nugget", Item::new)

            .register(),

        GRAPHENE = CreateNuclear.REGISTRATE
            .item("graphene", Item::new)
            .register(),

        RAW_THORIUM = CreateNuclear.REGISTRATE
            .item("raw_thorium", Item::new)

            .register(),

        THORIUM_DUST = CreateNuclear.REGISTRATE
            .item("thorium_dust", Item::new)

            .register(),

        THORIUM_NUGGET = CreateNuclear.REGISTRATE
            .item("thorium_nugget", Item::new)

            .register(),

        THORIUM_INGOT = CreateNuclear.REGISTRATE
            .item("thorium_ingot", Item::new)

            .register(),

        THORIUM_ROD = CreateNuclear.REGISTRATE
            .item("thorium_rod", Item::new)
            .onRegister(ItemRodTypesValue.setRodTypeInfos(new RodType.Builder()
                .baseRodHeat(() -> CNConfigs.server().rods.baseValueThorium.get())
                .proximityRodHeat(() -> (float) CNConfigs.server().rods.thoriumProxyBonus.get())
                .rodTimer(() -> CNConfigs.server().rods.thoriumRodLifetime.get())
                .ratio(() -> CNConfigs.server().rods.thoriumHeatRatio.get())
                .fuelRodType()))

            .register(),

        NITRATE = CreateNuclear.REGISTRATE
            .item("nitrate", Item::new)

            .register(),

        NITROGEN_CONCENTRATE = CreateNuclear.REGISTRATE
            .item("nitrogen_concentrate", Item::new)

            .register(),

        COOLED_NITROGEN_CONCENTRATE = CreateNuclear.REGISTRATE
            .item("cooled_nitrogen_concentrate", Item::new)

            .register()
    ;

    public static final ItemEntry<Helmet> ANTI_RADIATION_HELMETS = CreateNuclear.REGISTRATE
        .item("default_anti_radiation_helmet", Helmet::new)
        .properties(p -> p.stacksTo(1))

        .transform(setColorComponent(Cloths.DEFAULT))

        .register();

    public static final ItemEntry<Chestplate> ANTI_RADIATION_CHESTPLATES = CreateNuclear.REGISTRATE
            .item("default_anti_radiation_chestplate", Chestplate::new)
            .properties(p -> p.stacksTo(1))

            .transform(setColorComponent(Cloths.DEFAULT))

            .register();

    public static final ItemEntry<Leggings> ANTI_RADIATION_LEGGINGS = CreateNuclear.REGISTRATE
            .item("default_anti_radiation_leggings", Leggings::new)
            .properties(p -> p.stacksTo(1))

            .transform(setColorComponent(Cloths.DEFAULT))

            .register();

    public static final ItemEntry<Boot> ANTI_RADIATION_BOOTS = CreateNuclear.REGISTRATE
            .item("default_anti_radiation_boots", Boot::new)
            .properties(p -> p.stacksTo(1))

            .transform(setColorComponent(Cloths.DEFAULT))

            .register();

    public static final DyedItemsList<ClothItem> CLOTHS = new DyedItemsList<>(color -> {
        String colorName = color.getSerializedName();
        List<Item> ingredients = new ArrayList<>(Arrays.asList(Items.WHITE_DYE, Items.ORANGE_DYE, Items.MAGENTA_DYE, Items.LIGHT_BLUE_DYE, Items.YELLOW_DYE, Items.LIME_DYE, Items.PINK_DYE, Items.GRAY_DYE, Items.LIGHT_GRAY_DYE, Items.CYAN_DYE, Items.PURPLE_DYE, Items.BLUE_DYE, Items.BROWN_DYE, Items.GREEN_DYE, Items.RED_DYE, Items.BLACK_DYE));

        return CreateNuclear.REGISTRATE.item(colorName+ "_cloth", p -> new ClothItem(p, color))

            .register();
    });

    public static final ItemEntry<SpawnEggItem> SPAWN_WOLF = spawnEgg("wolf_irradiated_spawn_egg", CNEntityType.IRRADIATED_WOLF.get());
    public static final ItemEntry<SpawnEggItem> SPAWN_CAT = spawnEgg("cat_irradiated_spawn_egg", CNEntityType.IRRADIATED_CAT.get());
    public static final ItemEntry<SpawnEggItem> SPAWN_CHICKEN = spawnEgg("chicken_irradiated_spawn_egg", CNEntityType.IRRADIATED_CHICKEN.get());

    public static final ItemEntry<ReactorBluePrintItem> REACTOR_BLUEPRINT = CreateNuclear.REGISTRATE
        .item("reactor_blueprint_item", ReactorBluePrintItem::new)

        
        .properties(p -> p.stacksTo(1))
        .register();

    public static final ItemEntry<Item> REINFORCED_GLASS_BOTTLE = CreateNuclear.REGISTRATE
        .item("reinforced_glass_bottle", Item::new)

        .properties(p -> p.stacksTo(16))
        .register();

    public static final ItemEntry<BiomeIrradiationExtractorItem> IRRADIATION_BIOME_EXTRACTOR = CreateNuclear.REGISTRATE
        .item("biome_irradiation_extractor", BiomeIrradiationExtractorItem::new)

        .properties(p -> p.stacksTo(16).fireResistant())
        .register();

    /**
     * Spawn eggs carry their entity type as a component since 1.21.5, and their colours are gone:
     * each egg has its own texture now, resolved by the item model definition.
     */
    private static ItemEntry<SpawnEggItem> spawnEgg(String name, EntityType<? extends Mob> type) {
        return CreateNuclear.REGISTRATE
            .item(name, p -> new SpawnEggItem(p.spawnEgg(type)))
            .register();
    }

    public static void register() {}
}
