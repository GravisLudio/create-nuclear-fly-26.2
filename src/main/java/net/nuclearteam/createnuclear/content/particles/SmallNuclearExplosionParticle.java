package net.nuclearteam.createnuclear.content.particles;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;

/**
 * Smoke and fire puffs of the mushroom cloud.
 * <p>
 * Upstream carried Alex's Caves' whole factory set (mine, underzealot, raygun and more); only the
 * nuke factory was ever registered, so only it is kept. {@code TextureSheetParticle} became
 * {@link SingleQuadParticle}, which takes its first sprite up front, and the lit particle sheet
 * is the opaque layer with full brightness from {@link #getLightCoords}.
 */
@Environment(EnvType.CLIENT)
public class SmallNuclearExplosionParticle extends SingleQuadParticle {

    private final SpriteSet sprites;
    private boolean hasFadeColor = false;
    private float fadeR;
    private float fadeG;
    private float fadeB;

    protected SmallNuclearExplosionParticle(ClientLevel world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites, boolean shortLifespan, int color1) {
        super(world, x, y, z, xSpeed, ySpeed, zSpeed, sprites.first());
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;
        this.setSize(0.5F, 0.5F);
        this.quadSize = (shortLifespan ? 1 : 0.8F) + this.random.nextFloat() * 0.3F;
        this.lifetime = shortLifespan ? 5 + this.random.nextInt(3) : 15 + this.random.nextInt(10);
        this.friction = 0.96F;
        float randCol = this.random.nextFloat() * 0.05F;
        this.sprites = sprites;
        this.setColor(Math.min(ARGB.red(color1) / 255F + randCol, 1), Math.min(1F, ARGB.green(color1) / 255F + randCol), Math.min(1F, ARGB.blue(color1) / 255F + randCol));
    }

    public void setFadeColor(int i) {
        hasFadeColor = true;
        this.fadeR = (float) ((i & 16711680) >> 16) / 255.0F;
        this.fadeG = (float) ((i & '＀') >> 8) / 255.0F;
        this.fadeB = (float) ((i & 255) >> 0) / 255.0F;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.setSpriteFromAge(this.sprites);
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            if (hasFadeColor) {
                this.rCol += (fadeR - this.rCol) * 0.2F;
                this.gCol += (fadeG - this.gCol) * 0.2F;
                this.bCol += (fadeB - this.bCol) * 0.2F;
            } else {
                this.rCol = this.rCol * 0.95F;
                this.gCol = this.gCol * 0.95F;
                this.bCol = this.bCol * 0.95F;
            }
            this.move(this.xd, this.yd, this.zd);
            this.xd *= (double) this.friction;
            this.yd *= (double) this.friction;
            this.zd *= (double) this.friction;
        }
    }

    @Override
    protected Layer getLayer() {
        return Layer.OPAQUE;
    }

    @Override
    protected int getLightCoords(float partialTicks) {
        return 240;
    }

    public static class NukeFactory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public NukeFactory(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
            SmallNuclearExplosionParticle particle = new SmallNuclearExplosionParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, spriteSet, false, 0XFFB300);
            particle.setSpriteFromAge(spriteSet);
            return particle;
        }
    }
}
