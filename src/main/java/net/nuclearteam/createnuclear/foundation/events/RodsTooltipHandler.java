package net.nuclearteam.createnuclear.foundation.events;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.fabricmc.api.EnvType;
import net.nuclearteam.createnuclear.CreateNuclear;
import net.nuclearteam.createnuclear.foundation.item.RodsStats;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import java.util.List;

/** Was an {@code ItemTooltipEvent} subscriber; registered on Fabric's {@code ItemTooltipCallback} in {@code CreateNuclearClient}. */
public class RodsTooltipHandler {
    public static void onItemTooltip(ItemStack stack, List<Component> tooltip) {
        Item item = stack.getItem();
        Player player = Minecraft.getInstance().player;

        if (player == null) return;

        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        // Mod items already get their rod tooltip via Registrate's setTooltipModifierFactory
        // (CreateNuclear.REGISTRATE). This handler only serves EXTERNAL items (other mods or
        // datapack-defined RodTypes resolved at runtime via RodType.resolveRodType), which
        // setTooltipModifierFactory cannot cover. Do NOT remove/invert: doing so double-tooltips mod rods.
        if (id != null && CreateNuclear.MOD_ID.equals(id.getNamespace())) return;

        RodsStats rodsStats = RodsStats.create(item);
        rodsStats.modify(tooltip, player);
    }
}