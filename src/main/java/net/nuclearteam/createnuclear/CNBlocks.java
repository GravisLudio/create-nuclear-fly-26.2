package net.nuclearteam.createnuclear;

import com.simibubi.create.AllTags;
import com.zurrtum.create.api.stress.BlockStressValues;
import com.zurrtum.create.client.content.decoration.encasing.EncasedCTBehaviour;
import com.simibubi.create.foundation.data.AssetLookup;
import net.nuclearteam.createnuclear.foundation.registrate.CNRegistrate;
import net.nuclearteam.createnuclear.foundation.registrate.SharedProperties;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.providers.loot.RegistrateBlockLootTables;
import net.nuclearteam.createnuclear.foundation.registrate.BlockEntry;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.Tags;
import net.nuclearteam.createnuclear.content.enriching.campfire.EnrichingCampfireBlock;
import net.nuclearteam.createnuclear.content.enriching.fire.EnrichingFireBlock;
import net.nuclearteam.createnuclear.content.multiblock.alarm.ReactorAlarm;
import net.nuclearteam.createnuclear.content.multiblock.casing.ReactorCasing;
import net.nuclearteam.createnuclear.CNTags.CNBlockTags;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerBlock;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerGenerator;
import net.nuclearteam.createnuclear.content.multiblock.core.ReactorCore;
import net.nuclearteam.createnuclear.content.multiblock.frame.ReactorFrame;
import net.nuclearteam.createnuclear.content.multiblock.frame.ReactorframeItem;
import net.nuclearteam.createnuclear.content.multiblock.input.fluid.ReactorFluidInput;
import net.nuclearteam.createnuclear.content.multiblock.input.fluid.ReactorFluidInputGenerator;
import net.nuclearteam.createnuclear.content.multiblock.input.item.ReactorRodInput;
import net.nuclearteam.createnuclear.content.multiblock.input.item.ReactorRodInputGenerator;
import net.nuclearteam.createnuclear.content.multiblock.output.ReactorOutput;
import net.nuclearteam.createnuclear.content.multiblock.output.ReactorOutputGenerator;
import net.nuclearteam.createnuclear.content.multiblock.cooler.ReactorCooler;
import net.nuclearteam.createnuclear.content.multiblock.reinforced.ReinforcedGlassBlock;
import net.nuclearteam.createnuclear.content.uraniumOre.UraniumOreBlock;
import net.nuclearteam.createnuclear.content.uraniumOre.UraniumOreItem;

import static net.nuclearteam.createnuclear.foundation.registrate.CNBehaviours.displaySource;
import static com.simibubi.create.foundation.data.CNRegistrate.casingConnectivity;
import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static net.nuclearteam.createnuclear.foundation.registrate.TagGen.axeOrPickaxe;
import static net.nuclearteam.createnuclear.foundation.registrate.TagGen.pickaxeOnly;

public class CNBlocks {

    static {
        CreateNuclear.REGISTRATE.setCreativeTab(CNCreativeModeTabs.MAIN);
    }

    public static final BlockEntry<ReactorCasing> REACTOR_CASING = CreateNuclear.REGISTRATE
        .block("reactor_casing", properties -> new ReactorCasing(properties, ReactorCasing.TypeBlock.CASING))
        .properties(p -> p
            .explosionResistance(3F)
            .destroyTime(4F)
        )

        .onRegister(CNRegistrate.connectedTextures(() -> new EncasedCTBehaviour(CNSpriteShifts.REACTOR_CASING)))
        .onRegister(casingConnectivity((block, cc) -> cc.makeCasing(block, CNSpriteShifts.REACTOR_CASING)))
        .tag(BlockTags.NEEDS_DIAMOND_TOOL)
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
        .tag(BlockTags.NEEDS_DIAMOND_TOOL)

        .transform(pickaxeOnly())
        .simpleItem()
        .register();

    public static final BlockEntry<ReactorFrame> REACTOR_FRAME = CreateNuclear.REGISTRATE
        .block("reactor_frame", ReactorFrame::new)
        .initialProperties(SharedProperties::stone)
        .properties(p -> p.explosionResistance(3F).destroyTime(2F).noOcclusion())
        .transform(pickaxeOnly())
        .tag(BlockTags.NEEDS_DIAMOND_TOOL)

