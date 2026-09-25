package net.nuclearteam.createnuclear.content.decoration.palettes;

import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

/**
 * The mod's palette stone types. Upstream generated each variant from a copy of Create's
 * {@code PaletteBlockPattern} machinery, most of which existed to drive datagen; the blocks are
 * registered explicitly in {@link CNPaletteBlocks} now, the way Create Fly registers its own
 * palettes, and this enum only keeps the handle other code reads.
 */
public enum CNPaletteStoneTypes {
    AUTUNITE(() -> CNPaletteBlocks.AUTUNITE.get());

    public final Supplier<Block> baseBlock;

    CNPaletteStoneTypes(Supplier<Block> baseBlock) {
        this.baseBlock = baseBlock;
    }

    public Supplier<Block> getBaseBlock() {
        return baseBlock;
    }
}
