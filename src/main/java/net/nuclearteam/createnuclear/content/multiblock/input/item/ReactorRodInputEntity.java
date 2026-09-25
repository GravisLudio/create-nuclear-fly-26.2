package net.nuclearteam.createnuclear.content.multiblock.input.item;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.gui.menu.MenuBase;
import com.zurrtum.create.foundation.gui.menu.MenuProvider;
import net.minecraft.world.Containers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Holds the reactor's rods. Upstream registered the inventory as a NeoForge item handler
 * capability; the block now exposes it through {@code ItemInventoryProvider}. The menu goes through
 * Create Fly's {@link MenuProvider}, which writes this block entity into the open packet with
 * {@link #sendToMenu} as upstream did through {@code player.openMenu(be, be::sendToMenu)}.
 */
public class ReactorRodInputEntity extends SmartBlockEntity implements MenuProvider {
    protected BlockPos block;

    public ReactorRodInputInventory inventory;

    public ReactorRodInputEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        inventory = new ReactorRodInputInventory(this);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        if (!clientPacket) {
            inventory.read(view.childOrEmpty("Inventory"));
        }
        super.read(view, clientPacket);
    }

    @Override
    protected void write(ValueOutput view, boolean clientPacket) {
        if (!clientPacket) {
            inventory.write(view.child("Inventory"));
        }
        super.write(view, clientPacket);
    }

    /**
     * Drops the rods. Upstream did this in {@code ReactorRodInput.onRemove}, which 26.2 only calls
     * after the block entity has been removed.
     */
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState oldState) {
        super.preRemoveSideEffects(pos, oldState);
        if (level != null)
            Containers.dropContents(level, pos, inventory);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.createnuclear.reactor_input.title");
    }

    @Nullable
    @Override
    public MenuBase<?> createMenu(int id, Inventory inventory, Player player, RegistryFriendlyByteBuf extraData) {
        sendToMenu(extraData);
        return new ReactorRodInputMenu(id, inventory, this);
    }
}
