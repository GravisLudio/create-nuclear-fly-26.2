package net.nuclearteam.createnuclear.content.enriching.fire;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ScheduledTickAccess;
import com.mojang.serialization.MapCodec;
import java.util.function.UnaryOperator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.nuclearteam.createnuclear.CNTags.CNBlockTags;
import net.nuclearteam.createnuclear.CreateNuclear;
@SuppressWarnings({"deprecation"})
public class EnrichingFireBlock extends BaseFireBlock {
    public static final MapCodec<EnrichingFireBlock> CODEC = simpleCodec(EnrichingFireBlock::new);


    public EnrichingFireBlock(Properties properties, float fireDamage) {
        super(properties, fireDamage);
    }

    public EnrichingFireBlock(Properties properties) {
        super(properties, 1f);
    }

    @Override
    protected MapCodec<? extends BaseFireBlock> codec() {
        return CODEC;
    }

    public BlockState getStateForPlacement() {
        return this.defaultBlockState();
    }

    @Override
    public BlockState updateShape(BlockState pState, LevelReader pLevel, ScheduledTickAccess ticks, BlockPos pCurrentPos, Direction pFacing, BlockPos pFacingPos, BlockState pFacingState, RandomSource random) {
        return this.canSurvive(pState, pLevel, pCurrentPos)
                ? this.getStateForPlacement()
                : Blocks.AIR.defaultBlockState();
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        return EnrichingFireBlock.canSurviveOnBlock(worldIn.getBlockState(pos.below()));
    }

    @Override
    protected boolean canBurn(BlockState p_49284_) {
        return true;
    }

    public static boolean canSurviveOnBlock(BlockState pState) {
        return pState.is(CNBlockTags.ENRICHING_FIRE_BASE_BLOCKS.tag);
    }

    public static UnaryOperator<Properties> getLight() {
        return p -> p.lightLevel(a -> 15);
    }
}
