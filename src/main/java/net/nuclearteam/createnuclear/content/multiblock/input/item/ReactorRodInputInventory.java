package net.nuclearteam.createnuclear.content.multiblock.input.item;

import com.zurrtum.create.infrastructure.items.ItemStackHandler;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.nuclearteam.createnuclear.api.multiblock.rods.RodType.TypeRodPredicate;

/**
 * The rod input's one slot. Create Fly's {@link ItemStackHandler} is a vanilla {@code Container}:
 * NeoForge's {@code onContentsChanged(slot)} is {@link #setChanged()} and {@code isItemValid} is
 * {@link #canPlaceItem}.
 */
public class ReactorRodInputInventory extends ItemStackHandler {
    private final ReactorRodInputEntity be;

    public ReactorRodInputInventory(ReactorRodInputEntity be) {
        super(1);
        this.be = be;
    }

    @Override
    public void setChanged() {
        be.setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        Level level = be.getLevel();
        return slot == 0 && level != null && (TypeRodPredicate.isFuel(stack, level) || TypeRodPredicate.isCooled(stack, level));
    }
}
