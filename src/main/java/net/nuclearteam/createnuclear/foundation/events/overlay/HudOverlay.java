package net.nuclearteam.createnuclear.foundation.events.overlay;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.resources.Identifier;

/**
 * Base interface for all HUD overlays. They were NeoForge GUI layers registered above
 * {@code VanillaGuiLayers.CAMERA_OVERLAYS}; on Fabric they are HUD elements attached after the
 * equivalent vanilla element, {@code VanillaHudElements.MISC_OVERLAYS}.
 */
public interface HudOverlay extends HudElement {
    Identifier getAfterOverlay();

    Identifier getOverlayId();

    boolean isActive();

    int getPriority();

    default void register() {
        HudElementRegistry.attachElementAfter(getAfterOverlay(), getOverlayId(), this);
    }
}
