package net.nuclearteam.createnuclear.content.particles;

import net.nuclearteam.createnuclear.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.nuclearteam.createnuclear.content.explosion.CNNuclearExplosionSound;
import net.nuclearteam.createnuclear.foundation.utility.Maths;
import net.nuclearteam.createnuclear.infrastructure.config.CNConfigs;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.RandomSource;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class NuclearMushroomCloudParticle extends Particle {

    private static final Identifier TEXTURE = CreateNuclear.asResource("textures/particle/nuclear_mushroom_cloud.png");
    private static final Identifier TEXTURE_GLOW = CreateNuclear.asResource("textures/particle/nuclear_mushroom_cloud_glow.png");
    private static final Identifier TEXTURE_PINK = CreateNuclear.asResource("textures/particle/nuclear_mushroom_cloud_pink.png");
    private static final Identifier TEXTURE_PINK_GLOW = CreateNuclear.asResource("textures/particle/nuclear_mushroom_cloud_pink_glow.png");
    private static final NuclearMushroomCloudModel MODEL = new NuclearMushroomCloudModel();
    private static final int BALL_FOR = 10;
    private static final int GLOW_FOR = 20;
    private static final int FADE_SPEED = 10;
    private final float scale;

    private boolean playedRinging;
    private boolean playedExplosion;
    private boolean playedRumble;

    private final boolean pink;

    protected NuclearMushroomCloudParticle(ClientLevel level, double x, double y, double z, float scale, boolean pink) {
        super(level, x, y, z);
        this.gravity = 0.0F;
        this.lifetime = (int) Math.ceil(133.33F * scale);
        this.scale = scale + 0.2F;
        this.setSize(3.0F, 3.0F);
        this.pink = pink;
    }

    public void tick() {
        CNClientProxy.renderNukeSkyDarkFor = 70;
        CNClientProxy.muteNonNukeSoundsFor = 50;
        boolean large = this.scale > 2.0F;
        if(age > BALL_FOR / 2 + 5){
            if(!playedExplosion){
                playedExplosion = true;
                CreateNuclear.LOGGER.info("EXPLOSIOOOOOON");
                playSound(CNSoundEvents.NUCLEAR_EXPLOSION_MAIN.getMainEvent(), lifetime - 20, lifetime, 0.2F, false);
            }
        }
        if (age < BALL_FOR) {
            if (!playedRinging && CNConfigs.client().nuclearBombFlash.get()) {
                playedRinging = true;
                playSound(CNSoundEvents.NUCLEAR_EXPLOSION_RINGING.getMainEvent(), 100, 50, 0.05F, true);
                // playSound(CNSoundEvents.NUCLEAR_EXPLOSION_SHOCKWAVE.getMainEvent(), 100, 50, 0.05F, true);
            }
            CNClientProxy.renderNukeFlashFor = 16;
        } else if (age < lifetime - FADE_SPEED) {
            float life = (float) (Math.log(1 + (age - BALL_FOR) / (float) (lifetime - BALL_FOR))) * 2F;
            float explosionSpread = (12 * life + 4F) * scale;
            for (int i = 0; i < (1 + random.nextInt(2)) * scale; i++) {
                Vec3 from = new Vec3(random.nextFloat() - 0.5F, random.nextFloat() - 0.5F, random.nextFloat() - 0.5F).scale(scale * 1.4F).add(this.x, this.y, this.z);
                Vec3 away = new Vec3(random.nextFloat() - 0.5F, random.nextFloat() - 0.5F, random.nextFloat() - 0.5F).scale(2.34F);
                this.level.addParticle(CNParticleRegistry.NUCLEAR_MUSHROOM_CLOUD_SMOKE, from.x, from.y, from.z, away.x, away.y, away.z);
            }
            for (int j = 0; j < scale * scale; j++) {
                Vec3 explosionBase = new Vec3((random.nextFloat() - 0.5F) * explosionSpread, (-0.6F + random.nextFloat() * 0.5F) * explosionSpread * 0.1F, (random.nextFloat() - 0.5F) * explosionSpread).add(this.x, this.y, this.z);
                this.level.addParticle(CNParticleRegistry.NUCLEAR_MUSHROOM_CLOUD_EXPLOSION, explosionBase.x, explosionBase.y, explosionBase.z, 0, 0, 0);
            }
            if(age > BALL_FOR){
                if(!playedRumble){
                    playedRumble = true;

                    playSound(CNSoundEvents.NUCLEAR_EXPLOSION_RUMBLE_2.getMainEvent(), lifetime + 100, lifetime, 0.1F, true);
                }
            }
        }
        super.tick();
    }

    private void playSound(SoundEvent soundEvent, int duration, int fadesAt, float fadeInBy, boolean looping){
        Minecraft.getInstance().getSoundManager().queueTickingSound(new CNNuclearExplosionSound(soundEvent, this.x, this.y, this.z, duration, fadesAt, fadeInBy, looping));
    }

    /**
     * Upstream drew straight into the entity translucent buffer from {@code Particle.render}, which
     * is gone: 26.2 particles are extracted into render state by their group and submitted later.
     * This captures what upstream computed at render time -- position relative to the camera, the
     * animation progress and the fade -- for {@link MushroomCloudParticleGroup} to submit.
     */
    MushroomCloudParticleGroup.CloudState extract(Camera camera, float partialTick) {
        Vec3 vec3 = camera.position();
        float f = (float) (Mth.lerp((double) partialTick, this.xo, this.x) - vec3.x());
        float f1 = (float) (Mth.lerp((double) partialTick, this.yo, this.y) - vec3.y());
        float f2 = (float) (Mth.lerp((double) partialTick, this.zo, this.z) - vec3.z());
        PoseStack posestack = new PoseStack();
        posestack.translate(f, f1 - 0.5F, f2);
        posestack.scale(-scale, -scale, scale);
        float life = (float) (Math.log(1 + (age - BALL_FOR + partialTick) / (lifetime - BALL_FOR))) * 2F;
        int left = lifetime - age;
        float alpha = left <= FADE_SPEED ? left / (float) FADE_SPEED : 1.0F;
        int color = ((int) (alpha * 255.0F) << 24) | 0xFFFFFF;
        return new MushroomCloudParticleGroup.CloudState(
            posestack.last().copy(),
            RenderTypes.entityTranslucent(pink ? TEXTURE_PINK : TEXTURE),
            age >= BALL_FOR,
            age,
            Maths.smin(life, 1.0F, 0.5F),
            partialTick,
            getLightCoords(partialTick),
            color
        );
    }

    static void renderModel(MushroomCloudParticleGroup.CloudState state, PoseStack poseStack, VertexConsumer consumer) {
        MODEL.hideFireball(state.hideFireball());
        MODEL.animateParticle(state.age(), state.life(), state.partialTick());
        MODEL.renderToBuffer(poseStack, consumer, state.light(), OverlayTexture.NO_OVERLAY, state.color());
    }

    @Override
    public ParticleRenderType getGroup() {
        return MushroomCloudParticleGroup.SHEET;
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {

        @Override
        public Particle createParticle(SimpleParticleType typeIn, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
            if (xSpeed == 0.0) {
                xSpeed = 1.0F;
            }
            return new NuclearMushroomCloudParticle(worldIn, x, y, z, (float) Math.max(0.5F, xSpeed), ySpeed >= 1.0F);
        }
    }
}