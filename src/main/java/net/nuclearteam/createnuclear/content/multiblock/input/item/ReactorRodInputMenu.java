package net.nuclearteam.createnuclear.content.multiblock.input.item;

import com.zurrtum.create.foundation.gui.menu.MenuBase;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.nuclearteam.createnuclear.CNMenus;

/**
 * Create Fly's {@code MenuBase} takes the holder directly; the client-side reading of the block
 * entity out of the open packet moved to {@link ReactorRodInputScreen#create}, the screen factory.
 */
public class ReactorRodInputMenu extends MenuBase<ReactorRodInputEntity> {

    public ReactorRodInputMenu(int id, Inventory inv, ReactorRodInputEntity contentHolder) {
        super(CNMenus.SLOT_ITEM_STORAGE, id, inv, contentHolder);
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            result = stackInSlot.copy();

            int playerInventorySize = player.getInventory().getNonEquipmentItems().size(); // normally 36
            int containerStart = playerInventorySize;
            int containerEnd = containerStart + 1; // 1 machine slot

            // Clicked a machine slot (after the player slots)
            if (index >= containerStart && index < containerEnd) {
                if (!this.moveItemStackTo(stackInSlot, 0, playerInventorySize, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Clicked a player slot — try moving the stack into the machine container
                if (!this.moveItemStackTo(stackInSlot, containerStart, containerEnd, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (stackInSlot.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (stackInSlot.getCount() == result.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, stackInSlot);
        }
        return result;
    }



    @Override
    protected void initAndReadInventory(ReactorRodInputEntity contentHolder) {

    }

    @Override
    protected void addSlots() {
        // player Slots
        for (int hotbarSlot = 0; hotbarSlot < 9; ++hotbarSlot) {
            this.addSlot(new Slot(player.getInventory(), hotbarSlot, -31 + hotbarSlot * 18, 155));
        }

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(player.getInventory(), col + row * 9 + 9, -31 + col * 18, 97 + row * 18));
            }
        }

        Slot slot1 = new Slot(contentHolder.inventory, 0, 42, 29) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return contentHolder.inventory.canPlaceItem(0, stack);
            }
        };

        addSlot(slot1);
    }

    @Override
    protected void saveData(ReactorRodInputEntity contentHolder) {
    }

    @Override
    public void clicked(int slotId, int button, ContainerInput clickType, Player player) {
        if (clickType == ContainerInput.THROW) {
            int[] targetSlotIds = {9, 18, 27, 0, 1, 28, 19, 10, 16, 17, 26, 25, 34, 35, 8, 7};
            for (int id : targetSlotIds) {
                if (slotId == id) {
                    clickType = ContainerInput.PICKUP;
                    super.clicked(slotId, button, clickType, player);
                }
            }
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }
}