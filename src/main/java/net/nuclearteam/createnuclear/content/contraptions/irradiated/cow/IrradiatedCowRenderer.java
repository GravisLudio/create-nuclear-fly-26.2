package net.nuclearteam.createnuclear.content.contraptions.irradiated.cow;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.nuclearteam.createnuclear.CreateNuclear;
import net.nuclearteam.createnuclear.content.contraptions.irradiated.CNModelLayers;

@Environment(EnvType.CLIENT)
public class IrradiatedCowRenderer extends MobRenderer<IrradiatedCow, IrradiatedCowModel<IrradiatedCow>> {
    private static final Identifier COW_LOCATION = CreateNuclear.asResource("textures/entity/irradiated_cow.png");

    public IrradiatedCowRenderer(EntityRendererProvider.Context context) {
        super(context, new IrradiatedCowModel<>(context.bakeLayer(CNModelLayers.IRRADIATED_COW)), 0.7f);
    }

    /**
     * Returns the location of an entity's texture.
     */
    public Identifier getTextureLocation(IrradiatedCow pEntity) {
        return COW_LOCATION;
    }
}
