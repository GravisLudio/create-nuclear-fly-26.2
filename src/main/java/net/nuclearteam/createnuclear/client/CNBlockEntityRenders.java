package net.nuclearteam.createnuclear.client;

import com.zurrtum.create.client.AllBlockEntityRenders;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.content.kinetics.base.OrientedRotatingVisual;
import net.nuclearteam.createnuclear.CNBlockEntityTypes;
import net.nuclearteam.createnuclear.content.multiblock.frame.ReactorFrameRenderer;
import net.nuclearteam.createnuclear.content.multiblock.output.ReactorOutputRenderer;

/**
 * Block entity renderers and Flywheel visuals, chained onto {@code CNBlockEntityTypes} upstream.
 * <p>
 * Upstream's {@code .visual(() -> OrientedRotatingVisual.of(SHAFT_HALF), false)} passed Registrate's
 * {@code renderNormally = false}, which is Create Fly's {@code visual(...)} (renderer skipped while
 * Flywheel is on). The frame only ever had a renderer.
 */
public final class CNBlockEntityRenders {
    private CNBlockEntityRenders() {
    }

    public static void register() {
        AllBlockEntityRenders.render(CNBlockEntityTypes.REACTOR_FRAME.get(), ReactorFrameRenderer::new);
        AllBlockEntityRenders.visual(CNBlockEntityTypes.REACTOR_OUTPUT.get(), ReactorOutputRenderer::new,
            OrientedRotatingVisual.of(AllPartialModels.SHAFT_HALF));
    }
}
