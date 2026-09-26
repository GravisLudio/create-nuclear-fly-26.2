package net.nuclearteam.createnuclear.client;

import com.zurrtum.create.api.behaviour.display.DisplaySource;
import com.zurrtum.create.client.api.behaviour.display.DisplaySourceRender;
import com.zurrtum.create.client.content.redstone.displayLink.source.SingleLineDisplaySourceRender;
import com.zurrtum.create.client.foundation.gui.ModularGuiLineBuilder;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import com.zurrtum.create.content.redstone.displayLink.source.SingleLineDisplaySource;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.nuclearteam.createnuclear.CNDisplaySources;
import net.nuclearteam.createnuclear.content.redstone.displayLink.source.ConfigurableDisplaySource;

/**
 * Display Link screen widgets for the reactor's display sources. Create Fly attaches a client-side
 * {@link DisplaySourceRender} to each source ({@code AllDisplaySourceRenders}); these forward to the
 * widget code the sources kept from upstream. Single-line sources also get Create's label box, as
 * their superclass render provided upstream.
 */
@Environment(EnvType.CLIENT)
public final class CNDisplaySourceRenders {
    private CNDisplaySourceRenders() {
    }

    public static void register() {
        attach(CNDisplaySources.HEAT);
        attach(CNDisplaySources.LIQUID_LEVEL);
        attach(CNDisplaySources.FUEL);
        attach(CNDisplaySources.COOLER);
        attach(CNDisplaySources.REACTOR_SIZE);
        attach(CNDisplaySources.REACTOR_SUMMARY);
    }

    private static void attach(DisplaySource source) {
        source.attachRender = source instanceof SingleLineDisplaySource ? new SingleLine() : new Plain();
    }

    private static final class SingleLine extends SingleLineDisplaySourceRender {
        @Override
        public void initConfigurationWidgets(DisplaySource source, DisplayLinkContext context, ModularGuiLineBuilder builder, boolean isFirstLine) {
            super.initConfigurationWidgets(source, context, builder, isFirstLine);
            if (source instanceof ConfigurableDisplaySource configurable)
                configurable.initConfigurationWidgets(context, builder, isFirstLine);
        }
    }

    private static final class Plain implements DisplaySourceRender {
        @Override
        public void initConfigurationWidgets(DisplaySource source, DisplayLinkContext context, ModularGuiLineBuilder builder, boolean isFirstLine) {
            if (source instanceof ConfigurableDisplaySource configurable)
                configurable.initConfigurationWidgets(context, builder, isFirstLine);
        }
    }
}
