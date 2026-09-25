package net.nuclearteam.createnuclear.content.multiblock.input.fluid;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.fluid.FluidTank;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.nuclearteam.createnuclear.content.fluids.FluidUnits;
import net.nuclearteam.createnuclear.content.multiblock.MultiblockHelpers;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerBlockEntity;

import java.util.List;

/**
 * A reactor's coolant input.
 * <p>
 * Upstream held a NeoForge {@code SmartFluidTank} and exposed a {@code FilteredFluidHandler}
 * capability wrapping it, which refused fluids other than the one the reactor is locked to and
 * took or released that lock as the tank filled and emptied. Create Fly has no capability system:
 * the block exposes the inventory ({@code ReactorFluidInput implements FluidInventoryProvider}),
 * so the filtering moved into the tank itself -- {@link InputTank#isValid} is the fill check and
 * {@link InputTank#markDirty} sees every change.
 * <p>
 * <b>Units.</b> Create Fly counts fluids in droplets, 81 per millibucket. The capacities here are
 * upstream's millibucket values converted once, through {@link FluidUnits}; everything the reactor
 * logic reads goes back through the same conversion in {@code ReactorInputFluidManager}.
 */
public class ReactorFluidInputEntity extends SmartBlockEntity implements IHaveGoggleInformation {

    /** Capacité par défaut tant que l'input n'est rattaché à aucun réacteur assemblé (mB). */
    public static final int DEFAULT_CAPACITY = 16000;

    private final InputTank internalTank;
    private LerpedFloat fluidLevel;

    public ReactorFluidInputEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        internalTank = new InputTank(FluidUnits.toDroplets(DEFAULT_CAPACITY));
    }

    /** What upstream exposed as the fluid handler capability. */
    public InputTank getTank() {
        return internalTank;
    }

    /**
     * Capacité du tank en fonction de la taille du réacteur (tier), in millibuckets.
     * 5x5 -> tier 1, 7x7 -> tier 2, 9x9 -> tier 3.
     */
    public static int getCapacityForReactorSize(int reactorSize) {
        return switch (reactorSize) {
            case 5 -> 144000;
            case 7 -> 448000;
            case 9 -> 848000;
            default -> DEFAULT_CAPACITY;
        };
    }

    /**
     * Applies an explicit tank capacity to this input, in millibuckets.
     * <p>
     * The per-reactor-size capacity ({@link #getCapacityForReactorSize(int)}) is the TOTAL the
     * reactor should hold, split across all fluid inputs by {@code ReactorAssembler}, so the sum
     * of every input's capacity stays equal to the configured value no matter how many inputs the
     * player places.
     */
    public void applyCapacity(int capacity) {
        int droplets = FluidUnits.toDroplets(capacity);
        if (internalTank.getMaxAmountPerStack() == droplets) return;
        internalTank.setCapacity(droplets);
        if (level != null && !level.isClientSide()) {
            setChanged();
            sendData();
        }
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
    }

    @Override
    protected void write(ValueOutput view, boolean clientPacket) {
        super.write(view, clientPacket);
        internalTank.write(view.child("tank"));
        view.putInt("capacity", internalTank.getMaxAmountPerStack());
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        super.read(view, clientPacket);
        view.getInt("capacity").ifPresent(internalTank::setCapacity);
        internalTank.read(view.childOrEmpty("tank"));

        if (view.getBooleanOr("ForceFluidLevel", false) || fluidLevel == null)
            fluidLevel = LerpedFloat.linear()
                    .startWithValue(getFillState());
    }

    public float getFillState() {
        return (float) internalTank.getFluid().getAmount() / internalTank.getMaxAmountPerStack();
    }

    protected void onTankContentsChanged() {
        // Avoid accessing level during deserialization when the block entity isn't attached yet
        if (this.level == null) {
            if (fluidLevel == null)
                fluidLevel = LerpedFloat.linear()
                        .startWithValue(getFillState());
            return;
        }

        if (!level.isClientSide()) {
            setChanged();
            sendData();
        }

        if (isVirtual()) {
            if (fluidLevel == null)
                fluidLevel = LerpedFloat.linear()
                        .startWithValue(getFillState());
            fluidLevel.chase(getFillState(), .5f, LerpedFloat.Chaser.EXP);
        }

        if (!level.isClientSide() && internalTank.isEmpty()) {
            ReactorControllerBlockEntity controller = MultiblockHelpers.getControllerForPart(level, worldPosition);
            if (controller != null) controller.clearLockIfAllInputsEmpty();
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        return containedFluidTooltip(tooltip, isPlayerSneaking, internalTank);
    }

    private BlockPos controllerPos() {
        ReactorControllerBlockEntity controller = MultiblockHelpers.getControllerForPart(level, worldPosition);
        return controller != null ? controller.getBlockPos() : null;
    }

    /** The tank with upstream's {@code FilteredFluidHandler} rules folded in. */
    public class InputTank extends FluidTank {
        InputTank(int capacity) {
            super(capacity);
        }

        /** Upstream's {@code fill} guard: only the fluid the reactor is locked to gets in. */
        @Override
        public boolean isValid(int slot, FluidStack stack) {
            if (stack.isEmpty() || level == null)
                return true;
            BlockPos controllerPos = controllerPos();
            if (controllerPos == null)
                return true;
            if (level instanceof ServerLevel serverLevel)
                return PersistentFluidLocks.get(serverLevel).canAccept(controllerPos, stack.getFluid());
            return FluidLockManager.canAccept(controllerPos, stack);
        }

        /**
         * Every change lands here. Upstream took the lock after a successful fill and released
         * it once a drain left the tank empty; both are a function of the contents afterwards.
         */
        @Override
        public void markDirty() {
            if (level != null) {
                BlockPos controllerPos = controllerPos();
                if (controllerPos != null) {
                    if (!fluid.isEmpty()) {
                        if (level instanceof ServerLevel serverLevel)
                            PersistentFluidLocks.get(serverLevel).tryLock(controllerPos, fluid.getFluid());
                        else
                            FluidLockManager.tryLock(controllerPos, fluid.getFluid());
                    } else {
                        if (level instanceof ServerLevel serverLevel)
                            PersistentFluidLocks.get(serverLevel).clearLock(controllerPos);
                        else
                            FluidLockManager.clearLock(controllerPos);
                    }
                }
            }
            onTankContentsChanged();
        }
    }
}
