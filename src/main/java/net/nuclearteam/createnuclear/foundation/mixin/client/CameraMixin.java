package net.nuclearteam.createnuclear.foundation.mixin.client;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.nuclearteam.createnuclear.foundation.events.ClientEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The nuclear explosion camera shake. Upstream hooked NeoForge's
 * {@code ViewportEvent.ComputeCameraAngles}, which fires once the camera is placed for the frame;
 * the end of {@code Camera.update} is the same point.
 */
@Mixin(Camera.class)
public abstract class CameraMixin {
    @Inject(method = "update", at = @At("TAIL"))
    private void createnuclear$shake(DeltaTracker deltaTracker, CallbackInfo ci) {
        ClientEvents.shakeCamera((Camera) (Object) this);
    }
}
