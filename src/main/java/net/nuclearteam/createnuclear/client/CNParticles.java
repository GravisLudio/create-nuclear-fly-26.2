package net.nuclearteam.createnuclear.client;

import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.nuclearteam.createnuclear.CNParticleRegistry;
import net.nuclearteam.createnuclear.CNParticleTypes;
import net.nuclearteam.createnuclear.content.particles.IrradiatedParticles;
import net.nuclearteam.createnuclear.content.particles.NuclearMushroomCloudParticle;
import net.nuclearteam.createnuclear.content.particles.SmallNuclearExplosionParticle;

/**
 * Particle providers, which upstream registered from {@code RegisterParticleProvidersEvent} (and,
 * for the irradiated particles, through Create's {@code ICustomParticleData}).
 */
public final class CNParticles {
    private CNParticles() {
    }

    public static void register() {
        ParticleProviderRegistry registry = ParticleProviderRegistry.getInstance();
        registry.register(CNParticleTypes.IRRADIATED_PARTICLES, IrradiatedParticles.Provider::new);
        registry.register(CNParticleRegistry.NUCLEAR_MUSHROOM_CLOUD, new NuclearMushroomCloudParticle.Factory());
        registry.register(CNParticleRegistry.NUCLEAR_MUSHROOM_CLOUD_SMOKE, SmallNuclearExplosionParticle.NukeFactory::new);
        registry.register(CNParticleRegistry.NUCLEAR_MUSHROOM_CLOUD_EXPLOSION, SmallNuclearExplosionParticle.NukeFactory::new);
    }
}
