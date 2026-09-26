package net.nuclearteam.createnuclear;

import com.zurrtum.create.api.stress.BlockStressValues;
import net.nuclearteam.createnuclear.foundation.registrate.CNRegistrate;
import net.nuclearteam.createnuclear.foundation.registrate.SharedProperties;
import net.nuclearteam.createnuclear.foundation.registrate.BlockEntry;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.nuclearteam.createnuclear.content.enriching.campfire.EnrichingCampfireBlock;
import net.nuclearteam.createnuclear.content.enriching.fire.EnrichingFireBlock;
import net.nuclearteam.createnuclear.content.multiblock.alarm.ReactorAlarm;
import net.nuclearteam.createnuclear.content.multiblock.casing.ReactorCasing;
import net.nuclearteam.createnuclear.CNTags.CNBlockTags;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerBlock;
import net.nuclearteam.createnuclear.content.multiblock.core.ReactorCore;
import net.nuclearteam.createnuclear.content.multiblock.frame.ReactorFrame;
import net.nuclearteam.createnuclear.content.multiblock.frame.ReactorframeItem;
import net.nuclearteam.createnuclear.content.multiblock.input.fluid.ReactorFluidInput;
import net.nuclearteam.createnuclear.content.multiblock.input.item.ReactorRodInput;
import net.nuclearteam.createnuclear.content.multiblock.output.ReactorOutput;
import net.nuclearteam.createnuclear.content.multiblock.cooler.ReactorCooler;
import net.nuclearteam.createnuclear.content.multiblock.reinforced.ReinforcedGlassBlock;
import net.nuclearteam.createnuclear.content.uraniumOre.UraniumOreBlock;
import net.nuclearteam.createnuclear.content.uraniumOre.UraniumOreItem;

import static net.nuclearteam.createnuclear.foundation.registrate.CNBehaviours.displaySource;
import static net.nuclearteam.createnuclear.foundation.registrate.TagGen.axeOrPickaxe;
import static net.nuclearteam.createnuclear.foundation.registrate.TagGen.pickaxeOnly;

public class CNBlocks {

    public static final BlockEntry<ReactorCasing> REACTOR_CASING = CreateNuclear.REGISTRATE
        .block("reactor_casing", properties -> new ReactorCasing(properties, ReactorCasing.TypeBlock.CASING))
        .properties(p -> p
            .explosionResistance(3F)
            .destroyTime(4F)
        )

        .simpleItem()
        .transform(pickaxeOnly())
        .transform(displaySource(CNDisplaySources.HEAT))
        .transform(displaySource(CNDisplaySources.LIQUID_LEVEL))
        .transform(displaySource(CNDisplaySources.FUEL))
        .transform(displaySource(CNDisplaySources.COOLER))
        .transform(displaySource(CNDisplaySources.REACTOR_SIZE))
        .transform(displaySource(CNDisplaySources.REACTOR_SUMMARY))
        .register();

    public static final BlockEntry<ReactorCore> REACTOR_CORE = CreateNuclear.REGISTRATE
        .block("reactor_core", ReactorCore::new)
        .properties(p -> p.explosionResistance(6F))
        .properties(p -> p.destroyTime(4F))

        .transform(pickaxeOnly())
        .simpleItem()
        .register();

    public static final BlockEntry<ReactorFrame> REACTOR_FRAME = CreateNuclear.REGISTRATE
        .block("reactor_frame", ReactorFrame::new)
        .initialProperties(SharedProperties::stone)
        .properties(p -> p.explosionResistance(3F).destroyTime(2F).noOcclusion())
        .transform(pickaxeOnly())

        .item(ReactorframeItem::new)

        .build()
        .register();

    public static final BlockEntry<ReactorCooler> REACTOR_COOLER = CreateNuclear.REGISTRATE
        .block("reactor_cooler", ReactorCooler::new)
        .properties(p -> p
            .explosionResistance(3F)
            .destroyTime(4F))

        .simpleItem()
        .transform(pickaxeOnly())
        .register();

    public static final BlockEntry<ReactorRodInput> REACTOR_ROD_INPUT = CreateNuclear.REGISTRATE
        .block("reactor_rod_input", ReactorRodInput::new)
        .initialProperties(SharedProperties::stone)
        .properties(p -> p
            .explosionResistance(6F)
            .destroyTime(2F)
        )
        .transform(pickaxeOnly())

