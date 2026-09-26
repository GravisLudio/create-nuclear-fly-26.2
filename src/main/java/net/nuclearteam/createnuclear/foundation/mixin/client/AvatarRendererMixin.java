package net.nuclearteam.createnuclear.foundation.mixin.client;

import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import net.nuclearteam.createnuclear.content.equipment.armor.AntiRadiationArmorItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hides the skin's outer layers (hat, jacket, sleeves, pants) under the suit, so they don't poke
 * through it. Upstream did this on the model from {@code RenderPlayerEvent.Pre}; in 26.2 the
 * player model reads these flags from the render state, which is filled here.
 */
@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin {
    @Inject(
        method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",
        at = @At("TAIL")
    )
    private void createnuclear$hideLayersUnderSuit(Avatar entity, AvatarRenderState state, float partialTicks, CallbackInfo ci) {
        if (state.headEquipment.getItem() instanceof AntiRadiationArmorItem.Helmet) {
            state.showHat = false;
        }
        if (state.chestEquipment.getItem() instanceof AntiRadiationArmorItem.Chestplate) {
            state.showJacket = false;
            state.showLeftSleeve = false;
            state.showRightSleeve = false;
        }
        if (state.legsEquipment.getItem() instanceof AntiRadiationArmorItem.Leggings) {
            state.showLeftPants = false;
            state.showRightPants = false;
        }
    }
}
