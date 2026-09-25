package net.nuclearteam.createnuclear;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;

public class CNParticleRegistry {
    public static final SimpleParticleType NUCLEAR_MUSHROOM_CLOUD = register("nuclear_mushroom_cloud");
    public static final SimpleParticleType NUCLEAR_MUSHROOM_CLOUD_SMOKE = register("nuclear_mushroom_cloud_smoke");
    public static final SimpleParticleType NUCLEAR_MUSHROOM_CLOUD_EXPLOSION = register("nuclear_mushroom_cloud_explosion");

    private static SimpleParticleType register(String name) {
        return Registry.register(BuiltInRegistries.PARTICLE_TYPE, CreateNuclear.asResource(name), FabricParticleTypes.simple(false));
    }

    public static void register() {
    }
}
