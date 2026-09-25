package net.nuclearteam.createnuclear;

import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.foundation.gui.menu.MenuType;
import net.minecraft.core.Registry;
import net.minecraft.world.item.ItemStack;
import net.nuclearteam.createnuclear.content.multiblock.bluePrintItem.ReactorBluePrintMenu;
import net.nuclearteam.createnuclear.content.multiblock.input.item.ReactorRodInputEntity;
import net.nuclearteam.createnuclear.content.multiblock.input.item.ReactorRodInputMenu;

/**
 * Menus go through Create Fly's own menu system rather than vanilla's: a {@link MenuType} keyed by
 * its holder type, registered in {@link CreateRegistries#MENU_TYPE}, opened with
 * {@code MenuProvider.openHandledScreen}. The holder is written into the open packet's extra data
 * on the server and read back by the screen factory, registered client-side in
 * {@code client.CNMenuScreens}, instead of Registrate's {@code menu(name, factory, screenFactory)}.
 */
public class CNMenus {
    public static final MenuType<ItemStack> REACTOR_BLUEPRINT_MENU = register("reactor_blueprint_menu", ReactorBluePrintMenu::new);
    public static final MenuType<ReactorRodInputEntity> SLOT_ITEM_STORAGE = register("slot_item_menu", ReactorRodInputMenu::new);

    private static <T> MenuType<T> register(String name, MenuType<T> type) {
        return Registry.register(CreateRegistries.MENU_TYPE, CreateNuclear.asResource(name), type);
    }

    public static void register() {}
}
