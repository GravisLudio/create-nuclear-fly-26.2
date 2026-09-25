package net.nuclearteam.createnuclear.foundation.events.overlay;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Abstract HUD overlay with a smooth fade-in/out (ease-in-out) effect.
 */
public abstract class EasingHudOverlay implements HudOverlay {
    private float progress = 0f;
    protected float fadeSpeed = 0.01f;

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
        // Update progress based on the active state
        progress = isActive()
                ? Math.min(1f, progress + fadeSpeed)
                : Math.max(0f, progress - fadeSpeed);
        if (progress > 0f) {
            renderWithAlpha(guiGraphics, deltaTracker.getRealtimeDeltaTicks(), ease(progress));
        }
    }

    /**
     * Smoothstep interpolation (ease-in-out).
     */
    private float ease(float t) {
        return t * t * (3f - 2f * t);
    }

    /**
     * Renders the overlay with a specific alpha. Subclasses implement the actual drawing here.
     */
    protected abstract void renderWithAlpha(GuiGraphicsExtractor graphics, float partialTicks, float alpha);
}