        .item(ReactorframeItem::new)

        .build()
        .register();

    public static final BlockEntry<ReactorCooler> REACTOR_COOLER = CreateNuclear.REGISTRATE
        .block("reactor_cooler", ReactorCooler::new)
        .properties(p -> p
            .explosionResistance(3F)
            .destroyTime(4F))

        .tag(BlockTags.NEEDS_DIAMOND_TOOL)
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
        .tag(BlockTags.NEEDS_DIAMOND_TOOL)

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
        .tag(BlockTags.NEEDS_DIAMOND_TOOL)

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
        .tag(AllTags.AllBlockTags.SAFE_NBT.tag, BlockTags.NEEDS_DIAMOND_TOOL)
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
        .tag(BlockTags.NEEDS_DIAMOND_TOOL)

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
        .onRegister(CNRegistrate.connectedTextures(() -> new EncasedCTBehaviour(CNSpriteShifts.REACTOR_GLASS)))
        .onRegister(casingConnectivity((block,cc) -> cc.makeCasing(block, CNSpriteShifts.REACTOR_GLASS)))

        .tag(Tags.Blocks.GLASS_BLOCKS, BlockTags.IMPERMEABLE)


        .item()
        .tag(Tags.Items.GLASS_BLOCKS)
        .build()
        .register();

    public static final BlockEntry<EnrichingFireBlock> ENRICHING_FIRE = CreateNuclear.REGISTRATE
        .block("enriching_fire", properties -> new EnrichingFireBlock(properties, 3.0f))
        .initialProperties(() -> Blocks.FIRE)
        .properties(Properties::replaceable)
        .properties(Properties::noCollission)
        .properties(Properties::noOcclusion)
        .properties(EnrichingFireBlock.getLight())
        .tag(CNBlockTags.FAN_PROCESSING_CATALYSTS_ENRICHED.tag)


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
        .tag(CNBlockTags.FAN_PROCESSING_CATALYSTS_ENRICHED.tag)
        .register();


    public static final BlockEntry<Block> ENRICHED_SOUL_SOIL = CreateNuclear.REGISTRATE
        .block("enriched_soul_soil", Block::new)
        .initialProperties(() -> Blocks.SOUL_SOIL)

        .simpleItem()
        .tag(BlockTags.MINEABLE_WITH_SHOVEL)
        .tag(CNBlockTags.ENRICHING_FIRE_BASE_BLOCKS.tag, BlockTags.NEEDS_DIAMOND_TOOL)
        .register();

    public static final BlockEntry<UraniumOreBlock> DEEPSLATE_URANIUM_ORE = CreateNuclear.REGISTRATE
        .block("deepslate_uranium_ore", UraniumOreBlock::new)
        .initialProperties(() -> Blocks.DIAMOND_ORE)
        .properties(UraniumOreBlock.litBlockEmission())
        .transform(pickaxeOnly())

        .tag(
            BlockTags.NEEDS_DIAMOND_TOOL,
            BlockTags.NEEDS_IRON_TOOL,
            Tags.Blocks.ORES,
            Tags.Blocks.ORES_IN_GROUND_DEEPSLATE,
            CNTags.forgeBlockTag("ores/uranium")
        )
        .item((b, p) -> new UraniumOreItem(b, p, 3))
        .tag(Tags.Items.ORES, CNTags.forgeItemTag("ores/uranium"))
        .build()
        .register();

    public static final BlockEntry<Block> DEEPSLATE_LEAD_ORE = CreateNuclear.REGISTRATE
        .block("deepslate_lead_ore", Block::new)
        .initialProperties(() -> Blocks.DIAMOND_ORE)
        .simpleItem()
        .transform(pickaxeOnly())

        .tag(
            BlockTags.NEEDS_IRON_TOOL,
            Tags.Blocks.ORES,
            Tags.Blocks.ORES_IN_GROUND_DEEPSLATE,
            CNTags.forgeBlockTag("ores/lead")
        )
        .item()
        .tag(Tags.Items.ORES, CNTags.forgeItemTag("ores/lead"))
        .build()
        .register();

