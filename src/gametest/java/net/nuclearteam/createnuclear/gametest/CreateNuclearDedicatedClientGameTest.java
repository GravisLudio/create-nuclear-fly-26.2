package net.nuclearteam.createnuclear.gametest;

import lib.multiblock.impl.IMultiBlockPattern;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerConnection;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.core.registries.Registries;
import net.minecraft.locale.Language;
import net.nuclearteam.createnuclear.CNAttachmentTypes;
import net.nuclearteam.createnuclear.CNDamageTypes;
import net.nuclearteam.createnuclear.CNBlocks;
import net.nuclearteam.createnuclear.CNDataComponents;
import net.nuclearteam.createnuclear.CNEffects;
import net.nuclearteam.createnuclear.CNItems;
import net.nuclearteam.createnuclear.content.multiblock.ReactorAssembler;
import net.nuclearteam.createnuclear.content.multiblock.bluePrintItem.PatternData;
import net.nuclearteam.createnuclear.content.multiblock.bluePrintItem.ReactorBluePrintData;
import net.nuclearteam.createnuclear.content.multiblock.bluePrintItem.ReactorBluePrintItem;
import net.nuclearteam.createnuclear.content.multiblock.bluePrintItem.ReactorBluePrintItemScreen;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerBlock;
import net.nuclearteam.createnuclear.content.multiblock.controller.ReactorControllerBlockEntity;
import net.nuclearteam.createnuclear.content.multiblock.input.item.ReactorRodInputEntity;
import net.nuclearteam.createnuclear.content.multiblock.input.item.ReactorRodInputScreen;
import net.nuclearteam.createnuclear.content.radiation.capability.RadiationCapability;

import java.util.List;

/**
 * The same content as {@link CreateNuclearClientGameTest}, but with a real client connected to a
 * <em>dedicated</em> server: nothing the client sees can come from the integrated server's shared
 * memory, so this is what proves the network side (block entity sync, menu packets, the synced
 * radiation attachment, effects, entities). Every assertion reads the CLIENT's copy.
 */
public class CreateNuclearDedicatedClientGameTest implements FabricClientGameTest {
    private static final String NS = "createnuclear";

    @Override
    public void runTest(ClientGameTestContext context) {
        // A free port picked by the OS: the default 25565 is often taken by someone else's dev
        // server on this machine, which makes the test server fail to bind.
        java.util.Properties properties = new java.util.Properties();
        try (java.net.ServerSocket probe = new java.net.ServerSocket(0)) {
            properties.setProperty("server-port", Integer.toString(probe.getLocalPort()));
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Could not find a free port for the test server", e);
        }
        try (TestDedicatedServerContext server = context.worldBuilder().createServer(properties)) {
            try (TestServerConnection connection = server.connect()) {
                connection.getClientLevel().waitForChunksDownload();
                run(context, server);
            }
        }
    }

