package net.nuclearteam.createnuclear;

import static net.nuclearteam.createnuclear.CNTags.CNItemTags;
import static net.nuclearteam.createnuclear.content.equipment.armor.AntiRadiationArmorItem.*;
import static net.nuclearteam.createnuclear.foundation.data.CNBuilderTransformers.biomeRestoreModel;
import static net.nuclearteam.createnuclear.foundation.data.CNBuilderTransformers.coloredArmorModel;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import net.nuclearteam.createnuclear.foundation.registrate.ItemEntry;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.common.Tags;
import net.nuclearteam.createnuclear.api.ItemRodTypesValue;
import net.nuclearteam.createnuclear.api.data.recipe.SmithingClothRecipeBuilder;
import net.nuclearteam.createnuclear.api.multiblock.rods.RodType;
import net.nuclearteam.createnuclear.content.biome.BiomeIrradiationExtractorItem;
import net.nuclearteam.createnuclear.content.equipment.armor.CNArmorMaterials;
import net.nuclearteam.createnuclear.infrastructure.config.CNConfigs;
import net.nuclearteam.createnuclear.content.equipment.cloth.ClothItem;
import net.nuclearteam.createnuclear.content.equipment.cloth.ClothItem.Cloths;
import net.nuclearteam.createnuclear.content.multiblock.bluePrintItem.ReactorBluePrintItem;
import net.nuclearteam.createnuclear.content.radiation.RadiationItem;
import net.nuclearteam.createnuclear.foundation.data.CNBuilderTransformers;
import net.nuclearteam.createnuclear.foundation.item.DyedItemsList;
import net.nuclearteam.createnuclear.foundation.utility.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings({"unused", "deprecation"})
public class CNItems {
    static {
        CreateNuclear.REGISTRATE.setCreativeTab(CNCreativeModeTabs.MAIN);
    }

    public static final ItemEntry<RadiationItem>
        YELLOWCAKE = CreateNuclear.REGISTRATE
            .item("yellowcake", p -> new RadiationItem(p, 4))
            .properties(p -> p.food(new FoodProperties.Builder()
                .nutrition(20)
                .saturationModifier(0.3F)
                .alwaysEdible()
                .effect((new MobEffectInstance(CNEffects.RADIATION.getDelegate(),600,2)) , 1.0F)
                .build())
            )
            .register(),

        ENRICHED_YELLOWCAKE = CreateNuclear.REGISTRATE
            .item("enriched_yellowcake", p -> new RadiationItem(p, 2))
            .register(),

        RAW_URANIUM = CreateNuclear.REGISTRATE
            .item("raw_uranium", p -> new RadiationItem(p, 3))
            .tag(CNTags.forgeItemTag("raw_ores"), Tags.Items.RAW_MATERIALS, CNTags.forgeItemTag("raw_materials/uranium"))
            
            .register(),

        URANIUM_POWDER = CreateNuclear.REGISTRATE
            .item("uranium_powder", p -> new RadiationItem(p, 2))
            .tag(Tags.Items.DUSTS, CNTags.forgeItemTag("dusts/uranium"))
            .register(),

        URANIUM_ROD = CreateNuclear.REGISTRATE
            .item("uranium_rod", p -> new RadiationItem(p, 100))
            .onRegister(ItemRodTypesValue.setRodTypeInfos(new RodType.Builder()
                .baseRodHeat(() -> CNConfigs.server().rods.uraniumBaseValue.get())
                .proximityRodHeat(() -> (float) CNConfigs.server().rods.uraniumProximityBonus.get())
                .rodTimer(() -> CNConfigs.server().rods.uraniumRodLifetime.get())
                .ratio(() -> CNConfigs.server().rods.uraniumHeatRatio.get())
                .fuelRodType()))
            .tag(Tags.Items.RODS, CNItemTags.FUEL.tag)
            .register();
    
    public static final ItemEntry<Item>
        RAW_LEAD = CreateNuclear.REGISTRATE
            .item("raw_lead", Item::new)
            .tag(CNTags.forgeItemTag("raw_ores"), Tags.Items.RAW_MATERIALS, CNTags.forgeItemTag("raw_materials/lead"))
            
            .register(),

        STEEL_INGOT = CreateNuclear.REGISTRATE
            .item("steel_ingot", Item::new)
            .tag(Tags.Items.INGOTS, CNTags.forgeItemTag("ingots/steel"))
            
            .register(),

        COAL_DUST = CreateNuclear.REGISTRATE
            .item("coal_dust", Item::new)
            .tag(Tags.Items.DUSTS, CNTags.forgeItemTag("dusts/coal"))
            .register(),

