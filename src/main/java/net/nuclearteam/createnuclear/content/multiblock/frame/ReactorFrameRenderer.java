package net.nuclearteam.createnuclear.content.multiblock.frame;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.catnip.render.FluidRenderHelper;
import com.zurrtum.create.client.catnip.render.FluidRenderHelper.FluidRenderState;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerBlockEntity;
import net.nuclearteam.createnuclear.content.multiblock.controller.manager.ReactorFrameDisplayManagerI;
import org.jetbrains.annotations.Nullable;

/**
 * Renders the reactor fluid dynamically inside the {@link ReactorFrame} window.
 * The fluid (texture + tint) is resolved from the owning reactor controller, so
 * the visible liquid reflects whichever fluid the reactor actually uses
 * (water, liquid nitrogen, ...) instead of a texture baked into the model.
 * <p>
 * 26.2 splits block entity rendering into extracting a render state and submitting it later. The
 * geometry is upstream's; the fluid box itself is Create Fly's {@link FluidRenderHelper}, which
 * resolves texture and tint from the fluid stack as upstream's {@code CatnipServices.FLUID_RENDERER}
 * did.
 */
@Environment(EnvType.CLIENT)
public class ReactorFrameRenderer implements BlockEntityRenderer<ReactorFrameEntity, ReactorFrameRenderer.FrameRenderState> {

    // Horizontal interior of the window (1..15 px on a 16 px block).
    private static final float X_MIN = 1f / 16f;
    private static final float X_MAX = 15f / 16f;
    private static final float Z_MIN = 1f / 16f;
    private static final float Z_MAX = 15f / 16f;

    public ReactorFrameRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public FrameRenderState createRenderState() {
        return new FrameRenderState();
    }

    @Override
    public void extractRenderState(ReactorFrameEntity be, FrameRenderState state, float partialTicks, Vec3 cameraPos,
                                   @Nullable CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        state.fluid = null;

        ReactorControllerBlockEntity controller = be.getControllerEntity();
        if (controller == null) return;

        ReactorFrameDisplayManagerI frameDisplay = controller.getFrameDisplayManager();

        FluidStack fluid = frameDisplay.getDisplayedFluid(controller.getLevel(), controller.getInputFluidManager());
        if (fluid == null || fluid.isEmpty()) return;

        // Local vertical bounds of the liquid volume inside this block, matching
        // what used to be baked into each frame part model
        // (frame_top / frame_middle / frame_bottom / frame_none).
        float boxYMin;
        float boxYMax;
        switch (be.getBlockState().getValue(ReactorFrame.PART)) {
            case START -> { boxYMin = 0f;          boxYMax = 9f / 16f; }
            case MIDDLE -> { boxYMin = 0f;         boxYMax = 1f; }
            case END -> { boxYMin = 4f / 16f;      boxYMax = 1f; }
            default -> { boxYMin = 2.9f / 16f;     boxYMax = 9.9f / 16f; }
        }

        // Clamp the liquid to the reactor's global fill level so the whole wall
        // shares one continuous surface that rises from the bottom as the input
        // fills. The level is mapped over the range that liquid actually occupies:
        // from the bottom frame's lip (frameMinY + 4/16) to the top frame's cap
        // (frameMaxY + 9/16), so even a nearly-empty reactor still shows a sliver.
        float yMax = boxYMax;
        if (frameDisplay.hasFrameColumn()) {
            float ratio = frameDisplay.getDisplayedFluidFillRatio(controller.getLevel(), controller.getInputFluidManager());
            double liquidBottomWorldY = frameDisplay.getFrameColumnMinY() + 4.0 / 16.0;
            double liquidTopWorldY = frameDisplay.getFrameColumnMaxY() + 9.0 / 16.0;
            double surfaceWorldY = liquidBottomWorldY + ratio * (liquidTopWorldY - liquidBottomWorldY);
            double localSurface = surfaceWorldY - be.getBlockPos().getY();
            if (localSurface <= boxYMin) return; // liquid level is below this block
            yMax = (float) Math.min(boxYMax, localSurface);
        }

        Level level = be.getLevel();
        int light = level != null ? LightCoordsUtil.getLightCoords(level, be.getBlockPos()) : LightCoordsUtil.FULL_BRIGHT;

        // Last arg (invertGasses) must be false: we always fill bottom-to-top here.
        // The stack, not a bare fluid state, is what carries the tint -- without it water
        // renders almost black in the frame windows.
        state.fluid = FluidRenderHelper.extractFluidRenderState(
            level instanceof BlockAndTintGetter getter ? getter : null,
            be.getBlockPos(),
            Minecraft.getInstance().getModelManager().getFluidStateModelSet(),
            fluid.getFluid(),
            fluid.getComponentChanges(),
            X_MIN, boxYMin, Z_MIN, X_MAX, yMax, Z_MAX,
            light, false, false);
    }

    @Override
    public void submit(FrameRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        if (state.fluid != null)
            state.fluid.submit(poseStack, collector);
    }

    public static class FrameRenderState extends BlockEntityRenderState {
        @Nullable
        FluidRenderState fluid;
    }
}
