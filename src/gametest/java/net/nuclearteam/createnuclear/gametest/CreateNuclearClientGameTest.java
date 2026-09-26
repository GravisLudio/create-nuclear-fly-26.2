package net.nuclearteam.createnuclear.gametest;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import java.util.List;

/**
 * Smoke test in a real client: builds a small scene with the mod's content and takes a screenshot
 * of each part (build/run/clientGameTest/screenshots). It fails on crashes and missing registry
 * entries; what it shows (textures, models, animation poses) has to be looked at.
 */
public class CreateNuclearClientGameTest implements FabricClientGameTest {
    private static final String NS = "createnuclear";

    private static final List<String> BLOCKS = List.of(
        "reactor_casing", "reactor_frame", "reactor_controller", "reactor_core", "reactor_cooler",
        "reactor_rod_input", "reactor_fluid_input", "reactor_output", "reactor_alarm",
        "uranium_ore", "thorium_ore", "lead_ore", "nitrate_ore", "autunite", "steel_block",
        "lead_block", "reinforced_glass", "enriched_soul_soil", "enriching_campfire");

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
            singleplayer.getClientLevel().waitForChunksRender();
            TestServerContext server = singleplayer.getServer();

            server.runCommand("time set noon");
            server.runCommand("weather clear");
            server.runCommand("gamemode creative @p");
            server.runCommand("fill -8 99 -8 12 99 16 minecraft:smooth_stone");
            server.runCommand("fill -8 100 -8 12 110 16 minecraft:air");

            // 1. The four irradiated mobs, adults and a baby of each, frozen in front of the player.
            server.runCommand("tp @p 2 100 -4 0 15");
            String[] mobs = {"irradiated_cow", "irradiated_chicken", "irradiated_wolf", "irradiated_cat"};
            for (int i = 0; i < mobs.length; i++) {
                int x = -2 + i * 2;
                server.runCommand("summon " + NS + ":" + mobs[i] + " " + x + " 100 3 {NoAI:1b,Rotation:[180f,0f]}");
                server.runCommand("summon " + NS + ":" + mobs[i] + " " + x + " 100 6 {NoAI:1b,Age:-24000,Rotation:[180f,0f]}");
            }
            context.waitTicks(20);
            context.takeScreenshot("createnuclear-mobs");

            // 2. A row of the mod's blocks.
            for (int i = 0; i < BLOCKS.size(); i++) {
                server.runCommand("setblock " + (-8 + i) + " 100 12 " + NS + ":" + BLOCKS.get(i));
            }
            server.runCommand("kill @e[type=!minecraft:player]");
            server.runCommand("tp @p 1 101 4 0 15");
            context.waitTicks(20);
            context.takeScreenshot("createnuclear-blocks");

            // 3. The suit on the player, seen from the front (dyed chestplate to check the cloth texture).
            server.runCommand("item replace entity @p armor.head with " + NS + ":default_anti_radiation_helmet");
            server.runCommand("item replace entity @p armor.chest with " + NS + ":default_anti_radiation_chestplate[" + NS + ":cloth_color=\"red\"]");
            server.runCommand("item replace entity @p armor.legs with " + NS + ":default_anti_radiation_leggings");
            server.runCommand("item replace entity @p armor.feet with " + NS + ":default_anti_radiation_boots");
            context.runOnClient(mc -> mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT));
            context.waitTicks(20);
            context.takeScreenshot("createnuclear-suit");
            context.runOnClient(mc -> mc.options.setCameraType(CameraType.FIRST_PERSON));

            // 4. Item icons: the first 36 of the mod's items in the inventory.
            server.runCommand("clear @p");
            // Dyed suit pieces first: they select a different model on the cloth_color component.
            server.runCommand("give @p " + NS + ":default_anti_radiation_helmet[" + NS + ":cloth_color=\"blue\"]");
            server.runCommand("give @p " + NS + ":default_anti_radiation_chestplate[" + NS + ":cloth_color=\"lime\"]");
            List<Identifier> items = context.computeOnClient(mc -> BuiltInRegistries.ITEM.keySet().stream()
                .filter(id -> id.getNamespace().equals(NS)).sorted().limit(34).toList());
            for (Identifier id : items) {
                server.runCommand("give @p " + id);
            }
            server.runCommand("gamemode survival @p");
            context.waitTicks(5);
            context.setScreen(() -> new InventoryScreen(Minecraft.getInstance().player)); // supplier runs on the client thread
            context.waitTicks(10);
            context.takeScreenshot("createnuclear-items");
            context.setScreen(() -> null);
        }
    }
}
