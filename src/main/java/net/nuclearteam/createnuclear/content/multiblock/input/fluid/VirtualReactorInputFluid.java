package net.nuclearteam.createnuclear.content.multiblock.input.fluid;

import net.nuclearteam.createnuclear.content.fluids.FluidUnits;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.nuclearteam.createnuclear.content.logistics.BigFluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A lightweight, in-memory aggregation of fluid quantities keyed by
 * fluid Identifier. This record is used to represent the combined
 * input fluids available to a reactor without modifying the real handlers.
 */
public record VirtualReactorInputFluid(Map<Identifier, Long> fluids) {

    /**
     * Create an empty virtual inventory.
     */
    public VirtualReactorInputFluid() {
        this(new HashMap<>());
    }

    /**
     * Add the contents of the provided FluidStack to this virtual inventory.
     * Empty stacks or non-registered fluids are ignored.
     * <p>
     * The stack comes from a tank and is counted in droplets; this inventory keeps millibuckets,
     * which is what every reader of it (the heat calculation, displays, tooltips) was written for.
     * @param stack the fluid stack to add
     */
    public void addFluid(@NotNull FluidStack stack) {
        if (stack.isEmpty() || stack.getAmount() <= 0) return;
        Identifier id = BuiltInRegistries.FLUID.getKey(stack.getFluid());
        if (id == null) return;
        fluids.merge(id, FluidUnits.toMillibuckets((long) stack.getAmount()), Long::sum);
    }

    /**
     * Remove up to {@code amount} units of the specified fluid from this virtual inventory.
     * Returns a FluidStack describing the removed amount (or {@link FluidStack#EMPTY}).
     * @param fluidId fluid identifier
     * @param amount maximum amount to remove
     */
    public FluidStack removeFluid(@NotNull Identifier fluidId, long amount) {
        if (amount < 0 || fluidId == null) return FluidStack.EMPTY;
        long current = fluids.getOrDefault(fluidId, 0L);
        long removed = Math.min(current, amount);
        if (removed == 0) return FluidStack.EMPTY;
        long remaining = current - removed;
        if (remaining == 0) fluids.remove(fluidId);
        else fluids.put(fluidId, remaining);

        int removedInt = (int) Math.min(removed, Integer.MAX_VALUE);
        return new FluidStack(BuiltInRegistries.FLUID.getValue(fluidId), removedInt);
    }

    /**
     * Get the stored amount for the given fluid id.
     * @param fluidId fluid identifier
     * @return total amount stored for that fluid
     */
    public long getAmount(@NotNull Identifier fluidId) {
        if (fluidId == null) return 0L;
        return fluids.getOrDefault(fluidId, 0L);
    }

    /**
     * Convert a map of Identifier->long into a list of BigFluidStack
     * suitable for display or consumption elsewhere.
     */
    public static List<BigFluidStack> toBigList(Map<Identifier, Long> map) {
        List<BigFluidStack> list = new ArrayList<>();
        for (Entry<Identifier, Long> e : map.entrySet()) {
            Identifier id = e.getKey();
            long total = e.getValue();
            Fluid fluid = BuiltInRegistries.FLUID.getValue(id);
            if (fluid == null || total <= 0) continue;
            int amount = (int) Math.min(total, BigFluidStack.INF);
            list.add(new BigFluidStack(new FluidStack(fluid, amount), amount));
        }
        return list;
    }

    @Override
    public @NotNull String toString() {
        return "VirtualReactorInputFluid" + fluids.toString();
    }
}
