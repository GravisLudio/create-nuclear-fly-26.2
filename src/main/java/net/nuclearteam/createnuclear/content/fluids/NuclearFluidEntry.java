package net.nuclearteam.createnuclear.content.fluids;

import com.zurrtum.create.infrastructure.fluids.FlowableFluid;
import com.zurrtum.create.infrastructure.fluids.FluidBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

/**
 * A still/flowing fluid pair with its block and bucket, replacing Registrate's {@code standardFluid}.
 * <p>
 * Create Fly's own {@code FluidEntry} is the model, but its flow parameters are fixed; upstream's
 * {@code fluidProperties} set a different drop-off, tick rate and slope distance per fluid, so the
 * pair here reads them from the entry. Ids follow Registrate's scheme: {@code <name>} for the
 * source, {@code flowing_<name>} for the flowing fluid, {@code <name>_bucket} for the bucket.
 * <p>
 * What NeoForge's {@code FluidType} carried beyond flow -- viscosity, density, swim and drown
 * behaviour -- has no vanilla equivalent; entity physics in vanilla follow fluid tags.
 */
public final class NuclearFluidEntry {
    public final Identifier id;
    public final FlowableFluid still = new Still();
    public final FlowableFluid flowing = new Flowing();
    public FluidBlock block;
    public BucketItem bucket;

    private final int dropOff;
    private final int tickDelay;
    private final int slopeFindDistance;
    private final Optional<SoundEvent> pickupSound;

    private NuclearFluidEntry(Identifier id, int dropOff, int tickDelay, int slopeFindDistance,
                              Optional<SoundEvent> pickupSound) {
        this.id = id;
        this.dropOff = dropOff;
        this.tickDelay = tickDelay;
        this.slopeFindDistance = slopeFindDistance;
        this.pickupSound = pickupSound;
    }

    public static NuclearFluidEntry register(Identifier id, int dropOff, int tickDelay, int slopeFindDistance,
                                             Optional<SoundEvent> pickupSound,
                                             UnaryOperator<BlockBehaviour.Properties> blockProperties,
                                             BiFunction<Fluid, Item.Properties, ? extends BucketItem> bucketFactory) {
        NuclearFluidEntry entry = new NuclearFluidEntry(id, dropOff, tickDelay, slopeFindDistance, pickupSound);

        Registry.register(BuiltInRegistries.FLUID, id, entry.still);
        Registry.register(BuiltInRegistries.FLUID, id.withPrefix("flowing_"), entry.flowing);

        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id);
        entry.block = (FluidBlock) Blocks.register(blockKey, p -> new FluidBlock(entry.still, p),
            blockProperties.apply(BlockBehaviour.Properties.ofFullCopy(Blocks.WATER)));

        Identifier bucketId = id.withSuffix("_bucket");
        ResourceKey<Item> bucketKey = ResourceKey.create(Registries.ITEM, bucketId);
        entry.bucket = Registry.register(BuiltInRegistries.ITEM, bucketId, bucketFactory.apply(entry.still,
            new Item.Properties().setId(bucketKey).craftRemainder(Items.BUCKET).stacksTo(1)));
        return entry;
    }

    public FlowableFluid get() {
        return still;
    }

    public FlowableFluid getSource() {
        return still;
    }

    public FlowableFluid getFlowing() {
        return flowing;
    }

    public BucketItem getBucket() {
        return bucket;
    }

    public FluidBlock getBlock() {
        return block;
    }

    public boolean is(Fluid fluid) {
        return fluid == still || fluid == flowing;
    }

    private abstract class Base extends FlowableFluid {
        @Override
        public Fluid getFlowing() {
            return flowing;
        }

        @Override
        public Fluid getSource() {
            return still;
        }

        @Override
        public Item getBucket() {
            return bucket != null ? bucket : Items.AIR;
        }

        @Override
        public BlockState createLegacyBlock(FluidState state) {
            if (block == null)
                return Blocks.AIR.defaultBlockState();
            return block.defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
        }

        @Override
        public boolean isSame(Fluid fluid) {
            return fluid == still || fluid == flowing;
        }

        @Override
        public int getDropOff(LevelReader level) {
            return dropOff;
        }

        @Override
        public int getTickDelay(LevelReader level) {
            return tickDelay;
        }

        @Override
        public int getSlopeFindDistance(LevelReader level) {
            return slopeFindDistance;
        }

        @Override
        protected boolean canConvertToSource(ServerLevel level) {
            return false;
        }

        @Override
        public Optional<SoundEvent> getPickupSound() {
            return pickupSound.isPresent() ? pickupSound : super.getPickupSound();
        }
    }

    private final class Flowing extends Base {
        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }

        @Override
        public boolean isSource(FluidState state) {
            return false;
        }
    }

    private final class Still extends Base {
        @Override
        public int getAmount(FluidState state) {
            return 8;
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }
    }
}
