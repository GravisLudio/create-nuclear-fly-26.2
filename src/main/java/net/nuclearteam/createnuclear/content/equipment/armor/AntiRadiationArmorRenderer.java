package net.nuclearteam.createnuclear.content.equipment.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.nuclearteam.createnuclear.CNItems;
import net.nuclearteam.createnuclear.content.contraptions.irradiated.CNModelLayers;
import net.nuclearteam.createnuclear.foundation.utility.ClothTagHelper;

import java.util.EnumMap;
import java.util.Map;

/**
 * Draws the suit with its own model and the cloth-coloured texture. Replaces upstream's
 * {@code IClientItemExtensions.getHumanoidArmorModel} (model) and
 * {@code AntiRadiationArmorTextureMixin} on {@code getArmorTexture} (texture); Fabric skips the
 * vanilla equipment layer for items that have an armor renderer.
 */
@Environment(EnvType.CLIENT)
public final class AntiRadiationArmorRenderer implements ArmorRenderer {
    private final Map<EquipmentSlot, AntiRadiationArmorModel> models = new EnumMap<>(EquipmentSlot.class);

    private AntiRadiationArmorRenderer(EntityRendererProvider.Context context) {
        for (EquipmentSlot slot : new EquipmentSlot[] { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET }) {
            ModelPart root = context.bakeLayer(CNModelLayers.ANTI_RADIATION_ARMOR);
            models.put(slot, new AntiRadiationArmorModel(root, slot));
        }
    }

    public static void register() {
        ArmorRenderer.register(
            (ArmorRenderer.Factory) AntiRadiationArmorRenderer::new,
            CNItems.ANTI_RADIATION_HELMETS.get(),
            CNItems.ANTI_RADIATION_CHESTPLATES.get(),
            CNItems.ANTI_RADIATION_LEGGINGS.get(),
            CNItems.ANTI_RADIATION_BOOTS.get()
        );
    }

    @Override
    public void render(PoseStack poseStack, SubmitNodeCollector collector, ItemStack stack, HumanoidRenderState state,
                       EquipmentSlot slot, int light, HumanoidModel<HumanoidRenderState> contextModel) {
        AntiRadiationArmorModel model = models.get(slot);
        if (model == null) return;

        Identifier texture = ClothTagHelper.getArmorTexturePath(stack, "anti_radiation_suit.png");
        collector.order(1).submitModel(model, state, poseStack, RenderTypes.armorCutoutNoCull(texture), light,
            OverlayTexture.NO_OVERLAY, -1, null, state.outlineColor, null);
        if (stack.hasFoil()) {
            collector.order(2).submitModel(model, state, poseStack, RenderTypes.armorEntityGlint(), light,
                OverlayTexture.NO_OVERLAY, -1, null, state.outlineColor, null);
        }
    }

    @Override
    public boolean shouldRenderDefaultHeadItem(net.minecraft.world.entity.LivingEntity entity, ItemStack stack) {
        return false;
    }
}
