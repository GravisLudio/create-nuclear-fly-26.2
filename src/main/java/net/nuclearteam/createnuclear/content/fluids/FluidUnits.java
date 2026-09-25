package net.nuclearteam.createnuclear.content.fluids;

/**
 * Fluid amount conversion between upstream's millibuckets and Fabric's droplets.
 * <p>
 * NeoForge counts 1000 per bucket, Fabric (and so Create Fly's tanks) 81000. Nothing fails when a
 * millibucket value reaches a droplet API unconverted -- it silently holds or moves 81 times less --
 * so every crossing between the reactor's own bookkeeping (millibuckets, as upstream wrote it) and
 * a tank goes through here.
 */
public final class FluidUnits {
    public static final int DROPLETS_PER_MB = 81;

    private FluidUnits() {
    }

    public static int toDroplets(int millibuckets) {
        return millibuckets * DROPLETS_PER_MB;
    }

    public static long toDroplets(long millibuckets) {
        return millibuckets * DROPLETS_PER_MB;
    }

    public static int toMillibuckets(int droplets) {
        return droplets / DROPLETS_PER_MB;
    }

    public static long toMillibuckets(long droplets) {
        return droplets / DROPLETS_PER_MB;
    }
}
