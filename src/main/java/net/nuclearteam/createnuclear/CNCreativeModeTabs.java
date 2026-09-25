package net.nuclearteam.createnuclear;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.nuclearteam.createnuclear.foundation.registrate.BlockEntry;
import net.nuclearteam.createnuclear.foundation.registrate.ItemEntry;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * The mod's one creative tab.
 * <p>
 * Upstream used a copy of Create's {@code RegistrateDisplayItemsGenerator}: every entry Registrate
 * registered with {@code setCreativeTab(MAIN)} -- which was all of them -- flat items first, then
 * blocks, then items with 3D models. Its orderings, exclusions and stack factories were all empty.
 * The 3D split read baked item models, which 26.2 no longer exposes that way, so the tab lists
 * plain items, then the fluid buckets, then blocks, each in registration order.
 * <p>
 * {@code withTabsBefore(SPAWN_EGGS)} is gone: 26.2's builder takes a (row, column) placement, and
 * Create Fly passes {@code (null, -1)} for "wherever", as does this.
 */
public class CNCreativeModeTabs {
    public static final ResourceKey<CreativeModeTab> MAIN = ResourceKey.create(
        Registries.CREATIVE_MODE_TAB,
        CreateNuclear.asResource("main")
    );

    public static void register() {
        Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            MAIN,
            CreativeModeTab.builder(null, -1)
                .title(Component.translatable("itemGroup.createnuclear.main"))
                .icon(CNItems.URANIUM_POWDER::asStack)
                .displayItems((params, output) -> {
                    Set<Item> items = new LinkedHashSet<>();
                    for (ItemEntry<?> entry : CreateNuclear.REGISTRATE.getAllItems())
                        items.add(entry.asItem());
                    items.add(CNFluids.URANIUM.getBucket());
                    items.add(CNFluids.THORIUM.getBucket());
                    items.add(CNFluids.LIQUID_NITROGEN.getBucket());
                    for (BlockEntry<?> entry : CreateNuclear.REGISTRATE.getAllBlocks())
                        items.add(entry.asItem());
                    items.remove(Items.AIR);
                    items.forEach(output::accept);
                })
                .build()
        );
    }
}
