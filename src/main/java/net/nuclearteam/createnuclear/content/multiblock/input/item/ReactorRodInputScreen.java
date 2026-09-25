package net.nuclearteam.createnuclear.content.multiblock.input.item;

import com.zurrtum.create.foundation.gui.menu.MenuType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.nuclearteam.createnuclear.CreateNuclear;
import org.jetbrains.annotations.Nullable;

import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.menu.AbstractSimiContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.nuclearteam.createnuclear.foundation.gui.CNGuiTextures;

import static com.zurrtum.create.client.foundation.gui.AllGuiTextures.PLAYER_INVENTORY;

public class ReactorRodInputScreen extends AbstractSimiContainerScreen<ReactorRodInputMenu> {

    protected static final CNGuiTextures background = CNGuiTextures.REACTOR_SLOT_INVENTOR;

    public ReactorRodInputScreen(ReactorRodInputMenu container, Inventory inv, Component title) {
        super(container, inv, title, background.width, background.height + 4 + PLAYER_INVENTORY.getHeight());
    }

    /**
     * Screen factory for Create Fly's menu system. Upstream's {@code ReactorRodInputMenu.createOnClient}
     * did this read: the packet carries the block position and its update tag.
     */
    @Nullable
    public static ReactorRodInputScreen create(Minecraft mc, MenuType<ReactorRodInputEntity> type, int syncId,
                                               Inventory inventory, Component title, RegistryFriendlyByteBuf extraData) {
        ReactorRodInputEntity entity = getBlockEntity(mc, extraData);
        if (entity == null)
            return null;
        try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(entity.problemPath(), CreateNuclear.LOGGER)) {
            ValueInput view = TagValueInput.create(logging, extraData.registryAccess(), extraData.readNbt());
            entity.readClient(view);
            return type.create(ReactorRodInputScreen::new, syncId, inventory, title, entity);
        }
    }

    @Override
    protected void init() {
        setWindowOffset(0, 0);
        super.init();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
        int invX = getLeftOfCentered(PLAYER_INVENTORY.getWidth());
        int invY = topPos + background.height + 4;
        renderPlayerInventory(guiGraphics, invX, invY);

        background.render(guiGraphics, leftPos, topPos);
    }
}
