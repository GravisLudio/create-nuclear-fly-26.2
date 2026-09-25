package net.nuclearteam.createnuclear.content.multiblock.input.item;

import net.minecraft.world.InteractionResult;
import net.minecraft.server.level.ServerLevel;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.item.ItemHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;


import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.nuclearteam.createnuclear.CNBlockEntityTypes;
import net.nuclearteam.createnuclear.CNShapes;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerBlockEntity;
import net.nuclearteam.createnuclear.content.multiblock.MultiblockHelpers;
import net.nuclearteam.createnuclear.foundation.block.MultiDirectionalReactorBlock;
import org.jetbrains.annotations.NotNull;

import org.jetbrains.annotations.Nullable;




public class ReactorRodInput extends MultiDirectionalReactorBlock implements IWrenchable, IBE<ReactorRodInputEntity> {

    public ReactorRodInput(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        ItemStack itemInHand = player.getItemInHand(hand);

        if (itemInHand.getItem() instanceof BlockItem) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }


        if (level.isClientSide()) {return InteractionResult.SUCCESS;}

        withBlockEntityDo(level, pos, be -> player.openMenu(be, be::sendToMenu));
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        MultiblockHelpers.handleOnPlace(pos, level, ReactorControllerBlockEntity::addInput);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity pPlacer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, pPlacer, stack);
        MultiblockHelpers.handleAdvancedPlacedBy(pos, level, pPlacer);
    }


    @Override
    public void affectNeighborsAfterRemoval(BlockState pState, ServerLevel pLevel, BlockPos pPos, boolean pIsMoving) {
        // Was onRemove. This only runs for an actual removal/replacement, never for a FACING
        // update, and after the block entity is gone -- which is why dropping the rods moved to
        // ReactorRodInputEntity.preRemoveSideEffects.
        MultiblockHelpers.handleRemoval(pPos, pLevel, ReactorControllerBlockEntity::removeInput);
        super.affectNeighborsAfterRemoval(pState, pLevel, pPos, pIsMoving);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }
    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return CNShapes.REACTOR_INPUT.get(state.getValue(FACING));
    }

    @Override
    public Class<ReactorRodInputEntity> getBlockEntityClass() {
        return ReactorRodInputEntity.class;
    }

    @Override
    public BlockEntityType<? extends ReactorRodInputEntity> getBlockEntityType() {
        return CNBlockEntityTypes.REACTOR_INPUT.get();
    }
}
