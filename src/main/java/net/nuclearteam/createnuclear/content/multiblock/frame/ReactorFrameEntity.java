package net.nuclearteam.createnuclear.content.multiblock.frame;

import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Block entity for {@link ReactorFrame}. It only stores a reference to the
 * reactor controller that owns this frame so the client-side renderer
 * ({@link ReactorFrameRenderer}) can fetch the reactor's current fluid and
 * draw it dynamically inside the frame window.
 *
 * <p>The controller position is assigned by
 * {@link net.nuclearteam.createnuclear.content.multiblock.ReactorAssembler}
 * during assembly and persisted/synced through {@link SmartBlockEntity}.</p>
 */
public class ReactorFrameEntity extends SmartBlockEntity {

    @Nullable
    private BlockPos controller;

    public ReactorFrameEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {

        super(type, pos, state);
        setController(pos);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) { }

    /** Assigns the owning reactor controller and syncs the change to clients. */
    public void setController(@Nullable BlockPos controllerPos) {
        if (java.util.Objects.equals(this.controller, controllerPos)) return;
        this.controller = controllerPos;
        notifyUpdate();
    }

    @Nullable
    public BlockPos getController() {
        return controller;
    }

    /**
     * Resolves the controller block entity, returning {@code null} when no
     * controller is assigned or the referenced block is no longer a controller.
     */
    @Nullable
    public ReactorControllerBlockEntity getControllerEntity() {
        if (controller == null || level == null) return null;
        if (level.getBlockEntity(controller) instanceof ReactorControllerBlockEntity controllerEntity)
            return controllerEntity;
        return null;
    }

    @Override
    protected void write(ValueOutput view, boolean clientPacket) {
        super.write(view, clientPacket);
        if (controller != null) {
            view.putLong("Controller", controller.asLong());
        }
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        super.read(view, clientPacket);
        this.controller = view.getLong("Controller").map(BlockPos::of).orElse(null);
    }
}