    private static void run(ClientGameTestContext context, TestDedicatedServerContext server) {
        server.runCommand("time set noon");
        server.runCommand("weather clear");
        server.runCommand("gamemode creative @p");
        server.runCommand("fill -8 99 -8 12 99 16 minecraft:smooth_stone");
        server.runCommand("fill -8 100 -8 12 110 16 minecraft:air");
        server.runCommand("tp @p 2 100 -4 0 15");
        context.waitTicks(20);

        // 1. The irradiated mobs must exist on the client (entity spawn packets + their renderers).
        for (String mob : new String[] {"irradiated_cow", "irradiated_chicken", "irradiated_wolf", "irradiated_cat"}) {
            server.runCommand("summon " + NS + ":" + mob + " " + (mob.length() % 5) + " 100 3 {NoAI:1b,Rotation:[180f,0f]}");
        }
        context.waitTicks(30);
        long mobsOnClient = context.computeOnClient(mc -> {
            long n = 0;
            for (var e : mc.level.entitiesForRendering()) {
                if (e.getType().builtInRegistryHolder().key().identifier().getNamespace().equals(NS)) n++;
            }
            return n;
        });
        context.takeScreenshot("dedicated-mobs");
        server.runCommand("kill @e[type=!minecraft:player]");
        System.out.println("[createnuclear-dedicated] irradiated mobs seen by the client: " + mobsOnClient);
        if (mobsOnClient != 4) {
            throw new AssertionError("The client sees " + mobsOnClient + " irradiated mobs, expected 4");
        }

        // 2. A 5x5 reactor, fuelled and running on the dedicated server.
        BlockPos controller = new BlockPos(2, 103, 6);
        IMultiBlockPattern reactor = ReactorPatterns.build(ReactorPatterns.REACTOR_5);
        server.runOnServer(s -> reactor.construct(s.overworld(), controller, (c, st) -> true));
        context.waitTicks(10);
        List<BlockPos> inputs = server.computeOnServer(s -> Direction.Plane.HORIZONTAL.stream()
            .map(controller::relative)
            .filter(pos -> s.overworld().getBlockState(pos).is(CNBlocks.REACTOR_CASING.get()))
            .toList());
        if (inputs.size() != 2) {
            throw new AssertionError("Expected two casings beside the controller, found " + inputs);
        }
        BlockPos rodInput = inputs.get(0);
        BlockPos fluidInput = inputs.get(1);
        server.runOnServer(s -> {
            s.overworld().setBlockAndUpdate(rodInput, CNBlocks.REACTOR_ROD_INPUT.getDefaultState());
            s.overworld().setBlockAndUpdate(fluidInput, CNBlocks.REACTOR_FLUID_INPUT.getDefaultState());
        });
        context.waitTicks(2);
        boolean assembled = server.computeOnServer(s -> {
            ReactorAssembler.assemble(controller, s.overworld());
            return s.overworld().getBlockEntity(controller) instanceof ReactorControllerBlockEntity be && be.isAssembled();
        });
        long[] inserted = server.computeOnServer(s -> {
            ServerLevel level = s.overworld();
            long rods, water;
            try (Transaction tx = Transaction.openOuter()) {
                Storage<ItemVariant> rodStorage = ItemStorage.SIDED.find(level, rodInput, null);
                Storage<FluidVariant> fluids = FluidStorage.SIDED.find(level, fluidInput, null);
                rods = rodStorage == null ? -1 : rodStorage.insert(ItemVariant.of(CNItems.URANIUM_ROD.get()), 64, tx);
                water = fluids == null ? -1 : fluids.insert(FluidVariant.of(Fluids.WATER), FluidConstants.BUCKET * 8, tx);
                tx.commit();
            }
            PatternData[] pattern = new PatternData[57];
            for (int i = 0; i < pattern.length; i++) {
                pattern[i] = new PatternData(i, new ItemStack(Items.GLASS_PANE));
            }
            pattern[28] = new PatternData(28, new ItemStack(CNItems.URANIUM_ROD.get()));
            ItemStack blueprint = new ItemStack(CNItems.REACTOR_BLUEPRINT.get());
            blueprint.set(CNDataComponents.REACTOR_BLUE_PRINT_DATA, new ReactorBluePrintData(0, 1, pattern));
            ReactorControllerBlockEntity be = (ReactorControllerBlockEntity) level.getBlockEntity(controller);
            be.getInventoryObject().setItem(0, blueprint);
            be.setConfiguredPattern(blueprint);
            return new long[] {rods, water};
        });
        server.runCommand("tp @p 2 101 0 facing 2 103 6");
        context.waitTicks(100);
        if (!assembled || inserted[0] <= 0 || inserted[1] <= 0) {
            throw new AssertionError("Dedicated server: assembled=" + assembled + " rods=" + inserted[0] + " water=" + inserted[1]);
        }

        // 2b. What the CLIENT knows about that reactor: its block state and its block entity.
        String clientView = context.computeOnClient(mc -> {
            boolean active = mc.level.getBlockState(controller).getValue(ReactorControllerBlock.ACTIVE);
            if (!(mc.level.getBlockEntity(controller) instanceof ReactorControllerBlockEntity be)) {
                return "active=" + active + " blockEntity=MISSING";
            }
            return "active=" + active + " assembled=" + be.isAssembled() + " size=" + be.getMultiblockSize();
        });
        context.takeScreenshot("dedicated-reactor");
        System.out.println("[createnuclear-dedicated] reactor as the client sees it: " + clientView);
        if (!clientView.startsWith("active=true") || !clientView.contains("assembled=true")) {
            throw new AssertionError("The client does not see the running reactor: " + clientView);
        }

        // 3. Menus: the server opens each menu for the player, the client must end up on the right screen.
        server.runOnServer(s -> {
            ServerPlayer player = s.getPlayerList().getPlayers().getFirst();
            ((ReactorRodInputEntity) s.overworld().getBlockEntity(rodInput)).openHandledScreen(player);
        });
        context.waitTicks(10);
        String rodScreen = context.computeOnClient(mc -> mc.gui.screen() == null ? "none" : mc.gui.screen().getClass().getSimpleName());
        context.takeScreenshot("dedicated-menu-rod-input");
        context.runOnClient(mc -> mc.player.closeContainer());
        context.waitTicks(5);
        System.out.println("[createnuclear-dedicated] rod input menu -> client screen: " + rodScreen);
        if (!rodScreen.equals(ReactorRodInputScreen.class.getSimpleName())) {
            throw new AssertionError("Rod input menu did not open on the client: " + rodScreen);
        }

        server.runCommand("give @p " + NS + ":reactor_blueprint_item");
        context.waitTicks(5);
        boolean holdsBlueprint = server.computeOnServer(s -> s.getPlayerList().getPlayers().getFirst()
            .getMainHandItem().is(CNItems.REACTOR_BLUEPRINT.get()));
        if (!holdsBlueprint) {
            throw new AssertionError("The blueprint is not in the player's main hand after /give");
        }
        server.runOnServer(s -> {
            ServerPlayer player = s.getPlayerList().getPlayers().getFirst();
            ((ReactorBluePrintItem) CNItems.REACTOR_BLUEPRINT.get()).use(s.overworld(), player, InteractionHand.MAIN_HAND);
        });
        context.waitTicks(10);
        String blueprintScreen = context.computeOnClient(mc -> mc.gui.screen() == null ? "none" : mc.gui.screen().getClass().getSimpleName());
        context.takeScreenshot("dedicated-menu-blueprint");
        context.runOnClient(mc -> mc.player.closeContainer());
        context.waitTicks(5);
        System.out.println("[createnuclear-dedicated] blueprint menu -> client screen: " + blueprintScreen);
        if (!blueprintScreen.equals(ReactorBluePrintItemScreen.class.getSimpleName())) {
            throw new AssertionError("Blueprint menu did not open on the client: " + blueprintScreen);
        }

        // 4. Radiation: the synced attachment and the effect must reach the player's client; the full suit stops it.
        server.runCommand("clear @p");
        server.runCommand("effect clear @p");
        server.runCommand("tp @p -6 100 -6");
        server.runCommand("gamemode survival @p");
        server.runCommand("give @p " + NS + ":uranium_rod 16");
        context.waitTicks(60);
        String exposed = context.computeOnClient(mc -> {
            RadiationCapability cap = mc.player.getAttached(CNAttachmentTypes.RADIATION);
            return "effect=" + mc.player.hasEffect(CNEffects.RADIATION) + " radiation=" + (cap == null ? "NO_ATTACHMENT" : cap.getRadiation());
        });
        context.takeScreenshot("dedicated-radiation");
        for (String piece : new String[] {"head:helmet", "chest:chestplate", "legs:leggings", "feet:boots"}) {
            String[] p = piece.split(":");
            server.runCommand("item replace entity @p armor." + p[0] + " with " + NS + ":default_anti_radiation_" + p[1]);
        }
        context.waitTicks(5);
        server.runCommand("effect clear @p");
        context.waitTicks(60);
        String suited = context.computeOnClient(mc -> "effect=" + mc.player.hasEffect(CNEffects.RADIATION));
        System.out.println("[createnuclear-dedicated] radiation on the client, no suit: " + exposed + " | full suit: " + suited);
        if (!exposed.startsWith("effect=true") || exposed.contains("NO_ATTACHMENT")) {
            throw new AssertionError("Radiation did not reach the client: " + exposed);
        }
        if (!suited.equals("effect=false")) {
            throw new AssertionError("The suit does not stop radiation on the client: " + suited);
        }

        // 4b. Death messages: the damage types' message ids must resolve to translated text, or the
        //     death screen shows a raw key such as "death.attack.radiation".
        for (var key : List.of(CNDamageTypes.RADIATION, CNDamageTypes.FAN_RADIATION)) {
            String msgId = server.computeOnServer(s -> s.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE)
                .getOrThrow(key).value().msgId());
            boolean translated = context.computeOnClient(mc -> Language.getInstance().has("death.attack." + msgId));
            System.out.println("[createnuclear-dedicated] death message death.attack." + msgId + " translated=" + translated);
            if (!translated) {
                throw new AssertionError("No translation for death.attack." + msgId);
            }
        }

