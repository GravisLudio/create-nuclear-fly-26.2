package net.nuclearteam.createnuclear;


import com.mojang.logging.LogUtils;
import com.simibubi.create.CreateBuildInfo;
import com.zurrtum.create.content.equipment.goggles.GogglesItem;
import net.nuclearteam.createnuclear.foundation.registrate.CNRegistrate;
import com.zurrtum.create.client.foundation.item.ItemDescription;
import com.zurrtum.create.client.foundation.item.KineticStats;
import net.nuclearteam.createnuclear.content.equipment.armor.AntiRadiationArmorItem;
import net.nuclearteam.createnuclear.foundation.item.RodsStats;
import com.zurrtum.create.client.foundation.item.TooltipModifier;
import com.zurrtum.create.client.catnip.lang.FontHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.nuclearteam.createnuclear.content.contraptions.irradiated.IrradiatedAnimal;
import net.nuclearteam.createnuclear.content.decoration.palettes.CNPaletteBlocks;
import net.nuclearteam.createnuclear.content.equipment.armor.CNArmorMaterials;
import net.nuclearteam.createnuclear.content.kinetics.fan.processing.CNFanProcessingTypes;
import net.nuclearteam.createnuclear.content.radiation.CNRadiationValues;
import net.nuclearteam.createnuclear.foundation.advancement.CNAdvancement;
import net.nuclearteam.createnuclear.foundation.advancement.CNTriggers;
import net.nuclearteam.createnuclear.infrastructure.config.CNConfigs;
import net.nuclearteam.createnuclear.infrastructure.data.CreateNuclearDatagen;
import net.nuclearteam.createnuclear.infrastructure.worldgen.CNPlacementModifiers;
import net.nuclearteam.createnuclear.infrastructure.worldgen.biome.surfacerule.BiomeTagRule;
import org.slf4j.Logger;

import com.simibubi.create.api.registrate.CreateRegistrateRegistrationCallback;

@Mod(CreateNuclear.MOD_ID)
public class CreateNuclear {
    public static final String MOD_ID = "createnuclear";
    public static final Logger LOGGER = LogUtils.getLogger();

    /**
     * <b>Other mods should not use this field!</b> If you are an addon developer, create your own instance of
     * {@link CNRegistrate}.
     * </br
     * If you were using this instance to render a callback listener use {@link CreateRegistrateRegistrationCallback#register} instead.
     */
    public static final CNRegistrate REGISTRATE = CNRegistrate.create(MOD_ID)
            .defaultCreativeTab((ResourceKey<CreativeModeTab>) null)
            .setTooltipModifierFactory(item ->
                    new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
                            .andThen(TooltipModifier.mapNull(KineticStats.create(item)))
                            .andThen(RodsStats.create(item))
            );

    public CreateNuclear(IEventBus eventBus, ModContainer modContainer) {
        onCtor(eventBus, modContainer);
    }

    public static void onCtor(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("{} {} initializing! Commit hash: {}", MOD_ID, CreateBuildInfo.VERSION, CreateBuildInfo.GIT_COMMIT);

        ModLoadingContext modLoadingContext = ModLoadingContext.get();

        IEventBus forgeEventBus = NeoForge.EVENT_BUS;

        REGISTRATE.registerEventListeners(modEventBus);

        CNSoundEvents.prepare();
        CNDisplaySources.register();
        CNTags.init();
        CNBlocks.register();
        CNBlockEntityTypes.register();
        CNItems.register();
        CNPackets.register();
        CNMenus.register();
        CNFluids.register();
        CNEntityType.register();
        CNPaletteBlocks.register();

        CNArmorMaterials.register(modEventBus);
        CNDataComponents.register(modEventBus);
        CNAttachmentTypes.register(modEventBus);

        CNConfigs.register(modLoadingContext, modContainer);

        CNCreativeModeTabs.register(modEventBus);
        CNEffects.register(modEventBus);
        CNPotions.register(modEventBus);
        CNParticleTypes.register(modEventBus);
        CNParticleRegistry.DEF_REG.register(modEventBus);
        CNRecipeTypes.register(modEventBus);
        CNAttributes.register(modEventBus);
        CNPlacementModifiers.register(modEventBus);

        GogglesItem.addIsWearingPredicate(AntiRadiationArmorItem.IGoggleHelmet::isGoggleHelmet);

        modEventBus.addListener(CreateNuclear::init);
        modEventBus.addListener(CreateNuclear::onRegister);
        modEventBus.addListener(EventPriority.LOWEST, CreateNuclearDatagen::gatherData);
        forgeEventBus.addListener(CNFluids::handleFluidEffect);

        modEventBus.addListener(EventPriority.HIGHEST, CreateNuclearDatagen::gatherDataHighPriority);

        //DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> CreateNuclearClient.onCtorClient(modEventBus, forgeEventBus));
    }

    public static void init(final FMLCommonSetupEvent event) {
        CNFluids.registerFluidInteractions();
        CNRadiationValues.register();

        event.enqueueWork(CNOpenPipeEffectHandlers::registerDefaults);
        event.enqueueWork(() -> IrradiatedAnimal.VANILLA_TO_IRRADIATED.put(EntityType.CHICKEN, CNEntityType.IRRADIATED_CHICKEN.get()));
    }

    public static void onRegister(final RegisterEvent event) {
        CNSoundEvents.register(event);
        CNFanProcessingTypes.register();
        BiomeTagRule.register(event);

        if (event.getRegistry() == BuiltInRegistries.TRIGGER_TYPES) {
            CNAdvancement.register();
            CNTriggers.register();
        }
    }


    public static Identifier asResource(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

}
