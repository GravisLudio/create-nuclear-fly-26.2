package net.nuclearteam.createnuclear;

import net.minecraft.world.entity.EntityTypes;
import com.mojang.logging.LogUtils;
import com.zurrtum.create.content.equipment.goggles.GogglesItem;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.nuclearteam.createnuclear.content.contraptions.irradiated.IrradiatedAnimal;
import net.nuclearteam.createnuclear.content.decoration.palettes.CNPaletteBlocks;
import net.nuclearteam.createnuclear.content.equipment.armor.AntiRadiationArmorItem;
import net.nuclearteam.createnuclear.content.kinetics.fan.processing.CNFanProcessingTypes;
import net.nuclearteam.createnuclear.content.radiation.CNRadiationValues;
import net.nuclearteam.createnuclear.foundation.transfer.CNTransfer;
import net.nuclearteam.createnuclear.foundation.advancement.CNAdvancement;
import net.nuclearteam.createnuclear.foundation.advancement.CNTriggers;
import net.nuclearteam.createnuclear.foundation.registrate.CNRegistrate;
import net.nuclearteam.createnuclear.impl.registry.CreateNuclearRegistriesImpl;
import net.nuclearteam.createnuclear.infrastructure.config.CNConfigs;
import net.nuclearteam.createnuclear.infrastructure.worldgen.CNBiomeModifiers;
import net.nuclearteam.createnuclear.infrastructure.worldgen.CNPlacementModifiers;
import org.slf4j.Logger;

/**
 * Fabric main entrypoint (was a NeoForge {@code @Mod} class).
 * <p>
 * NeoForge deferred everything through the mod event bus: registries on {@code RegisterEvent},
 * setup on {@code FMLCommonSetupEvent}. On Fabric {@code onInitialize} already runs at the right
 * point and every registry here registers eagerly, so those phases become straight-line calls.
 * Order still matters in the usual places: effects and attributes before the items that reference
 * them, entity types before the spawn eggs, blocks before block entities.
 * <p>
 * Upstream's Registrate tooltip modifier (item description, kinetic stats, rod stats) is
 * client-only and moved to {@code client.CNItemTooltips}.
 */
public class CreateNuclear implements ModInitializer {
    public static final String MOD_ID = "createnuclear";
    public static final Logger LOGGER = LogUtils.getLogger();

    /**
     * <b>Other mods should not use this field!</b> It is the port's Registrate stand-in and only
     * tracks what this mod registers.
     */
    public static final CNRegistrate REGISTRATE = CNRegistrate.create(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("{} initializing", MOD_ID);

        // Were keyed off RegisterEvent for their registries.
        CNSoundEvents.register();
        CNAttributes.register();
        CNEffects.register();
        CNPotions.register();
        CNDataComponents.register();
        CNAttachmentTypes.register();
        CNParticleTypes.register();
        CNParticleRegistry.register();
        CNRecipeTypes.register();
        CNPlacementModifiers.register();
        CreateNuclearRegistriesImpl.register();

        CNDisplaySources.register();
        CNTags.init();
        CNEntityType.register();
        CNFluids.register();
        CNBlocks.register();
        CNPaletteBlocks.register();
        CNBlockEntityTypes.register();
        CNItems.register();
        CNMenus.register();
        CNCreativeModeTabs.register();
        CNFanProcessingTypes.register();

        CNConfigs.register();
        CNBiomeModifiers.register();

        CNAdvancement.register();
        CNTriggers.register();

        GogglesItem.addIsWearingPredicate(AntiRadiationArmorItem.IGoggleHelmet::isGoggleHelmet);

        // Previously FMLCommonSetupEvent. Every registry these read is populated by now.
        CNFluids.registerFluidInteractions();
        CNRadiationValues.register();
        CNOpenPipeEffectHandlers.registerDefaults();
        CNTransfer.register();
        IrradiatedAnimal.VANILLA_TO_IRRADIATED.put(EntityTypes.CHICKEN, CNEntityType.IRRADIATED_CHICKEN.get());
    }

    public static Identifier asResource(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