        GRAPHITE_ROD = CreateNuclear.REGISTRATE
            .item("graphite_rod", Item::new)
            .onRegister(ItemRodTypesValue.setRodTypeInfos(new RodType.Builder()
                .baseRodHeat(() -> CNConfigs.server().rods.graphiteBaseValue.get())
                .proximityRodHeat(() -> CNConfigs.server().rods.graphiteProximityMalus.getF())
                .rodTimer(() -> CNConfigs.server().rods.graphiteRodLifetime.get())
                .ratio(() -> CNConfigs.server().rods.graphiteHeatRatio.get())
                .coolerRodType()))
            .tag(Tags.Items.RODS, CNItemTags.COOLER.tag)
            .register(),

        LEAD_INGOT = CreateNuclear.REGISTRATE
            .item("lead_ingot", Item::new)
            .tag(Tags.Items.INGOTS, CNTags.forgeItemTag("ingots/lead"))
            
            .register(),

        STEEL_NUGGET = CreateNuclear.REGISTRATE
            .item("steel_nugget", Item::new)
            .tag(Tags.Items.NUGGETS, CNTags.forgeItemTag("nuggets/steel"))
            
            .register(),

        LEAD_NUGGET = CreateNuclear.REGISTRATE
            .item("lead_nugget", Item::new)
            .tag(Tags.Items.NUGGETS, CNTags.forgeItemTag("nuggets/lead"))
            
            .register(),

        GRAPHENE = CreateNuclear.REGISTRATE
            .item("graphene", Item::new)
            .register(),

        RAW_THORIUM = CreateNuclear.REGISTRATE
            .item("raw_thorium", Item::new)
            .tag(CNTags.forgeItemTag("raw_ores"), Tags.Items.RAW_MATERIALS, CNTags.forgeItemTag("raw_materials/thorium"))
            
            .register(),

        THORIUM_DUST = CreateNuclear.REGISTRATE
            .item("thorium_dust", Item::new)
            .tag(Tags.Items.DUSTS, CNTags.forgeItemTag("dusts/thorium"))
            .register(),

        THORIUM_NUGGET = CreateNuclear.REGISTRATE
            .item("thorium_nugget", Item::new)
            
            .tag(Tags.Items.NUGGETS, CNTags.forgeItemTag("nuggets/thorium"))
            
            .register(),

        THORIUM_INGOT = CreateNuclear.REGISTRATE
            .item("thorium_ingot", Item::new)
            
            .tag(Tags.Items.INGOTS, CNTags.forgeItemTag("ingots/thorium"))
            
            .register(),

        THORIUM_ROD = CreateNuclear.REGISTRATE
            .item("thorium_rod", Item::new)
            .onRegister(ItemRodTypesValue.setRodTypeInfos(new RodType.Builder()
                .baseRodHeat(() -> CNConfigs.server().rods.baseValueThorium.get())
                .proximityRodHeat(() -> (float) CNConfigs.server().rods.thoriumProxyBonus.get())
                .rodTimer(() -> CNConfigs.server().rods.thoriumRodLifetime.get())
                .ratio(() -> CNConfigs.server().rods.thoriumHeatRatio.get())
                .fuelRodType()))
            .tag(Tags.Items.RODS, CNItemTags.FUEL.tag)
            .register(),

        NITRATE = CreateNuclear.REGISTRATE
            .item("nitrate", Item::new)
            .lang("Nitrate")
            .register(),

        NITROGEN_CONCENTRATE = CreateNuclear.REGISTRATE
            .item("nitrogen_concentrate", Item::new)
            .lang("Nitrogen Concentrate")
            .register(),

        COOLED_NITROGEN_CONCENTRATE = CreateNuclear.REGISTRATE
            .item("cooled_nitrogen_concentrate", Item::new)
            .lang("Cooled Nitrogen Concentrate")
            .register()
    ;

    public static final ItemEntry<Helmet> ANTI_RADIATION_HELMETS = CreateNuclear.REGISTRATE
        .item("default_anti_radiation_helmet", Helmet::new)
        .properties(p -> p.stacksTo(1))
        .tag(
            Tags.Items.ARMORS,
            CNTags.forgeItemTag("armors/helmets"),
            CNItemTags.ANTI_RADIATION_ARMOR.tag,
            CNItemTags.ANTI_RADIATION_HELMET.tag
        )
        .transform(setColorComponent(Cloths.DEFAULT))
        .transform(CNArmorMaterials.setArmorDurability(Type.HELMET))
        
        .lang("Anti Radiation Helmet")
        
        .register();