        // The radiation phase can kill the player: respawn before looking at the explosion, since a
        // dead player is not sent entities.
        if (context.computeOnClient(mc -> mc.player.isDeadOrDying())) {
            System.out.println("[createnuclear-dedicated] the player died of radiation; respawning");
            context.runOnClient(mc -> mc.player.respawn());
            context.waitTicks(20);
        }

        // 5. The nuclear explosion entity must reach the client (flash, shake and cloud are client side).
        server.runCommand("clear @p");
        server.runCommand("effect clear @p");
        server.runCommand("gamemode spectator @p");
        server.runCommand("fill 40 90 40 60 99 60 minecraft:stone");
        server.runCommand("tp @p 50 118 20 facing 50 100 50");
        context.waitTicks(20);
        server.runCommand("summon " + NS + ":nuclear_explosion 50 100 50");
        context.waitTicks(30);
        context.takeScreenshot("dedicated-explosion");
        long explosionsOnClient = context.computeOnClient(mc -> {
            long n = 0;
            for (var e : mc.level.entitiesForRendering()) {
                if (e.getType().builtInRegistryHolder().key().identifier().getPath().equals("nuclear_explosion")) n++;
            }
            return n;
        });
        System.out.println("[createnuclear-dedicated] nuclear_explosion entities on the client: " + explosionsOnClient);
        if (explosionsOnClient == 0) {
            throw new AssertionError("The nuclear explosion never reached the client");
        }
    }
}
