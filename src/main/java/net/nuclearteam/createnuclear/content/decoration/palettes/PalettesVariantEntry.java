package net.nuclearteam.createnuclear.content.decoration.palettes;

import com.google.common.collect.ImmutableList;
import net.nuclearteam.createnuclear.foundation.registrate.CNRegistrate;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.DataIngredient;
import net.nuclearteam.createnuclear.foundation.registrate.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.nuclearteam.createnuclear.CreateNuclear;

import static com.simibubi.create.foundation.data.CNRegistrate.connectedTextures;
import static net.nuclearteam.createnuclear.foundation.registrate.TagGen.pickaxeOnly;

public class PalettesVariantEntry {
    private static final CNRegistrate REGISTRATE = CreateNuclear.REGISTRATE;

    public final ImmutableList<BlockEntry<? extends Block>> registeredBlocks;
    public final ImmutableList<BlockEntry<? extends Block>> registeredPartials;

    public PalettesVariantEntry(String name, CNPaletteStoneTypes paletteStoneVariants) {
        ImmutableList.Builder<BlockEntry<? extends Block>> registeredBlocks = ImmutableList.builder();
        ImmutableList.Builder<BlockEntry<? extends Block>> registeredPartials = ImmutableList.builder();
        NonNullSupplier<Block> baseBlock = paletteStoneVariants.baseBlock;

        for (PaletteBlockPattern pattern : paletteStoneVariants.variantTypes) {
            BlockBuilder<? extends Block, CNRegistrate> builder = REGISTRATE
                    .block(pattern.createName(name), pattern.getBlockFactory())
                    .initialProperties(baseBlock)
                    .transform(pickaxeOnly())
                    ;
            ItemBuilder<BlockItem, ? extends BlockBuilder<? extends Block, CNRegistrate>> itemBuilder =
                    builder.item();

            TagKey<Block>[] blockTags = pattern.getBlockTags();
            if (blockTags != null)
                builder.tag(blockTags);
            TagKey<Item>[] itemTags = pattern.getItemTags();
            if (itemTags != null)
                itemBuilder.tag(itemTags);

            itemBuilder.tag(paletteStoneVariants.materialTag);

            if (pattern.isTranslucent())
                builder.addLayer(() -> RenderType::translucent);
            pattern.createCTBehaviour(name)
                    .ifPresent(b -> builder.onRegister(connectedTextures(b)));


            itemBuilder.register();
            BlockEntry<? extends Block> block = builder.register();
            registeredBlocks.add(block);

            for (PaletteBlockPartial<? extends Block> partialBlock : pattern.getPartials())
                registeredPartials.add(partialBlock.create(name, pattern, block, paletteStoneVariants)
                        .register());
        }

        REGISTRATE.addDataGenerator(ProviderType.RECIPE,
                p -> p.stonecutting(DataIngredient.tag(paletteStoneVariants.materialTag), RecipeCategory.BUILDING_BLOCKS,
                        baseBlock));
        REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, p -> p.addTag(paletteStoneVariants.materialTag)
                .add(baseBlock.get()
                        .asItem()));

        this.registeredBlocks = registeredBlocks.build();
        this.registeredPartials = registeredPartials.build();
    }
}
