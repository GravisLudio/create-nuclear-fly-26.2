package net.nuclearteam.createnuclear.content.redstone.displayLink.source;

import com.zurrtum.create.client.foundation.gui.ModularGuiLineBuilder;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;

/**
 * A display source with its own options in the Display Link screen. Upstream overrode Create's
 * {@code initConfigurationWidgets}; Create Fly moved that method to a client-side
 * {@code DisplaySourceRender}, so the sources keep their widget code behind this interface and
 * {@code client.CNDisplaySourceRenders} forwards to it.
 */
public interface ConfigurableDisplaySource {
    void initConfigurationWidgets(DisplayLinkContext context, ModularGuiLineBuilder builder, boolean isFirstLine);
}