    public static final BlockEntry<Block> DEEPSLATE_THORIUM_ORE = CreateNuclear.REGISTRATE
        .block("deepslate_thorium_ore", Block::new)
        .initialProperties(() -> Blocks.DIAMOND_ORE)
        .simpleItem()
        .transform(pickaxeOnly())

        .tag(
            BlockTags.NEEDS_DIAMOND_TOOL,
            BlockTags.NEEDS_IRON_TOOL,
            Tags.Blocks.ORES,
            Tags.Blocks.ORES_IN_GROUND_DEEPSLATE,
            CNTags.forgeBlockTag("ores/thorium")
        )
        .item()
        .tag(Tags.Items.ORES, CNTags.forgeItemTag("ores/thorium"))
        .build()
        .register();

    public static final BlockEntry<UraniumOreBlock> URANIUM_ORE = CreateNuclear.REGISTRATE
        .block("uranium_ore", UraniumOreBlock::new)
        .initialProperties(SharedProperties::stone)
        .properties(UraniumOreBlock.litBlockEmission())
        .simpleItem()
        .transform(pickaxeOnly())

        .tag(
            BlockTags.NEEDS_DIAMOND_TOOL,
            BlockTags.NEEDS_IRON_TOOL,
            Tags.Blocks.ORES,
            Tags.Blocks.ORES_IN_GROUND_STONE,
            CNTags.forgeBlockTag("ores/uranium")
        )
        .item((b, p) -> new UraniumOreItem(b, p, 3))
        .tag(Tags.Items.ORES, CNTags.forgeItemTag("ores/uranium"))
        .build()
        .register();

    public static final BlockEntry<Block> LEAD_ORE = CreateNuclear.REGISTRATE
        .block("lead_ore", Block::new)
        .initialProperties(SharedProperties::stone)
        .simpleItem()
        .transform(pickaxeOnly())

        .tag(
            BlockTags.NEEDS_IRON_TOOL,
            Tags.Blocks.ORES,
            Tags.Blocks.ORES_IN_GROUND_STONE,
            CNTags.forgeBlockTag("ores/lead")
        )
        .item()
        .tag(Tags.Items.ORES, CNTags.forgeItemTag("ores/lead"))
        .build()
        .register();


    public static final BlockEntry<Block> THORIUM_ORE = CreateNuclear.REGISTRATE
        .block("thorium_ore", Block::new)
        .initialProperties(SharedProperties::stone)
        .simpleItem()
        .transform(pickaxeOnly())

        .tag(
            BlockTags.NEEDS_DIAMOND_TOOL,
            BlockTags.NEEDS_IRON_TOOL,
            Tags.Blocks.ORES,
            Tags.Blocks.ORES_IN_GROUND_STONE,
            CNTags.forgeBlockTag("ores/thorium")
        )
        .item()
        .tag(Tags.Items.ORES, CNTags.forgeItemTag("ores/thorium"))
        .build()
        .register();

    public static final BlockEntry<Block> NITRATE_ORE = CreateNuclear.REGISTRATE
        .block("nitrate_ore", Block::new)
        .initialProperties(SharedProperties::stone)
        .transform(pickaxeOnly())

        .tag(
            BlockTags.NEEDS_IRON_TOOL,
            Tags.Blocks.ORES,
            Tags.Blocks.ORES_IN_GROUND_STONE,
            CNTags.forgeBlockTag("ores/nitrate")
        )
        .item()
        .tag(Tags.Items.ORES, CNTags.forgeItemTag("ores/nitrate"))
        .build()
        .register();

    public static final BlockEntry<Block> DEEPSLATE_NITRATE_ORE = CreateNuclear.REGISTRATE
        .block("deepslate_nitrate_ore", Block::new)
        .initialProperties(SharedProperties::stone)
        .simpleItem()
        .transform(pickaxeOnly())

