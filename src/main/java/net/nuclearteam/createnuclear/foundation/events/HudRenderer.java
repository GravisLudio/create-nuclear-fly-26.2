package net.nuclearteam.createnuclear.foundation.events;

import net.nuclearteam.createnuclear.foundation.events.overlay.HelmetOverlay;
import net.nuclearteam.createnuclear.foundation.events.overlay.HudOverlay;
import net.nuclearteam.createnuclear.foundation.events.overlay.RadiationOverlay;

import java.util.Comparator;
import java.util.List;

public class HudRenderer {
    private static final List<HudOverlay> overlays = List.of(
            new HelmetOverlay(),
            new RadiationOverlay()
    );

    public void register() {
        overlays.stream()
                .sorted(Comparator.comparingInt(HudOverlay::getPriority))
                .forEach(HudOverlay::register);
    }
}
