package net.nuclearteam.createnuclear.content.contraptions.irradiated.chicken;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ChickenRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.nuclearteam.createnuclear.CreateNuclear;
import net.nuclearteam.createnuclear.content.contraptions.irradiated.CNModelLayers;

@Environment(EnvType.CLIENT)
public class IrradiatedChickenRenderer extends AgeableMobRenderer<IrradiatedChicken, ChickenRenderState, IrradiatedChickenModel> {
    private static final Identifier IRRADIATED_CHICKEN_LOCATION = CreateNuclear.asResource("textures/entity/irradiated_chicken.png");

    public IrradiatedChickenRenderer(EntityRendererProvider.Context context) {
        super(context,
            new IrradiatedChickenModel(context.bakeLayer(CNModelLayers.IRRADIATED_CHICKEN)),
            new IrradiatedChickenModel(context.bakeLayer(CNModelLayers.IRRADIATED_CHICKEN_BABY)),
            0.3F);
    }

    @Override
    public ChickenRenderState createRenderState() {
        return new ChickenRenderState();
    }

    // Was getBob: the wing flap now travels in the render state.
    @Override
    public void extractRenderState(IrradiatedChicken entity, ChickenRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.flap = Mth.lerp(partialTicks, entity.oFlap, entity.flap);
        state.flapSpeed = Mth.lerp(partialTicks, entity.oFlapSpeed, entity.flapSpeed);
    }

    @Override
    public Identifier getTextureLocation(ChickenRenderState state) {
        return IRRADIATED_CHICKEN_LOCATION;
    }
}
