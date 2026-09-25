package net.nuclearteam.createnuclear.foundation.utility;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

/**
 * Full-screen texture overlays for the HUD.
 * <p>
 * Upstream drove {@code RenderSystem} blend and depth state around a {@code GuiGraphics.blit} at
 * z -90. 26.2's GUI is extracted into render state and drawn later, so blending comes from the
 * {@link RenderPipelines#GUI_TEXTURED} pipeline and the alpha goes in as the blit's colour.
 * The coverage parameter was already a no-op upstream (its scale call was commented out).
 */
@Environment(EnvType.CLIENT)
public class RenderHelper {
    public static void renderOverlay(GuiGraphicsExtractor graphics, Identifier texture,
                                     float alpha, float coverage, boolean onlyFirstPerson) {
        if (onlyFirstPerson && !Minecraft.getInstance().options.getCameraType().isFirstPerson())
            return;
        renderTextureOverlay(graphics, texture, alpha);
    }

    public static void renderOverlay(GuiGraphicsExtractor graphics, Identifier texture, float alpha) {
        renderOverlay(graphics, texture, alpha, 1f, false);
    }

    public static void renderFirstPersonOverlay(GuiGraphicsExtractor graphics, Identifier texture, float alpha, float coverage) {
        renderOverlay(graphics, texture, alpha, coverage, true);
    }

    public static void renderTextureOverlay(GuiGraphicsExtractor graphics, Identifier texture, float alpha) {
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, 0, 0, 0, 0, width, height, width, height, ARGB.white(alpha));
    }
}
