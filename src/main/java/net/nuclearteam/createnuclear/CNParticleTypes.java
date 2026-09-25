package net.nuclearteam.createnuclear;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.nuclearteam.createnuclear.content.particles.IrradiatedParticlesData;

/**
 * Particle types with data. Upstream went through Create's {@code ICustomParticleData}, which
 * bundled the type with its client factory; the factories are client-side now, in
 * {@code client.CNParticles}. The data-less mushroom cloud types are in {@link CNParticleRegistry}.
 */
public class CNParticleTypes {
    public static final String IRRADIATED_PARTICLES_ID = "irradiated_particles";

    public static final ParticleType<IrradiatedParticlesData> IRRADIATED_PARTICLES = register(
        IRRADIATED_PARTICLES_ID,
        FabricParticleTypes.complex(IrradiatedParticlesData.CODEC, IrradiatedParticlesData.STREAM_CODEC)
    );

    private static <T extends ParticleOptions> ParticleType<T> register(String name, ParticleType<T> type) {
        return Registry.register(BuiltInRegistries.PARTICLE_TYPE, CreateNuclear.asResource(name), type);
    }

    public static void register() {
    }
}