        .item()

        .register();

    public static final BlockEntry<ReactorFluidInput> REACTOR_FLUID_INPUT = CreateNuclear.REGISTRATE
        .block("reactor_fluid_input", ReactorFluidInput::new)
        .initialProperties(SharedProperties::stone)
        .properties(p -> p
            .explosionResistance(6F)
            .destroyTime(2F)
        )
        .transform(pickaxeOnly())

        .item()

        .register();

    public static final BlockEntry<ReactorOutput> REACTOR_OUTPUT = CreateNuclear.REGISTRATE
        .block("reactor_output", ReactorOutput::new)
        .initialProperties(SharedProperties::stone)
        .properties(p -> p
            .explosionResistance(6F)
            .destroyTime(4F)
            .mapColor(MapColor.COLOR_PURPLE)
            .forceSolidOn()
        )

        .transform(pickaxeOnly())

        .onRegister(block -> BlockStressValues.CAPACITIES.register(block, () -> 64000.0))
        .item()

        .register();

    public static final BlockEntry<ReactorControllerBlock> REACTOR_CONTROLLER = CreateNuclear.REGISTRATE
        .block("reactor_controller", ReactorControllerBlock::new)
        .initialProperties(SharedProperties::stone)
        .properties(p -> p
            .explosionResistance(6F)
            .destroyTime(4F)
        )
        .transform(pickaxeOnly())

        .transform(displaySource(CNDisplaySources.HEAT))
        .transform(displaySource(CNDisplaySources.LIQUID_LEVEL))
        .transform(displaySource(CNDisplaySources.FUEL))
        .transform(displaySource(CNDisplaySources.COOLER))
        .transform(displaySource(CNDisplaySources.REACTOR_SIZE))
        .transform(displaySource(CNDisplaySources.REACTOR_SUMMARY))
        .item()

        .register();

    public static final BlockEntry<ReactorAlarm> REACTOR_ALARM = CreateNuclear.REGISTRATE
        .block("reactor_alarm", ReactorAlarm::new)
        .initialProperties(SharedProperties::stone)
        .transform(pickaxeOnly())
        .item()
        .build()
        .register();

    public static final BlockEntry<ReinforcedGlassBlock> REINFORCED_GLASS = CreateNuclear.REGISTRATE
        .block("reinforced_glass", ReinforcedGlassBlock::new)
        .initialProperties(() -> Blocks.GLASS)
        .properties(p -> p
            .explosionResistance(1200.0F)
            .destroyTime(2F)
        )

        .item()

        .build()
        .register();

    public static final BlockEntry<EnrichingFireBlock> ENRICHING_FIRE = CreateNuclear.REGISTRATE
        .block("enriching_fire", properties -> new EnrichingFireBlock(properties, 3.0f))
        .initialProperties(() -> Blocks.FIRE)
        .properties(Properties::replaceable)
        .properties(Properties::noCollision)
        .properties(Properties::noOcclusion)
        .properties(EnrichingFireBlock.getLight())

        .register();

    public static final BlockEntry<EnrichingCampfireBlock> ENRICHING_CAMPFIRE = CreateNuclear.REGISTRATE
        .block("enriching_campfire", properties -> new EnrichingCampfireBlock(properties, true, 5))
        .properties(p -> p.mapColor(MapColor.PODZOL)
            .instrument(NoteBlockInstrument.BASS)
            .strength(2.0F)
            .sound(SoundType.WOOD)
            .lightLevel(EnrichingCampfireBlock::getLight))
        .properties(Properties::noOcclusion)
        .properties(Properties::ignitedByLava)
        .transform(axeOrPickaxe())

        .item()

        .build()

        .register();

    public static final BlockEntry<Block> ENRICHED_SOUL_SOIL = CreateNuclear.REGISTRATE
        .block("enriched_soul_soil", Block::new)
        .initialProperties(() -> Blocks.SOUL_SOIL)

        .simpleItem()

        .register();

    public static final BlockEntry<UraniumOreBlock> DEEPSLATE_URANIUM_ORE = CreateNuclear.REGISTRATE
        .block("deepslate_uranium_ore", UraniumOreBlock::new)
        .initialProperties(() -> Blocks.DIAMOND_ORE)
        .properties(UraniumOreBlock.litBlockEmission())
        .transform(pickaxeOnly())

