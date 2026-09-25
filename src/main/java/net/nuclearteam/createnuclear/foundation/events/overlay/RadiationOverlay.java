package net.nuclearteam.createnuclear.foundation.events.overlay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.GameType;
import net.nuclearteam.createnuclear.CNEffects;
import net.nuclearteam.createnuclear.CreateNuclear;
import net.nuclearteam.createnuclear.foundation.utility.RenderHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

/**
 * HUD overlay for radiation effect when the player is irradiated.
 */
public class RadiationOverlay extends EasingHudOverlay {
    private static final Identifier RADIATION_TEXTURE =
            CreateNuclear.asResource("textures/misc/irradiated_vision/irradiated_vision.png");
    private static float coverage = 1f;

    /**
     * Updates the coverage scale for the radiation effect.
     * @param newCoverage scale factor (1.0 = normal size)
     */
    public static void setCoverage(float newCoverage) {
        coverage = newCoverage;
    }

    @Override
    public Identifier getAfterOverlay() {
        return VanillaHudElements.MISC_OVERLAYS;
    }

    @Override
    public Identifier getOverlayId() {
        return CreateNuclear.asResource("radiation_overlay");
    }

    @Override
    public boolean isActive() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player != null && player.hasEffect(CNEffects.RADIATION);
    }

    @Override
    protected void renderWithAlpha(GuiGraphicsExtractor graphics, float partialTicks, float alpha) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.gameMode == null || mc.gameMode.getPlayerMode() == GameType.SPECTATOR || mc.gameMode.getPlayerMode() == GameType.CREATIVE)
            return;
        if (!mc.options.getCameraType().isFirstPerson())
            return;

        RenderHelper.renderTextureOverlay(graphics, RADIATION_TEXTURE, alpha * coverage);
    }

    @Override
    public int getPriority() {
        return 100; // Fixed background priority for radiation effect
    }
}
