package net.nuclearteam.createnuclear.content.multiblock.controller.manager;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.nuclearteam.createnuclear.CreateNuclear;
import net.nuclearteam.createnuclear.content.fluids.FluidUnits;
import net.nuclearteam.createnuclear.content.multiblock.input.fluid.ReactorFluidInputEntity;
import net.nuclearteam.createnuclear.content.multiblock.input.fluid.VirtualReactorInputFluid;

import java.util.ArrayList;
import java.util.List;

public class ReactorInputFluidManager extends AbstractReactorIOManager implements ReactorInputFluidManagerI {
    private static final String NBT_KEY = "ReactorInputFluid";

    /**
     * Manager that tracks fluid input blocks for a reactor.
     * Responsible for serializing tracked positions, validating
     * handlers, reporting aggregated inventory and extracting fluids.
     */

    @Override
    /**
     * Read tracked positions from NBT data.
     * Existing positions are cleared before reading.
     */
    public void read(CompoundTag compound) {
        positions.clear();
        if (!compound.contains(NBT_KEY)) return;
        ListTag list = compound.getListOrEmpty(NBT_KEY);
        for (int i = 0; i < list.size(); ++i) {
            CompoundTag tag = list.getCompoundOrEmpty(i);
            positions.add(new BlockPos(tag.getIntOr("x", 0), tag.getIntOr("y", 0), tag.getIntOr("z", 0)));
        }
    }

    @Override
    /**
     * Write tracked positions to the provided NBT compound.
     */
    public void write(CompoundTag compound) {
        ListTag list = new ListTag();
        for (BlockPos pos : positions) {
            CompoundTag tag = new CompoundTag();
            tag.putInt("x", pos.getX());
            tag.putInt("y", pos.getY());
            tag.putInt("z", pos.getZ());
            list.add(tag);
        }
        compound.put(NBT_KEY, list);
    }

    @Override
    /**
     * Remove any tracked positions that are no longer valid in the given level.
     */
    public void clearInvalid(Level level) {
        List<BlockPos> toRemove = new ArrayList<>();
        for (BlockPos p: positions) {
            if (level == null || !level.isLoaded(p)) {
                toRemove.add(p);
                continue;
            }

            BlockEntity be = level.getBlockEntity(p);
            if (be == null) {
                toRemove.add(p);
                continue;
            }

            if (!(be instanceof ReactorFluidInputEntity)) toRemove.add(p);
        }

        positions.removeAll(toRemove);
    }

    @Override
    /**
     * Return an immutable list of tracked block positions that correspond
     * to fluid input entities in the given level.
     */
    public List<BlockPos> getBlocksPosition(Level level) {
        List<BlockPos> positions = new ArrayList<>();

        for (BlockPos p : this.getBlocksPosition()) {
            if (level.getBlockEntity(p) instanceof ReactorFluidInputEntity) positions.add(p);
        }
        return List.copyOf(positions);
    }

    @Override
    /**
     * Collect and return fluid handler capabilities for all tracked positions.
     */
    public List<ReactorFluidInputEntity.InputTank> getFuildHandlers(Level level) {
        List<ReactorFluidInputEntity.InputTank> handlers = new ArrayList<>();
        for (BlockPos p : new ArrayList<>(positions)) {
            if (level == null || !level.isLoaded(p)) continue;
            if (level.getBlockEntity(p) instanceof ReactorFluidInputEntity input) {
                handlers.add(input.getTank());
            }
        }

        return handlers;
    }

    @Override
    /**
     * Build and return a virtual aggregated inventory of all input fluids.
     */
    public VirtualReactorInputFluid getInventory(Level level) {
        VirtualReactorInputFluid virtualReactorInputFluid = new VirtualReactorInputFluid();
        List<ReactorFluidInputEntity.InputTank> handlers = this.getFuildHandlers(level);
        if (handlers.isEmpty()) return new VirtualReactorInputFluid();

        for (ReactorFluidInputEntity.InputTank h : handlers) {
            virtualReactorInputFluid.addFluid(h.getFluid());
        }

        return virtualReactorInputFluid;
    }

    /**
     * Extracts up to {@code fluidNeeded} units of fluid, spread across the tracked handlers.
     * <p>
     * The requested amount is a TOTAL across every input, not a per-input quota: each handler
     * only ever drains what is still missing, so a request of 10 units against two inputs
     * holding 10 each removes 10 in total, not 20.
     *
     * @return {@code true} if at least one unit was actually drained. A partial extraction still
     *         returns {@code true}: the reactor consumes whatever coolant it can reach rather
     *         than refusing to run.
     */
    @Override
    public boolean extractFluids(Level level, int fluidNeeded) {
        if (level == null || fluidNeeded <= 0) return false;
        List<ReactorFluidInputEntity.InputTank> handlers = getFuildHandlers(level);
        if (handlers.isEmpty()) return false;

        // The request is in millibuckets, as upstream counted; the tanks hold droplets.
        int needed = FluidUnits.toDroplets(fluidNeeded);
        int remaining = needed;

        for (ReactorFluidInputEntity.InputTank handler : handlers) {
            if (remaining <= 0) break;

            FluidStack stack = handler.getFluid();
            if (stack.isEmpty()) continue;

            int toExtract = Math.min(remaining, stack.getAmount());
            if (toExtract <= 0) continue;

            // Subtract what was actually drained, not what was asked for: a handler is free to
            // hand back less than requested.
            remaining -= handler.extract(stack, toExtract);
        }

        return remaining < needed;
    }
}
