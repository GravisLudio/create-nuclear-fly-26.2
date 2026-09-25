package net.nuclearteam.createnuclear.foundation.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.nuclearteam.createnuclear.CNFluids;
import net.nuclearteam.createnuclear.content.radiation.capability.RadiationCapability;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Per-entity tick hooks, which Fabric has no event for. Upstream used two NeoForge events:
 * {@code EntityTickEvent.Pre} for radiation, and {@code LivingVisibilityEvent} -- which fires once
 * per tick on both sides -- for the fluid contact effects.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void createnuclear$radiation(CallbackInfo ci) {
        RadiationCapability.tickRadiation((LivingEntity) (Object) this);
    }

    @Inject(method = "baseTick", at = @At("TAIL"))
    private void createnuclear$fluidEffects(CallbackInfo ci) {
        CNFluids.handleFluidEffect((LivingEntity) (Object) this);
    }
}
