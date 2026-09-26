package net.nuclearteam.createnuclear.content.contraptions.irradiated.cow;

import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.nuclearteam.createnuclear.CreateNuclear;
import net.nuclearteam.createnuclear.content.contraptions.irradiated.CNModelLayers;

@Environment(EnvType.CLIENT)
public class IrradiatedCowRenderer extends AgeableMobRenderer<IrradiatedCow, LivingEntityRenderState, IrradiatedCowModel> {
    private static final Identifier COW_LOCATION = CreateNuclear.asResource("textures/entity/irradiated_cow.png");

    public IrradiatedCowRenderer(EntityRendererProvider.Context context) {
        super(context,
            new IrradiatedCowModel(context.bakeLayer(CNModelLayers.IRRADIATED_COW)),
            new IrradiatedCowModel(context.bakeLayer(CNModelLayers.IRRADIATED_COW_BABY)),
            0.7f);
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) {
        return COW_LOCATION;
    }
}
