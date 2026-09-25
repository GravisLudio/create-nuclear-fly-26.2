package net.nuclearteam.createnuclear.content.equipment.armor;

import com.google.common.collect.Maps;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.nuclearteam.createnuclear.CreateNuclear;

import java.util.Map;

/**
 * Armor materials stopped being a registry in 1.21.2: {@link ArmorMaterial} is a plain record now,
 * applied through {@code Item.Properties.humanoidArmor}. Same shape as Create Fly's
 * {@code AllArmorMaterials}.
 * <p>
 * The values are upstream's: defence {@code {2, 4, 3, 1, 4}} was indexed by the old
 * {@code ArmorItem.Type} ordinal (helmet, chestplate, leggings, boots, body), and durability used
 * a multiplier of 15. The repair ingredient was {@code Ingredient.of(LEAD_INGOT)}; materials only
 * take an item tag now, so it is {@code createnuclear:repairs_anti_radiation_armor}, which lists
 * the lead ingot.
 */
public class CNArmorMaterials {
    public static final TagKey<Item> REPAIRS_ANTI_RADIATION_ARMOR =
        TagKey.create(Registries.ITEM, CreateNuclear.asResource("repairs_anti_radiation_armor"));

    public static final ResourceKey<EquipmentAsset> ANTI_RADIATION_SUIT_ASSET =
        ResourceKey.create(EquipmentAssets.ROOT_ID, CreateNuclear.asResource("anti_radiation_suit"));

    public static final ArmorMaterial ANTI_RADIATION_SUIT = new ArmorMaterial(
        15,
        Maps.newEnumMap(Map.of(
            ArmorType.HELMET, 2,
            ArmorType.CHESTPLATE, 4,
            ArmorType.LEGGINGS, 3,
            ArmorType.BOOTS, 1,
            ArmorType.BODY, 4
        )),
        12,
        SoundEvents.ARMOR_EQUIP_NETHERITE,
        0.0f,
        0.0f,
        REPAIRS_ANTI_RADIATION_ARMOR,
        ANTI_RADIATION_SUIT_ASSET
    );

    public static int durabilityForType(ArmorType type) {
        return type.getDurability(ANTI_RADIATION_SUIT.durability());
    }
}
