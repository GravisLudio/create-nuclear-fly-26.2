package net.nuclearteam.createnuclear.content.multiblock.output;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.content.kinetics.base.SingleKineticRenderState;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * The output's half shaft, for when Flywheel is off. Upstream extended
 * {@code KineticBlockEntityRenderer} and only swapped the model; 26.2's renderers extract state and
 * submit it later, so this follows Create Fly's {@code CreativeMotorRenderer}, which draws the same
 * facing half shaft.
 */
@Environment(EnvType.CLIENT)
public class ReactorOutputRenderer implements BlockEntityRenderer<ReactorOutputEntity, SingleKineticRenderState> {

    public ReactorOutputRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public SingleKineticRenderState createRenderState() {
        return new SingleKineticRenderState();
    }

    @Override
    public void extractRenderState(ReactorOutputEntity be, SingleKineticRenderState state, float partialTicks,
                                   Vec3 cameraPosition, @Nullable CrumblingOverlay breakProgress) {
        Level level = SmartBlockEntityRenderer.extractBase(be, state, breakProgress);
        Direction facing = state.blockState.getValue(ReactorOutput.FACING);
        state.model = CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state.blockState, facing)
            .cardinalLighting(level).light(state.lightCoords).color(KineticBlockEntityRenderer.getTintColor(be))
            .extractRenderState();
        state.angle = KineticBlockEntityRenderer.getRotateAngleWithoutBeOffset(facing.getAxis(), be, state, level);
    }

    @Override
    public void submit(SingleKineticRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState camera) {
        state.submit(matrices, queue);
    }
}
