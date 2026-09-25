package net.nuclearteam.createnuclear.foundation.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Hud;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.nuclearteam.createnuclear.CNEffects;
import net.nuclearteam.createnuclear.CreateNuclear;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Heart rendering moved from {@code Gui.renderHeart} to {@code Hud.extractHeart} in 26.2, and the
 * sprite blit gained a leading {@code RenderPipeline}, so the sprite is argument 1 now.
 */
@Mixin(Hud.class)
public class RadiationHeartMixin {

    @ModifyArg(
            method = "extractHeart",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"
            ),
            index = 1
    )
    private Identifier CN$changeHeartTexture(Identifier originalTexture) {
        Player player = Minecraft.getInstance().player;

        if (player != null && player.hasEffect(CNEffects.RADIATION)) {
            String path = originalTexture.getPath();
            if (originalTexture.getNamespace().equals("minecraft") && path.startsWith("hud/heart/")) {
                String heartType = path.substring("hud/heart/".length());
                String baseType = heartType.replace("_blinking", "");
                
                if (baseType.equals("full") || baseType.equals("half") || baseType.equals("hardcore_full") || baseType.equals("hardcore_half")) {
                    return CreateNuclear.asResource("hud/heart/radiation_" + baseType);
                }
            }
        }
        return originalTexture;
    }
}