package net.nuclearteam.createnuclear.client;

import com.zurrtum.create.client.AllMenuScreens;
import net.nuclearteam.createnuclear.CNMenus;
import net.nuclearteam.createnuclear.content.multiblock.bluePrintItem.ReactorBluePrintItemScreen;
import net.nuclearteam.createnuclear.content.multiblock.input.item.ReactorRodInputScreen;

/** The screen half of Registrate's {@code menu(name, factory, screenFactory)}. */
public final class CNMenuScreens {
    private CNMenuScreens() {
    }

    public static void register() {
        AllMenuScreens.register(CNMenus.REACTOR_BLUEPRINT_MENU, ReactorBluePrintItemScreen::create);
        AllMenuScreens.register(CNMenus.SLOT_ITEM_STORAGE, ReactorRodInputScreen::create);
    }
}
