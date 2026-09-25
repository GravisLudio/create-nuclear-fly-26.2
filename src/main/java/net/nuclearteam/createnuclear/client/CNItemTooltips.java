package net.nuclearteam.createnuclear.client;

import com.zurrtum.create.client.catnip.lang.FontHelper;
import com.zurrtum.create.client.foundation.item.ItemDescription;
import com.zurrtum.create.client.foundation.item.KineticStats;
import com.zurrtum.create.client.foundation.item.TooltipModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.nuclearteam.createnuclear.CreateNuclear;
import net.nuclearteam.createnuclear.foundation.item.RodsStats;
import net.nuclearteam.createnuclear.foundation.registrate.BlockEntry;
import net.nuclearteam.createnuclear.foundation.registrate.ItemEntry;

/**
 * Upstream's {@code REGISTRATE.setTooltipModifierFactory}: every item the mod registers gets
 * Create's item description, kinetic stats and the rod stats. Create Fly keeps the modifiers in a
 * client-side registry keyed by item ({@code AllItemTooltips} does the same for Create's own).
 */
public final class CNItemTooltips {
    private CNItemTooltips() {
    }

    public static void register() {
        for (ItemEntry<?> entry : CreateNuclear.REGISTRATE.getAllItems())
            register(entry.asItem());
        for (BlockEntry<?> entry : CreateNuclear.REGISTRATE.getAllBlocks())
            register(entry.asItem());
    }

    private static void register(Item item) {
        if (item == Items.AIR)
            return;
        TooltipModifier.REGISTRY.register(item,
            new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
                .andThen(TooltipModifier.mapNull(KineticStats.create(item)))
                .andThen(RodsStats.create(item)));
    }
}