        .tag(
            BlockTags.NEEDS_IRON_TOOL,
            Tags.Blocks.ORES,
            Tags.Blocks.ORES_IN_GROUND_DEEPSLATE,
            CNTags.forgeBlockTag("ores/nitrate")
        )
        .item()
        .tag(Tags.Items.ORES, CNTags.forgeItemTag("ores/nitrate"))
        .build()
        .register();

    public static final BlockEntry<Block> RAW_URANIUM_BLOCK = CreateNuclear.REGISTRATE
        .block("raw_uranium_block", Block::new)
        .initialProperties(SharedProperties::stone)
        .transform(pickaxeOnly())
        .tag(
            BlockTags.NEEDS_DIAMOND_TOOL,
            Tags.Blocks.STORAGE_BLOCKS,
            CNTags.forgeBlockTag("storage_blocks/raw_uranium")
        )

        .item((b, p) -> new UraniumOreItem(b, p, 27))
        .tag(
            Tags.Items.STORAGE_BLOCKS,
            CNTags.forgeItemTag("storage_blocks/raw_uranium")
        )
        .build()
        .register();

    public static final BlockEntry<Block> RAW_LEAD_BLOCK = CreateNuclear.REGISTRATE
        .block("raw_lead_block", Block::new)
        .initialProperties(SharedProperties::stone)
        .transform(pickaxeOnly())
        .tag(
            Tags.Blocks.STORAGE_BLOCKS,
            CNTags.forgeBlockTag("storage_blocks/raw_lead")
        )

        .item()
        .tag(
            Tags.Items.STORAGE_BLOCKS,
            CNTags.forgeItemTag("storage_blocks/raw_lead")
        )
        .build()
        .register();


    public static final BlockEntry<Block> RAW_THORIUM_BLOCK = CreateNuclear.REGISTRATE
        .block("raw_thorium_block", Block::new)
        .initialProperties(SharedProperties::stone)
        .transform(pickaxeOnly())
        .tag(
            BlockTags.NEEDS_DIAMOND_TOOL,
            Tags.Blocks.STORAGE_BLOCKS,
            CNTags.forgeBlockTag("storage_blocks/raw_thorium")
        )

        .item()
        .tag(
            Tags.Items.STORAGE_BLOCKS,
            CNTags.forgeItemTag("storage_blocks/raw_thorium")
        )
        .build()
        .register();

    public static final BlockEntry<Block> LEAD_BLOCK = CreateNuclear.REGISTRATE
        .block("lead_block", Block::new)
        .initialProperties(SharedProperties::stone)
        .transform(pickaxeOnly())
        .tag(
            Tags.Blocks.STORAGE_BLOCKS,
            CNTags.forgeBlockTag("storage_blocks/lead")
        )
        .item()
        .tag(
            Tags.Items.STORAGE_BLOCKS,
            CNTags.forgeItemTag("storage_blocks/lead")
        )
        .build()
        .register();

    public static final BlockEntry<Block> THORIUM_BLOCK = CreateNuclear.REGISTRATE
        .block("thorium_block", Block::new)
        .initialProperties(SharedProperties::stone)
        .transform(pickaxeOnly())
        .tag(
            Tags.Blocks.STORAGE_BLOCKS,
            CNTags.forgeBlockTag("storage_blocks/thorium")
        )
        .item()
        .tag(
            Tags.Items.STORAGE_BLOCKS,
            CNTags.forgeItemTag("storage_blocks/thorium")
        )
        .build()
        .register();

    public static final BlockEntry<Block> STEEL_BLOCK = CreateNuclear.REGISTRATE
        .block("steel_block", Block::new)
        .initialProperties(SharedProperties::stone)
        .transform(pickaxeOnly())
        .tag(
            Tags.Blocks.STORAGE_BLOCKS,
            CNTags.forgeBlockTag("storage_blocks/steel")
        )
        .item()
        .tag(
            Tags.Items.STORAGE_BLOCKS,
            CNTags.forgeItemTag("storage_blocks/steel")
        )
        .build()
        .register();

    public static void register() {
        CreateNuclear.LOGGER.info("Registering ModBlocks for " + CreateNuclear.MOD_ID);
    }
}
