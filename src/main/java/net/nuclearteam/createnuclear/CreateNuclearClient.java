package net.nuclearteam.createnuclear;

import com.zurrtum.create.client.ponder.foundation.PonderIndex;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.nuclearteam.createnuclear.client.CNBlockEntityBehaviours;
import net.nuclearteam.createnuclear.client.CNBlockEntityRenders;
import net.nuclearteam.createnuclear.client.CNConnectedTextures;
import net.nuclearteam.createnuclear.client.CNItemModelProperties;
import net.nuclearteam.createnuclear.client.CNDisplaySourceRenders;
import net.nuclearteam.createnuclear.client.CNEntityRenderers;
import net.nuclearteam.createnuclear.client.CNFluidRenders;
import net.nuclearteam.createnuclear.client.CNItemTooltips;
import net.nuclearteam.createnuclear.client.CNMenuScreens;
import net.nuclearteam.createnuclear.client.CNParticles;
import net.nuclearteam.createnuclear.foundation.events.ClientEvents;
import net.nuclearteam.createnuclear.foundation.events.HudRenderer;
import net.nuclearteam.createnuclear.foundation.events.RodsTooltipHandler;
import net.nuclearteam.createnuclear.foundation.ponder.CreateNuclearPonderPlugin;

/**
 * Fabric client entrypoint (was {@code @Mod(dist = Dist.CLIENT)} plus a handful of
 * {@code @EventBusSubscriber(value = Dist.CLIENT)} classes, whose registrations are all made here).
 * <p>
 * Like the Connected port this is a single source set; Fabric only loads this class on a client
 * because it is declared under the {@code client} entrypoint.
 */
public class CreateNuclearClient implements ClientModInitializer {
    private static final HudRenderer HUD_RENDERER = new HudRenderer();

    @Override
    public void onInitializeClient() {
        // What Registrate chained onto registration upstream.
        CNEntityRenderers.register();
        CNBlockEntityRenders.register();
        CNBlockEntityBehaviours.register();
        CNConnectedTextures.register();
        CNFluidRenders.register();
        CNItemModelProperties.register();
        CNItemTooltips.register();
        CNDisplaySourceRenders.register();

        // Was RegisterParticleProvidersEvent / RegisterGuiLayersEvent.
        CNParticles.register();
        CNMenuScreens.register();
        HudElementRegistry.addFirst(CreateNuclear.asResource("nuke_flash"), CNClientProxy::renderNukeFlash);
        HUD_RENDERER.register();

        ClientTickEvents.END_CLIENT_TICK.register(ClientEvents::onClientTick);
        ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> RodsTooltipHandler.onItemTooltip(stack, lines));

        PonderIndex.addPlugin(new CreateNuclearPonderPlugin());
    }
}
