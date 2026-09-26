package net.nuclearteam.createnuclear.content.contraptions.irradiated.wolf;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.nuclearteam.createnuclear.CreateNuclear;
import net.nuclearteam.createnuclear.content.contraptions.irradiated.CNModelLayers;

@Environment(EnvType.CLIENT)
public class IrradiatedWolfRenderer extends AgeableMobRenderer<IrradiatedWolf, WolfRenderState, IrradiatedWolfModel> {
    private static final Identifier WOLF_LOCATION = CreateNuclear.asResource("textures/entity/irradiated_wolf.png");
    private static final Identifier WOLF_TAME_LOCATION = CreateNuclear.asResource("textures/entity/irradiated_wolf.png");
    private static final Identifier WOLF_ANGRY_LOCATION = CreateNuclear.asResource("textures/entity/irradiated_wolf_angry.png");

    public IrradiatedWolfRenderer(EntityRendererProvider.Context context) {
        super(context,
            new IrradiatedWolfModel(context.bakeLayer(CNModelLayers.IRRADIATED_WOLF)),
            new IrradiatedWolfModel(context.bakeLayer(CNModelLayers.IRRADIATED_WOLF_BABY)),
            0.5F);
    }

    @Override
    public WolfRenderState createRenderState() {
        return new WolfRenderState();
    }

    @Override
    public void extractRenderState(IrradiatedWolf entity, WolfRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.isAngry = entity.isAngry();
        state.isSitting = entity.isInSittingPose();
        state.tailAngle = entity.getTailAngle(); // was getBob
        state.headRollAngle = entity.getHeadRollAngle(partialTicks);
        state.shakeAnim = entity.getShakeAnim(partialTicks);
        state.wetShade = entity.isWet() ? entity.getWetShade(partialTicks) : 1.0F;
        state.texture = entity.isTame() ? WOLF_TAME_LOCATION : entity.isAngry() ? WOLF_ANGRY_LOCATION : WOLF_LOCATION;
    }

    // Wet darkening, as vanilla does it. Upstream's setColor((int) shade) truncated the shade to 0.
    @Override
    protected int getModelTint(WolfRenderState state) {
        float wetShade = state.wetShade;
        return wetShade == 1.0F ? -1 : ARGB.colorFromFloat(1.0F, wetShade, wetShade, wetShade);
    }

    @Override
    public Identifier getTextureLocation(WolfRenderState state) {
        return state.texture;
    }
}
