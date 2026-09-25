package net.nuclearteam.createnuclear.content.multiblock.controller.manager;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.Container;
import net.nuclearteam.createnuclear.content.multiblock.input.item.VirtualReactorInputsItem;

import java.util.List;

/**
 * Interface exposing operations specific to reactor inputs.
 * Currently provides access to `Container` instances at resolved input positions.
 */
public interface ReactorInputManagerI extends ReactorIOManager {
    /**
     * Retrieves valid item handlers for the given `level`.
     * May return an empty list if no valid positions exist.
     */
    List<Container> getItemHandlers(Level level);

    /** Returns an immutable copy of tracked positions. */
    List<BlockPos> getBlocksPosition(Level level);

    VirtualReactorInputsItem getInventory(Level level);

    boolean extractItems(Level level, int fuelNeeded, int coolerNeeded);
    boolean extractItemByName(Level level, String itemName);
}
