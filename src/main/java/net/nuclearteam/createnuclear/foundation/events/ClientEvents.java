package net.nuclearteam.createnuclear.foundation.events;

import net.minecraft.client.Minecraft;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.fabricmc.api.EnvType;
import net.nuclearteam.createnuclear.CNClientProxy;
import net.nuclearteam.createnuclear.CreateNuclear;
import net.nuclearteam.createnuclear.foundation.mixin.client.CameraAccessor;
import net.nuclearteam.createnuclear.infrastructure.config.CNConfigs;
import net.minecraft.client.Camera;

/**
 * Client handlers that were NeoForge event subscribers. Called from {@code CreateNuclearClient}
 * (client tick) and {@code CameraMixin} (camera shake).
 */
public class ClientEvents {

    /**
     * Ticks down the nuke flash/darken timers, once per client tick.
     */
    public static void onClientTick(Minecraft minecraft) {

            // Store the previous value for smooth interpolation of the flash
            CNClientProxy.prevNukeFlashAmount = CNClientProxy.nukeFlashAmount;

            // Count down the sky-darkening timer
            if (CNClientProxy.renderNukeSkyDarkFor > 0) {
                CNClientProxy.renderNukeSkyDarkFor--;
            }

            // Ramp the white flash up while active, then fade it back out
            if (CNClientProxy.renderNukeFlashFor > 0) {
                if (CNClientProxy.nukeFlashAmount < 1F) {
                    CNClientProxy.nukeFlashAmount = Math.min(CNClientProxy.nukeFlashAmount + 0.4F, 1F);
                }
                CNClientProxy.renderNukeFlashFor--;
            } else if (CNClientProxy.nukeFlashAmount > 0F) {
                CNClientProxy.nukeFlashAmount = Math.max(CNClientProxy.nukeFlashAmount - 0.05F, 0F);
            }
        }

    /**
     * Shakes the player's camera while a nuke explosion is active.
     */
    public static void shakeCamera(Camera camera) {
        Entity player = Minecraft.getInstance().getCameraEntity();

        // Shake at 1.5F while the sky is darkened (nuke active), otherwise no shake
        float tremorAmount = CNClientProxy.renderNukeSkyDarkFor > 0 ? 1.5F : 0F;

        if (player != null && CNConfigs.client().screenShaking.get()) {
            if (tremorAmount > 0) {
                // Generate random offsets for the shake, once per tick
                if (CNClientProxy.lastTremorTick != player.tickCount) {
                    RandomSource rng = player.level().getRandom();
                    CNClientProxy.randomTremorOffsets[0] = rng.nextFloat();
                    CNClientProxy.randomTremorOffsets[1] = rng.nextFloat();
                    CNClientProxy.randomTremorOffsets[2] = rng.nextFloat();
                    CNClientProxy.lastTremorTick = player.tickCount;
                }

                // Scale by Minecraft's screen-effect accessibility setting
                double intensity = tremorAmount * Minecraft.getInstance().options.screenEffectScale().get();

                // Physically offset the camera
                ((CameraAccessor) camera).callMove(
                    (float) (CNClientProxy.randomTremorOffsets[0] * 0.2F * intensity),
                    (float) (CNClientProxy.randomTremorOffsets[1] * 0.2F * intensity),
                    (float) (CNClientProxy.randomTremorOffsets[2] * 0.5F * intensity)
                );

            }
        }
    }
}
