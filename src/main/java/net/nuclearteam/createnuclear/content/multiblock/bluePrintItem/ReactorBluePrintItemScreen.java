package net.nuclearteam.createnuclear.content.multiblock.bluePrintItem;

import com.zurrtum.create.client.foundation.gui.menu.AbstractSimiContainerScreen;
import com.zurrtum.create.foundation.gui.menu.MenuType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.nuclearteam.createnuclear.foundation.gui.CNGuiTextures;

import static com.zurrtum.create.client.foundation.gui.AllGuiTextures.PLAYER_INVENTORY;

@SuppressWarnings({"unused"})
public class ReactorBluePrintItemScreen extends AbstractSimiContainerScreen<ReactorBluePrintMenu> {
    protected static final CNGuiTextures BG = CNGuiTextures.CONFIGURED_PATTERN_GUI;

    public ReactorBluePrintItemScreen(ReactorBluePrintMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, BG.width, BG.height + PLAYER_INVENTORY.getHeight());
    }

    /** Screen factory for Create Fly's menu system; the open packet carries the held blueprint. */
    public static ReactorBluePrintItemScreen create(Minecraft mc, MenuType<ItemStack> type, int syncId,
                                                   Inventory inventory, Component title, RegistryFriendlyByteBuf extraData) {
        return type.create(ReactorBluePrintItemScreen::new, syncId, inventory, title, getStack(extraData));
    }

    @Override
    protected void init() {
        setWindowOffset(0, 0);
        super.init();
        clearWidgets();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
        int x = leftPos;
        int y = topPos + 38;

        BG.render(guiGraphics, x + 23, y - 19);
        renderPlayerInventory(guiGraphics, x + 23, y + 175);

        // Colours are full ARGB in 26.2; upstream's 0x592424 had no alpha byte and would draw nothing.
        guiGraphics.text(font, title, x + 26, y - 12, 0xFF592424, false); //ici pour le titre
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (!ItemStack.matches(menu.player.getMainHandItem(), menu.contentHolder)) {
            minecraft.player.closeContainer();
        }
    }
}
