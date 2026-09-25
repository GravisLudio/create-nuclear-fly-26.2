package net.nuclearteam.createnuclear;

import com.zurrtum.create.api.behaviour.display.DisplaySource;
import com.zurrtum.create.api.registry.CreateRegistries;
import net.minecraft.core.Registry;
import net.nuclearteam.createnuclear.content.redstone.displayLink.source.*;

import java.util.function.Supplier;

/**
 * Registered straight into Create's display source registry, as Create Fly's own
 * {@code AllDisplaySources} does. The block bindings are made by {@code CNBlocks} through
 * {@code CNBehaviours.displaySource}, which is why the entries are plain instances now rather than
 * Registrate's deferred {@code RegistryEntry}.
 */
public class CNDisplaySources {
    public static final HeatDisplaySource HEAT = register("heat", HeatDisplaySource::new);
    public static final LiquidLevelDisplaySource LIQUID_LEVEL = register("liquid_level", LiquidLevelDisplaySource::new);
    public static final ReactorSummaryDisplaySource REACTOR_SUMMARY = register("reactor_summary", ReactorSummaryDisplaySource::new);
    public static final FuelDisplaySource FUEL = register("fuel", FuelDisplaySource::new);
    public static final CoolerDisplaySource COOLER = register("cooler", CoolerDisplaySource::new);
    public static final ReactorSizeDisplaySource REACTOR_SIZE = register("reactor_size", ReactorSizeDisplaySource::new);

    private static <T extends DisplaySource> T register(String name, Supplier<T> factory) {
        return Registry.register(CreateRegistries.DISPLAY_SOURCE, CreateNuclear.asResource(name), factory.get());
    }

    public static void register() {
    }
}
