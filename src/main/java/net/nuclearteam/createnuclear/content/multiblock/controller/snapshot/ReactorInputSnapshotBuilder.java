package net.nuclearteam.createnuclear.content.multiblock.controller.snapshot;

import net.nuclearteam.createnuclear.content.multiblock.input.fluid.ReactorFluidInputEntity;
import net.nuclearteam.createnuclear.content.fluids.FluidUnits;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.nuclearteam.createnuclear.content.logistics.BigFluidStack;
import net.nuclearteam.createnuclear.content.multiblock.controller.manager.ReactorInputFluidManagerI;
import net.nuclearteam.createnuclear.content.multiblock.controller.manager.ReactorInputManagerI;
import net.nuclearteam.createnuclear.content.multiblock.input.fluid.VirtualReactorInputFluid;
import net.nuclearteam.createnuclear.content.multiblock.input.item.VirtualReactorInputsItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReactorInputSnapshotBuilder {
    private ReactorInputSnapshotBuilder() {}

    public static ReactorInputSnapshot build(Level level, ReactorInputManagerI inputManager, ReactorInputFluidManagerI inputFluidManager) {
        // Populate display fields for client sync
        Map<Item, Integer> items = new HashMap<>();
        List<Container> itemHandlers = inputManager.getItemHandlers(level);
        for (Container h : itemHandlers) {
            for (int s = 0; s < h.getContainerSize(); s++) {
                ItemStack st = h.getItem(s);
                if (!st.isEmpty()) {
                    items.merge(st.getItem(), st.getCount(), Integer::sum);
                }
            }
        }

        long maxFluidCapacity = 0;
        // Millibuckets, like the virtual fluid inventory it is shown against; the tanks count droplets.
        for (ReactorFluidInputEntity.InputTank h : inputFluidManager.getFuildHandlers(level)) {
            maxFluidCapacity += FluidUnits.toMillibuckets((long) h.getMaxAmountPerStack());
        }

        VirtualReactorInputsItem virtualItems = inputManager.getInventory(level);
        VirtualReactorInputFluid virtualFluid = inputFluidManager.getInventory(level);
        List<BigFluidStack> fluids = VirtualReactorInputFluid.toBigList(virtualFluid.fluids());



        return new ReactorInputSnapshot(items, fluids, maxFluidCapacity);
    }
}