    public static final ItemEntry<Chestplate> ANTI_RADIATION_CHESTPLATES = CreateNuclear.REGISTRATE
            .item("default_anti_radiation_chestplate", Chestplate::new)
            .properties(p -> p.stacksTo(1))
            .tag(
                Tags.Items.ARMORS,
                CNTags.forgeItemTag("armors/chestplates"),
                CNTags.CNItemTags.ANTI_RADIATION_ARMOR.tag
            )
            .transform(setColorComponent(Cloths.DEFAULT))
            .transform(CNArmorMaterials.setArmorDurability(Type.CHESTPLATE))
            
            .lang("Anti Radiation Chestplate")
            
            .register();

    public static final ItemEntry<Leggings> ANTI_RADIATION_LEGGINGS = CreateNuclear.REGISTRATE
            .item("default_anti_radiation_leggings", Leggings::new)
            .properties(p -> p.stacksTo(1))
            .tag(
                Tags.Items.ARMORS,
                CNTags.forgeItemTag("armors/leggings"),
                CNTags.CNItemTags.ANTI_RADIATION_ARMOR.tag
            )
            .transform(setColorComponent(Cloths.DEFAULT))
            .transform(CNArmorMaterials.setArmorDurability(Type.LEGGINGS))
            
            .lang("Anti Radiation Leggings")
            
            .register();

    public static final ItemEntry<Boot> ANTI_RADIATION_BOOTS = CreateNuclear.REGISTRATE
            .item("default_anti_radiation_boots", Boot::new)
            .properties(p -> p.stacksTo(1))
            .tag(
                Tags.Items.ARMORS,
                CNTags.forgeItemTag("armors/boots"),
                CNTags.CNItemTags.ANTI_RADIATION_ARMOR.tag
            )
            .transform(setColorComponent(Cloths.DEFAULT))
            .transform(CNArmorMaterials.setArmorDurability(Type.BOOTS))
            
            .lang("Anti Radiation Boots")
            
            .register();

    public static final DyedItemsList<ClothItem> CLOTHS = new DyedItemsList<>(color -> {
        String colorName = color.getSerializedName();
        List<Item> ingredients = new ArrayList<>(Arrays.asList(Items.WHITE_DYE, Items.ORANGE_DYE, Items.MAGENTA_DYE, Items.LIGHT_BLUE_DYE, Items.YELLOW_DYE, Items.LIME_DYE, Items.PINK_DYE, Items.GRAY_DYE, Items.LIGHT_GRAY_DYE, Items.CYAN_DYE, Items.PURPLE_DYE, Items.BLUE_DYE, Items.BROWN_DYE, Items.GREEN_DYE, Items.RED_DYE, Items.BLACK_DYE));

        return CreateNuclear.REGISTRATE.item(colorName+ "_cloth", p -> new ClothItem(p, color))
            .tag(CNItemTags.CLOTH.tag)
            
            .lang(TextUtils.titleCaseConversion(color.getName()) + " Cloth")
            
            .register();
    });

    public static final ItemEntry<DeferredSpawnEggItem> SPAWN_WOLF = CNBuilderTransformers.spawnEgg("wolf_irradiated_spawn_egg", CNEntityType.IRRADIATED_WOLF, 0x42452B, 0x4C422B, "Irradiated Wolf Spawn Egg");
    public static final ItemEntry<DeferredSpawnEggItem> SPAWN_CAT = CNBuilderTransformers.spawnEgg("cat_irradiated_spawn_egg", CNEntityType.IRRADIATED_CAT, 0x382C19, 0x742728, "Irradiated Cat Spawn Egg");
    public static final ItemEntry<DeferredSpawnEggItem> SPAWN_CHICKEN = CNBuilderTransformers.spawnEgg("chicken_irradiated_spawn_egg", CNEntityType.IRRADIATED_CHICKEN, 0x6B9455, 0x95393C, "Irradiated Chicken Spawn Egg");

    public static final ItemEntry<ReactorBluePrintItem> REACTOR_BLUEPRINT = CreateNuclear.REGISTRATE
        .item("reactor_blueprint_item", ReactorBluePrintItem::new)
        .lang("Reactor Blueprint")
        
        
        .properties(p -> p.stacksTo(1))
        .register();

    public static final ItemEntry<Item> REINFORCED_GLASS_BOTTLE = CreateNuclear.REGISTRATE
        .item("reinforced_glass_bottle", Item::new)
        .lang("Reinforced Glass Bottle")
        
        .properties(p -> p.stacksTo(16))
        .register();

    public static final ItemEntry<BiomeIrradiationExtractorItem> IRRADIATION_BIOME_EXTRACTOR = CreateNuclear.REGISTRATE
        .item("biome_irradiation_extractor", BiomeIrradiationExtractorItem::new)
        .lang("Biome Irradiation Extractor")
        
        .properties(p -> p.stacksTo(16).fireResistant().setNoRepair())
        
        .register();

    public static void register() {}
}
