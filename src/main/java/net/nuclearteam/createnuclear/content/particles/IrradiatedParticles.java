package net.nuclearteam.createnuclear.content.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.*;
import net.minecraft.util.RandomSource;

import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class IrradiatedParticles extends SingleQuadParticle {
    protected IrradiatedParticles(ClientLevel pLevel, double pX, double pY, double pZ,
                                  SpriteSet spriteSet, double pXSpeed, double pYSpeed, double pZSpeed) {
        super(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, spriteSet.first());

        RandomSource randomSource = this.random;

        this.xo = randomSource.nextGaussian() * (double)1.0E-6f;
        this.yo = randomSource.nextGaussian() * (double)1.0E-4f;
        this.zo = randomSource.nextGaussian() * (double)1.0E-6f;

        this.quadSize *= this.random.nextFloat() * 0.6f + 0.6f;
        this.hasPhysics = false;
        this.gravity = 0.0f;

        this.friction = 0.8f;
        this.lifetime = 40;

        this.setSpriteFromAge(spriteSet);

    }

    @Override
    protected Layer getLayer() {
        return Layer.TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<IrradiatedParticlesData> {
        private final SpriteSet spriteSet;

        public Provider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Nullable
        @Override
        public Particle createParticle(IrradiatedParticlesData pType, ClientLevel pLevel, double pX, double pY, double pZ,
                                       double pXSpeed, double pYSpeed, double pZSpeed, RandomSource random) {
            return new IrradiatedParticles(pLevel, pX, pY, pZ, this.spriteSet, pXSpeed, pYSpeed, pZSpeed);
        }
    }
}