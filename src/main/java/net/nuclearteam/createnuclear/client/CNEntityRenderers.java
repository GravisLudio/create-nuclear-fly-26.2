package net.nuclearteam.createnuclear.client;

import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.nuclearteam.createnuclear.CNEntityType;
import net.nuclearteam.createnuclear.content.contraptions.irradiated.CNModelLayers;
import net.nuclearteam.createnuclear.content.contraptions.irradiated.cat.IrradiatedCatModel;
import net.nuclearteam.createnuclear.content.contraptions.irradiated.cat.IrradiatedCatRenderer;
import net.nuclearteam.createnuclear.content.contraptions.irradiated.chicken.IrradiatedChickenModel;
import net.nuclearteam.createnuclear.content.contraptions.irradiated.chicken.IrradiatedChickenRenderer;
import net.nuclearteam.createnuclear.content.contraptions.irradiated.cow.IrradiatedCowModel;
import net.nuclearteam.createnuclear.content.contraptions.irradiated.cow.IrradiatedCowRenderer;
import net.nuclearteam.createnuclear.content.contraptions.irradiated.wolf.IrradiatedWolfModel;
import net.nuclearteam.createnuclear.content.contraptions.irradiated.wolf.IrradiatedWolfRenderer;
import net.nuclearteam.createnuclear.content.equipment.armor.AntiRadiationArmorModel;

/**
 * Entity renderers and model layers. Registrate chained the renderers onto entity registration and
 * NeoForge collected the layers from {@code EntityRenderersEvent.RegisterLayerDefinitions}; both
 * are client-only, so they live here and run from the client entrypoint.
 */
public final class CNEntityRenderers {
    private CNEntityRenderers() {
    }

    public static void register() {
        EntityModelLayerRegistry.registerModelLayer(CNModelLayers.IRRADIATED_CAT, IrradiatedCatModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(CNModelLayers.IRRADIATED_CHICKEN, IrradiatedChickenModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(CNModelLayers.IRRADIATED_WOLF, IrradiatedWolfModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(CNModelLayers.IRRADIATED_COW, IrradiatedCowModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(CNModelLayers.ANTI_RADIATION_ARMOR, AntiRadiationArmorModel::createBodyLayer);

        EntityRenderers.register(CNEntityType.NUCLEAR_EXPLOSION.get(), NoopRenderer::new);
        EntityRenderers.register(CNEntityType.IRRADIATED_CAT.get(), IrradiatedCatRenderer::new);
        EntityRenderers.register(CNEntityType.IRRADIATED_CHICKEN.get(), IrradiatedChickenRenderer::new);
        EntityRenderers.register(CNEntityType.IRRADIATED_WOLF.get(), IrradiatedWolfRenderer::new);
        EntityRenderers.register(CNEntityType.IRRADIATED_COW.get(), IrradiatedCowRenderer::new);
    }
}
