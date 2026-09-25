package net.nuclearteam.createnuclear.content.particles;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.ParticleGroupRenderState;

import java.util.List;

/**
 * Render group for the mushroom cloud, which is a whole model rather than a quad.
 * <p>
 * Upstream's particle used {@code ParticleRenderType.CUSTOM} and rendered itself; 26.2 renders
 * particles per group, from extracted state, and a custom type needs its own group -- the same
 * shape as vanilla's {@code ElderGuardianParticleGroup}, which also draws a model. The group is
 * created and added to the render order by {@code ParticleEngineMixin}. The model is Tabula-style
 * geometry that writes its own vertices, so it goes through {@code submitCustomGeometry} rather
 * than {@code submitModel}.
 */
@Environment(EnvType.CLIENT)
public class MushroomCloudParticleGroup extends ParticleGroup<NuclearMushroomCloudParticle> {
    public static final ParticleRenderType SHEET = new ParticleRenderType("createnuclear:mushroom_cloud", "CN");

    public MushroomCloudParticleGroup(ParticleEngine engine) {
        super(engine);
    }

    @Override
    public ParticleGroupRenderState extractRenderState(Frustum frustum, Camera camera, float partialTick) {
        return new State(particles.stream().map(particle -> particle.extract(camera, partialTick)).toList());
    }

    public record CloudState(PoseStack.Pose pose, RenderType renderType, boolean hideFireball, int age, float life,
                             float partialTick, int light, int color) {
    }

    record State(List<CloudState> clouds) implements ParticleGroupRenderState {
        @Override
        public void submit(SubmitNodeCollector collector, CameraRenderState camera) {
            for (CloudState cloud : clouds) {
                PoseStack poseStack = new PoseStack();
                poseStack.last().set(cloud.pose());
                collector.submitCustomGeometry(poseStack, cloud.renderType(), (pose, consumer) -> {
                    PoseStack local = new PoseStack();
                    local.last().set(pose);
                    NuclearMushroomCloudParticle.renderModel(cloud, local, consumer);
                });
            }
        }
    }
}
