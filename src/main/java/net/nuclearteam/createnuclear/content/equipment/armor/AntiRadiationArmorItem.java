package net.nuclearteam.createnuclear.content.equipment.armor;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorType;
import net.nuclearteam.createnuclear.CNAttributes;
import net.nuclearteam.createnuclear.CNDataComponents;
import net.nuclearteam.createnuclear.CNItems;
import net.nuclearteam.createnuclear.CreateNuclear;
import net.nuclearteam.createnuclear.content.equipment.cloth.ClothItem.Cloths;
import net.nuclearteam.createnuclear.foundation.advancement.CNAdvancement;
import net.nuclearteam.createnuclear.foundation.registrate.ItemBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.function.UnaryOperator;

/**
 * {@code ArmorItem} is gone in 26.2: armour is a plain {@link Item} whose properties carry the
 * equippable and attribute components ({@code Properties.humanoidArmor}). The irradiation
 * resistance modifier used to be added by overriding {@code getDefaultAttributeModifiers}; it is
 * now part of the attribute component set on the properties.
 */
@SuppressWarnings("unused")
public class AntiRadiationArmorItem extends Item {
    public static final double RADIATION_VALUE = 0.25;

    private final ArmorType type;

    public AntiRadiationArmorItem(ArmorType type, Item.Properties properties) {
        super(armorProperties(type, properties));
        this.type = type;
    }

    private static Item.Properties armorProperties(ArmorType type, Item.Properties properties) {
        return properties
            .humanoidArmor(CNArmorMaterials.ANTI_RADIATION_SUIT, type)
            .attributes(CNArmorMaterials.ANTI_RADIATION_SUIT.createAttributes(type).withModifierAdded(
                CNAttributes.IRRADIATED_RESISTANCE,
                // The id must be unique per slot: modifiers are keyed by Identifier, so sharing one id
                // between the 4 pieces would make them overwrite each other instead of stacking to 1.0.
                new AttributeModifier(CreateNuclear.asResource("armor_resistance_irradiation_" + type.getName()), RADIATION_VALUE, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.bySlot(type.getSlot())
            ));
    }

    public ArmorType getType() {
        return type;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        if (!(entity instanceof Player player)) return;
        if (!stack.has(CNDataComponents.CLOTH_COLOR)) return;
        if (CNAdvancement.DYE_ANTI_RADIATION_ARMOR.isAlreadyAwardedTo(player)) return;
        CNAdvancement.DYE_ANTI_RADIATION_ARMOR.awardTo(player);
    }

    public static <T extends Item> UnaryOperator<ItemBuilder<T>> setColorComponent(Cloths cloths) {
        return b -> b
            .properties(p -> p
                .component(CNDataComponents.CLOTH_COLOR, Cloths.DEFAULT)
            );
    }

    public static class Helmet extends AntiRadiationArmorItem implements IGoggleHelmet {
        public Helmet(Properties p) {
            super(ArmorType.HELMET, p);
        }
    }

    public static class Chestplate extends AntiRadiationArmorItem {
        public Chestplate(Properties p) {
            super(ArmorType.CHESTPLATE, p);
        }
    }

    public static class Leggings extends AntiRadiationArmorItem {
        public Leggings(Properties p) {
            super(ArmorType.LEGGINGS, p);
        }
    }

    public static class Boot extends AntiRadiationArmorItem {
        public Boot(Properties p) {
            super(ArmorType.BOOTS, p);
        }
    }

    public interface IGoggleHelmet {
        static boolean isGoggleHelmet(LivingEntity entity) {
            ItemStack headSlot = entity.getItemBySlot(EquipmentSlot.HEAD);
            return CNItems.ANTI_RADIATION_HELMETS.isIn(headSlot);
        }
    }
}
