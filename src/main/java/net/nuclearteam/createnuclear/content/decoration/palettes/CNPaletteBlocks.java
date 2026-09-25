package net.nuclearteam.createnuclear.content.decoration.palettes;

import com.zurrtum.create.content.decoration.palettes.ConnectedPillarBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.material.MapColor;
import net.nuclearteam.createnuclear.CreateNuclear;
import net.nuclearteam.createnuclear.foundation.registrate.BlockEntry;
import net.nuclearteam.createnuclear.foundation.registrate.CNRegistrate;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Autunite palette blocks, listed out the way Create Fly lists its own granite, diorite and so on.
 * <p>
 * Upstream derived these from {@code PaletteBlockPattern.STANDARD_RANGE}: cut, polished cut, cut
 * bricks and small bricks (each with stairs, slab and wall), layered and pillar. The names, the
 * base properties ({@code paletteStoneBlock("autunite", () -> Blocks.ANDESITE, ...)} with the
 * destroy time and map colour set on it) and the "variant copies its pattern block" rule are
 * upstream's; they match the committed blockstates one for one. Tags, models, loot and recipes
 * were datagen and are committed JSON. The connected textures of the layered and pillar variants
 * are client-side, in {@code client.CNConnectedTextures}.
 */
public class CNPaletteBlocks {
    private static final CNRegistrate REGISTRATE = CreateNuclear.REGISTRATE;

    public static final BlockEntry<Block> AUTUNITE = REGISTRATE.block("autunite", Block::new)
        .initialProperties(() -> Blocks.ANDESITE)
        .properties(p -> p.destroyTime(1.25f).mapColor(MapColor.COLOR_GREEN))
        .simpleItem()
        .register();

    public static final BlockEntry<Block> CUT_AUTUNITE = variant("cut_autunite", Block::new);
    public static final BlockEntry<StairBlock> CUT_AUTUNITE_STAIRS = stairs("cut_autunite_stairs", CUT_AUTUNITE);
    public static final BlockEntry<SlabBlock> CUT_AUTUNITE_SLAB = slab("cut_autunite_slab", CUT_AUTUNITE);
    public static final BlockEntry<WallBlock> CUT_AUTUNITE_WALL = wall("cut_autunite_wall", CUT_AUTUNITE);

    public static final BlockEntry<Block> POLISHED_CUT_AUTUNITE = variant("polished_cut_autunite", Block::new);
    public static final BlockEntry<StairBlock> POLISHED_CUT_AUTUNITE_STAIRS = stairs("polished_cut_autunite_stairs", POLISHED_CUT_AUTUNITE);
    public static final BlockEntry<SlabBlock> POLISHED_CUT_AUTUNITE_SLAB = slab("polished_cut_autunite_slab", POLISHED_CUT_AUTUNITE);
    public static final BlockEntry<WallBlock> POLISHED_CUT_AUTUNITE_WALL = wall("polished_cut_autunite_wall", POLISHED_CUT_AUTUNITE);

    public static final BlockEntry<Block> CUT_AUTUNITE_BRICKS = variant("cut_autunite_bricks", Block::new);
    public static final BlockEntry<StairBlock> CUT_AUTUNITE_BRICK_STAIRS = stairs("cut_autunite_brick_stairs", CUT_AUTUNITE_BRICKS);
    public static final BlockEntry<SlabBlock> CUT_AUTUNITE_BRICK_SLAB = slab("cut_autunite_brick_slab", CUT_AUTUNITE_BRICKS);
    public static final BlockEntry<WallBlock> CUT_AUTUNITE_BRICK_WALL = wall("cut_autunite_brick_wall", CUT_AUTUNITE_BRICKS);

    public static final BlockEntry<Block> SMALL_AUTUNITE_BRICKS = variant("small_autunite_bricks", Block::new);
    public static final BlockEntry<StairBlock> SMALL_AUTUNITE_BRICK_STAIRS = stairs("small_autunite_brick_stairs", SMALL_AUTUNITE_BRICKS);
    public static final BlockEntry<SlabBlock> SMALL_AUTUNITE_BRICK_SLAB = slab("small_autunite_brick_slab", SMALL_AUTUNITE_BRICKS);
    public static final BlockEntry<WallBlock> SMALL_AUTUNITE_BRICK_WALL = wall("small_autunite_brick_wall", SMALL_AUTUNITE_BRICKS);

    public static final BlockEntry<Block> LAYERED_AUTUNITE = variant("layered_autunite", Block::new);
    public static final BlockEntry<ConnectedPillarBlock> AUTUNITE_PILLAR = variant("autunite_pillar", ConnectedPillarBlock::new);

    private static <T extends Block> BlockEntry<T> variant(String name, Function<Block.Properties, T> factory) {
        return REGISTRATE.block(name, factory)
            .initialProperties(AUTUNITE::get)
            .simpleItem()
            .register();
    }

    private static BlockEntry<StairBlock> stairs(String name, BlockEntry<? extends Block> base) {
        return REGISTRATE.block(name, p -> new StairBlock(base.getDefaultState(), p))
            .initialProperties(base::get)
            .simpleItem()
            .register();
    }

    private static BlockEntry<SlabBlock> slab(String name, BlockEntry<? extends Block> base) {
        return REGISTRATE.block(name, SlabBlock::new)
            .initialProperties(base::get)
            .simpleItem()
            .register();
    }

    private static BlockEntry<WallBlock> wall(String name, BlockEntry<? extends Block> base) {
        return REGISTRATE.block(name, WallBlock::new)
            .initialProperties(base::get)
            .properties(p -> p.forceSolidOn())
            .simpleItem()
            .register();
    }

    public static void register() {
    }
}
