package net.nuclearteam.createnuclear.content.multiblock.controller;

import com.zurrtum.create.infrastructure.items.ItemStackHandler;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.nuclearteam.createnuclear.CNItems;

/**
 * The controller's single blueprint slot.
 * <p>
 * Upstream extended Create's {@code SmartInventory(1, be, 1, false)}: one slot, a stack limit of
 * one, and a sync to the client on every change. Create Fly's inventories are vanilla
 * {@code Container}s, so those become {@link #getMaxStackSize}, {@link #setChanged} and
 * {@link #canPlaceItem} (which was {@code isItemValid}).
 */
public class ReactorControllerInventory extends ItemStackHandler {
    private final ReactorControllerBlockEntity be;

    public ReactorControllerInventory(ReactorControllerBlockEntity be) {
        super(1);
        this.be = be;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        be.setChanged();
        return super.removeItemNoUpdate(index);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public void setChanged() {
        be.setChanged();
        if (be.getLevel() != null && !be.getLevel().isClientSide())
            be.sendData();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack resource) {
        return slot == 0 && resource.is(CNItems.REACTOR_BLUEPRINT.get());
    }

    /** Stands in for NeoForge's {@code ItemStackHandler.serializeNBT}, which the persistence service used. */
    public CompoundTag serializeNBT(HolderLookup.Provider registries) {
        Tag tag = ItemStack.OPTIONAL_CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), getItem(0))
            .result().orElseGet(CompoundTag::new);
        CompoundTag out = new CompoundTag();
        out.put("Slot0", tag);
        return out;
    }

    public void deserializeNBT(HolderLookup.Provider registries, CompoundTag tag) {
        ItemStack stack = tag.get("Slot0") == null ? ItemStack.EMPTY
            : ItemStack.OPTIONAL_CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag.get("Slot0"))
                .result().orElse(ItemStack.EMPTY);
        stacks.set(0, stack);
    }
}
