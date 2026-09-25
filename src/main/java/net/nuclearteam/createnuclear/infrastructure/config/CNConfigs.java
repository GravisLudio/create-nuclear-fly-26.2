package net.nuclearteam.createnuclear.infrastructure.config;

import com.zurrtum.create.catnip.config.Builder;
import net.nuclearteam.createnuclear.CreateNuclear;

/**
 * Mod configuration.
 * <p>
 * NeoForge built a {@code ModConfigSpec} per side, registered it against the {@code ModContainer}
 * and forwarded {@code ModConfigEvent.Loading}/{@code Reloading} to the config objects. Create
 * Fly's catnip {@link Builder#create} does all of that in one call and owns the load lifecycle, so
 * the event handlers and the spec bookkeeping are gone rather than ported -- same as the Connected
 * port.
 */
public class CNConfigs {
    private static CNCClient client;
    private static CNCCommon common;
    public static CNCServer server;

    public static CNCClient client() {
        return client;
    }

    public static CNCCommon common() {
        return common;
    }

    public static CNCServer server() {
        return server;
    }

    public static void register() {
        client = Builder.create(CNCClient::new, CreateNuclear.MOD_ID, "client");
        common = Builder.create(CNCCommon::new, CreateNuclear.MOD_ID, "common");
        server = Builder.create(CNCServer::new, CreateNuclear.MOD_ID, "server");
    }
}
