package net.nuclearteam.createnuclear.foundation.mixin.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.nuclearteam.createnuclear.CNClientProxy;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    // Was darkenWorldAmount in 1.21.1.
    @Shadow
    private float bossOverlayWorldDarkening;

    // Handles darkening the sky/world during a nuclear explosion
    @Inject(
            method = "tick",
            at = @At(value = "TAIL")
    )
    public void CN$tick(CallbackInfo ci) {
        if (CNClientProxy.renderNukeSkyDarkFor > 0 && bossOverlayWorldDarkening < 1.0F) {
            bossOverlayWorldDarkening = Math.min(bossOverlayWorldDarkening + 0.3F, 1.0F);
        }
    }
}
