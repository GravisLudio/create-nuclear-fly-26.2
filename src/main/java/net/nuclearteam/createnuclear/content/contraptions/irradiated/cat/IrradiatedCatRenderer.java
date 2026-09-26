package net.nuclearteam.createnuclear.content.contraptions.irradiated.cat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.CatRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.nuclearteam.createnuclear.CreateNuclear;
import net.nuclearteam.createnuclear.content.contraptions.irradiated.CNModelLayers;

import java.util.List;

@Environment(EnvType.CLIENT)
public class IrradiatedCatRenderer extends AgeableMobRenderer<IrradiatedCat, CatRenderState, IrradiatedCatModel> {
    private static final Identifier IRRADIATED_CAT_LOCATION = CreateNuclear.asResource("textures/entity/irradiated_cat.png");

    public IrradiatedCatRenderer(EntityRendererProvider.Context context) {
        super(context,
            new IrradiatedCatModel(context.bakeLayer(CNModelLayers.IRRADIATED_CAT)),
            new IrradiatedCatModel(context.bakeLayer(CNModelLayers.IRRADIATED_CAT_BABY)),
            0.4f);
    }

    @Override
    public CatRenderState createRenderState() {
        return new CatRenderState();
    }

    @Override
    public void extractRenderState(IrradiatedCat entity, CatRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.isCrouching = entity.isCrouching();
        state.isSprinting = entity.isSprinting();
        state.isSitting = entity.isInSittingPose();
        state.lieDownAmount = entity.getLieDownAmount(partialTicks);
        state.lieDownAmountTail = entity.getLieDownAmountTail(partialTicks);
        state.relaxStateOneAmount = entity.getRelaxStateOneAmount(partialTicks);
        state.isLyingOnTopOfSleepingPlayer = state.lieDownAmount > 0.0F && isNextToSleepingPlayer(entity);
    }

    // The world lookup used to run while rendering; the state is filled on the render thread too, so it stays here.
    private static boolean isNextToSleepingPlayer(IrradiatedCat entity) {
        BlockPos blockPos = entity.blockPosition();
        List<Player> list = entity.level().getEntitiesOfClass(Player.class, (new AABB(blockPos)).inflate(2.0, 2.0, 2.0));
        for (Player player : list) {
            if (player.isSleeping()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Identifier getTextureLocation(CatRenderState state) {
        return IRRADIATED_CAT_LOCATION;
    }

    @Override
    protected void scale(CatRenderState state, PoseStack matrixStack) {
        super.scale(state, matrixStack);
        matrixStack.scale(0.8F, 0.8F, 0.8F);
    }

    @Override
    protected void setupRotations(CatRenderState state, PoseStack poseStack, float bodyRot, float entityScale) {
        super.setupRotations(state, poseStack, bodyRot, entityScale);

        float f = state.lieDownAmount;
        if (f > 0.0F) {
            poseStack.translate(0.4F * f, 0.15F * f, 0.1F * f);
            poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.rotLerp(f, 0.0F, 90.0F)));
            if (state.isLyingOnTopOfSleepingPlayer) {
                poseStack.translate(0.15F * f, 0.0F, 0.0F);
            }
        }
    }
}
