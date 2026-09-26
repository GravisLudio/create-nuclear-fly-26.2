package net.nuclearteam.createnuclear;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.resources.Identifier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.nuclearteam.createnuclear.infrastructure.config.CNConfigs;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.nuclearteam.createnuclear.foundation.utility.RenderHelper;

@Environment(EnvType.CLIENT)
public class CNClientProxy {

    public static final Identifier BOMB_FLASH = CreateNuclear.asResource("textures/misc/bomb_flash.png");

    public static int muteNonNukeSoundsFor = 0;
    public static int renderNukeFlashFor = 0;
    public static int renderNukeSkyDarkFor = 0;
    public static float prevNukeFlashAmount = 0;
    public static float nukeFlashAmount = 0;

    public static int lastTremorTick = -1;
    public static float[] randomTremorOffsets = new float[3];

    public static final Int2ObjectMap<AbstractTickableSoundInstance> ENTITY_SOUND_INSTANCE_MAP = new Int2ObjectOpenHashMap<>();

    public static float getNukeFlashAmount(float partialTicks) {
        return prevNukeFlashAmount + (nukeFlashAmount - prevNukeFlashAmount) * partialTicks;
    }

    /**
     * The white flash of a nuclear explosion. Upstream drew it with raw {@code RenderSystem} and
     * {@code Tesselator} calls from a {@code GameRenderer.render} mixin, just before the GUI; 26.2
     * has neither that immediate-mode path nor the injection point, so it is the first HUD element
     * instead, registered from {@code CreateNuclearClient}. Same texture, alpha and config toggle.
     */
    public static void renderNukeFlash(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        float screenEffectIntensity = Minecraft.getInstance().options.screenEffectScale().get().floatValue();
        float currentNukeFlash = getNukeFlashAmount(deltaTracker.getGameTimeDeltaPartialTick(false));

        if (currentNukeFlash > 0 && CNConfigs.client().nuclearBombFlash.get()) {
            RenderHelper.renderTextureOverlay(graphics, BOMB_FLASH, currentNukeFlash * screenEffectIntensity);
        }
    }

    public static boolean isFarFromCamera(double x, double y, double z) {
        return Minecraft.getInstance().gameRenderer.mainCamera().position().distanceToSqr(x, y, z) >= 256.0D;
    }
}