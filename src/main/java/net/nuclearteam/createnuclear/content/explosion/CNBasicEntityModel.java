package net.nuclearteam.createnuclear.content.explosion;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;

/**
 * Root of the Tabula-style models (from Citadel, via Alex's Caves) the mushroom cloud is drawn with.
 * <p>
 * Upstream extended vanilla's {@code EntityModel}, but only ever used it as a vertex emitter:
 * {@code renderToBuffer} walks its own parts, and nothing handed it to an entity renderer. 26.2's
 * {@code EntityModel} is built around render states and baked {@code ModelPart}s, which these
 * parts are not, so the class stands alone and keeps the one method callers use.
 */
@Environment(EnvType.CLIENT)
public abstract class CNBasicEntityModel<T extends Entity> {
    public int textureWidth;
    public int textureHeight;

    protected CNBasicEntityModel() {
        this.textureWidth = 64;
        this.textureHeight = 32;
    }

    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        float alpha = (float) (color >> 24 & 255) / 255.0F;
        this.parts().forEach(part -> part.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha));
    }

    public abstract Iterable<CNBasicModelPart> parts();

    public abstract void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch);

    public void prepareMobModel(T entity, float limbSwing, float limbSwingAmount, float partialTick) {
    }
}
