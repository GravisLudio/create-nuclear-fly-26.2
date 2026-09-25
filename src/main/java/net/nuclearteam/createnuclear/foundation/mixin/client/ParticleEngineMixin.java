package net.nuclearteam.createnuclear.foundation.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleRenderType;
import net.nuclearteam.createnuclear.content.particles.MushroomCloudParticleGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Registers the mushroom cloud's particle group: the engine only builds groups for render types it
 * knows and only renders the types in its render order. Create Fly adds its own groups the same
 * way; both wrap the same {@code List.of} call, which MixinExtras chains.
 */
@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {
    @Inject(method = "createParticleGroup", at = @At("TAIL"), cancellable = true)
    private void createnuclear$createParticleGroup(ParticleRenderType type, CallbackInfoReturnable<ParticleGroup<?>> cir) {
        if (type == MushroomCloudParticleGroup.SHEET) {
            cir.setReturnValue(new MushroomCloudParticleGroup((ParticleEngine) (Object) this));
        }
    }

    @WrapOperation(method = "<clinit>", at = @At(value = "INVOKE", target = "Ljava/util/List;of(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/util/List;", remap = false))
    private static <E> List<ParticleRenderType> createnuclear$renderOrder(E e1, E e2, E e3, Operation<List<ParticleRenderType>> original) {
        List<ParticleRenderType> list = original.call(e1, e2, e3);
        if (!(list instanceof ArrayList<ParticleRenderType>)) {
            list = new ArrayList<>(list);
        }
        list.add(MushroomCloudParticleGroup.SHEET);
        return list;
    }
}
