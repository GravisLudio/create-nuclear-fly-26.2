package net.nuclearteam.createnuclear.content.multiblock.frame;

import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.server.level.ServerLevel;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.nuclearteam.createnuclear.CNBlockEntityTypes;
import net.nuclearteam.createnuclear.content.multiblock.MultiblockHelpers;
import net.nuclearteam.createnuclear.content.multiblock.pattern.ReactorPattern;
import net.nuclearteam.createnuclear.foundation.utility.CreateNuclearLang;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ReactorFrame extends Block implements IWrenchable, IBE<ReactorFrameEntity> {
    public static final Property<Part> PART = EnumProperty.create("part", Part.class);
    protected ReactorPattern pattern =  new ReactorPattern();
    public ReactorFrame(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PART);
        super.createBlockStateDefinition(builder);
    }

    public enum Part implements StringRepresentable {
        START,
        MIDDLE,
        END,
        NONE
        ;

        @Override
        public @NotNull String getSerializedName() {
            return CreateNuclearLang.asId(name());
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction.Axis axis = context.getClickedFace().getAxis();

        if (axis == Direction.Axis.X || axis == Direction.Axis.Z) axis = Direction.Axis.Y;

        return this.defaultBlockState().setValue(PART, getType(this.defaultBlockState(), getRelativeTop(level, pos, axis), getRelativeBottom(level, pos, axis)));
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston) {
        if (level.isClientSide()) return;

        Direction.Axis axis = Direction.Axis.Y;
        Part part = getType(state, getRelativeTop(level, pos, axis), getRelativeBottom(level, pos, axis));

        if (state.getValue(PART) == part) return;

        state = state.setValue(PART, part);
        level.setBlock(pos, state, 3);
    }

    public BlockState getRelativeTop(Level level, BlockPos pos, Direction.Axis axis) {
        return level.getBlockState(pos.relative(Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE)));
    }

    public  BlockState getRelativeBottom(Level level, BlockPos pos, Direction.Axis axis) {
        return level.getBlockState(pos.relative(Direction.fromAxisAndDirection(axis, Direction.AxisDirection.NEGATIVE)));
    }

    public Part getType(BlockState state, BlockState above, BlockState below) {
        boolean shapeAboveSame = above.is(state.getBlock());
        boolean shapeBelowSame = below.is(state.getBlock());

        if (shapeAboveSame && ! shapeBelowSame) return  Part.END;
        else if (!shapeAboveSame && shapeBelowSame) return  Part.START;
        else if (shapeAboveSame) return Part.MIDDLE;
        return Part.NONE;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation direction) {
        return super.rotate(state, direction);
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return super.mirror(state, mirror);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        pattern.findController(pos, level, true);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @org.jetbrains.annotations.Nullable LivingEntity pPlacer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, pPlacer, stack);
        MultiblockHelpers.handleAdvancedPlacedBy(pos, level, pPlacer);
    }

    @Override
    public void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        // playerDestroy is NOT called in creative mode, so the structure must also be
        // re-evaluated here. This hook only runs for an actual block removal/replacement, never
        // for a PART property change on the same block, which upstream had to filter out itself.
        pattern.findController(pos, level, false);
    }

    @Override
    public Class<ReactorFrameEntity> getBlockEntityClass() {
        return ReactorFrameEntity.class;
    }

    @Override
    public BlockEntityType<? extends ReactorFrameEntity> getBlockEntityType() {
        return CNBlockEntityTypes.REACTOR_FRAME.get();
    }
}
