package net.nuclearteam.createnuclear.foundation.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.nuclearteam.createnuclear.CNAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Gives every living entity the irradiation resistance attribute. Upstream added it to all types
 * from NeoForge's {@code EntityAttributeModificationEvent}; Fabric has no such event, and every
 * living entity's attribute builder starts from {@code createLivingAttributes}.
 * <p>
 * It has to be on every entity, not only the ones wearing the suit: {@code getAttributeValue}
 * throws for an attribute the entity was built without.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityAttributesMixin {
    @Inject(method = "createLivingAttributes", at = @At("RETURN"))
    private static void createnuclear$addIrradiatedResistance(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        cir.getReturnValue().add(CNAttributes.IRRADIATED_RESISTANCE);
    }
}
