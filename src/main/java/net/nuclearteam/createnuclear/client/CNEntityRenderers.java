package net.nuclearteam.createnuclear.client;

import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
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
import net.nuclearteam.createnuclear.content.equipment.armor.AntiRadiationArmorRenderer;

/**
 * Entity renderers and model layers. Registrate chained the renderers onto entity registration and
 * NeoForge collected the layers from {@code EntityRenderersEvent.RegisterLayerDefinitions}; both
 * are client-only, so they live here and run from the client entrypoint.
 */
public final class CNEntityRenderers {
    private CNEntityRenderers() {
    }

    public static void register() {
        ModelLayerRegistry.registerModelLayer(CNModelLayers.IRRADIATED_CAT, IrradiatedCatModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(CNModelLayers.IRRADIATED_CHICKEN, IrradiatedChickenModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(CNModelLayers.IRRADIATED_WOLF, IrradiatedWolfModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(CNModelLayers.IRRADIATED_COW, IrradiatedCowModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(CNModelLayers.IRRADIATED_CAT_BABY, IrradiatedCatModel::createBabyLayer);
        ModelLayerRegistry.registerModelLayer(CNModelLayers.IRRADIATED_CHICKEN_BABY, IrradiatedChickenModel::createBabyLayer);
        ModelLayerRegistry.registerModelLayer(CNModelLayers.IRRADIATED_WOLF_BABY, IrradiatedWolfModel::createBabyLayer);
        ModelLayerRegistry.registerModelLayer(CNModelLayers.IRRADIATED_COW_BABY, IrradiatedCowModel::createBabyLayer);
        ModelLayerRegistry.registerModelLayer(CNModelLayers.ANTI_RADIATION_ARMOR, AntiRadiationArmorModel::createBodyLayer);

        AntiRadiationArmorRenderer.register();

        EntityRenderers.register(CNEntityType.NUCLEAR_EXPLOSION.get(), NoopRenderer::new);
        EntityRenderers.register(CNEntityType.IRRADIATED_CAT.get(), IrradiatedCatRenderer::new);
        EntityRenderers.register(CNEntityType.IRRADIATED_CHICKEN.get(), IrradiatedChickenRenderer::new);
        EntityRenderers.register(CNEntityType.IRRADIATED_WOLF.get(), IrradiatedWolfRenderer::new);
        EntityRenderers.register(CNEntityType.IRRADIATED_COW.get(), IrradiatedCowRenderer::new);
    }
}