        .item((b, p) -> new UraniumOreItem(b, p, 3))

        .build()
        .register();

    public static final BlockEntry<Block> DEEPSLATE_LEAD_ORE = CreateNuclear.REGISTRATE
        .block("deepslate_lead_ore", Block::new)
        .initialProperties(() -> Blocks.DIAMOND_ORE)
        .simpleItem()
        .transform(pickaxeOnly())

        .item()

        .build()
        .register();

    public static final BlockEntry<Block> DEEPSLATE_THORIUM_ORE = CreateNuclear.REGISTRATE
        .block("deepslate_thorium_ore", Block::new)
        .initialProperties(() -> Blocks.DIAMOND_ORE)
        .simpleItem()
        .transform(pickaxeOnly())

        .item()

        .build()
        .register();

    public static final BlockEntry<UraniumOreBlock> URANIUM_ORE = CreateNuclear.REGISTRATE
        .block("uranium_ore", UraniumOreBlock::new)
        .initialProperties(SharedProperties::stone)
        .properties(UraniumOreBlock.litBlockEmission())
        .simpleItem()
        .transform(pickaxeOnly())

        .item((b, p) -> new UraniumOreItem(b, p, 3))

        .build()
        .register();

    public static final BlockEntry<Block> LEAD_ORE = CreateNuclear.REGISTRATE
        .block("lead_ore", Block::new)
        .initialProperties(SharedProperties::stone)
        .simpleItem()
        .transform(pickaxeOnly())

        .item()

        .build()
        .register();

    public static final BlockEntry<Block> THORIUM_ORE = CreateNuclear.REGISTRATE
        .block("thorium_ore", Block::new)
        .initialProperties(SharedProperties::stone)
        .simpleItem()
        .transform(pickaxeOnly())

        .item()

        .build()
        .register();

    public static final BlockEntry<Block> NITRATE_ORE = CreateNuclear.REGISTRATE
        .block("nitrate_ore", Block::new)
        .initialProperties(SharedProperties::stone)
        .transform(pickaxeOnly())

        .item()

        .build()
        .register();

    public static final BlockEntry<Block> DEEPSLATE_NITRATE_ORE = CreateNuclear.REGISTRATE
        .block("deepslate_nitrate_ore", Block::new)
        .initialProperties(SharedProperties::stone)
        .simpleItem()
        .transform(pickaxeOnly())

        .item()

        .build()
        .register();

    public static final BlockEntry<Block> RAW_URANIUM_BLOCK = CreateNuclear.REGISTRATE
        .block("raw_uranium_block", Block::new)
        .initialProperties(SharedProperties::stone)
        .transform(pickaxeOnly())

        .item((b, p) -> new UraniumOreItem(b, p, 27))

        .build()
        .register();

    public static final BlockEntry<Block> RAW_LEAD_BLOCK = CreateNuclear.REGISTRATE
        .block("raw_lead_block", Block::new)
        .initialProperties(SharedProperties::stone)
        .transform(pickaxeOnly())

        .item()

        .build()
        .register();

    public static final BlockEntry<Block> RAW_THORIUM_BLOCK = CreateNuclear.REGISTRATE
        .block("raw_thorium_block", Block::new)
        .initialProperties(SharedProperties::stone)
        .transform(pickaxeOnly())

        .item()

        .build()
        .register();

    public static final BlockEntry<Block> LEAD_BLOCK = CreateNuclear.REGISTRATE
        .block("lead_block", Block::new)
        .initialProperties(SharedProperties::stone)
        .transform(pickaxeOnly())

        .item()

        .build()
        .register();

    public static final BlockEntry<Block> THORIUM_BLOCK = CreateNuclear.REGISTRATE
        .block("thorium_block", Block::new)
        .initialProperties(SharedProperties::stone)
        .transform(pickaxeOnly())

        .item()

        .build()
        .register();

    public static final BlockEntry<Block> STEEL_BLOCK = CreateNuclear.REGISTRATE
        .block("steel_block", Block::new)
        .initialProperties(SharedProperties::stone)
        .transform(pickaxeOnly())

        .item()

        .build()
        .register();

    public static void register() {
        CreateNuclear.LOGGER.info("Registering ModBlocks for " + CreateNuclear.MOD_ID);
    }
}
