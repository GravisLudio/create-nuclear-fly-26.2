package net.nuclearteam.createnuclear.foundation.advancement;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;


import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.predicates.ContextAwarePredicate;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.server.level.ServerPlayer;

public class SimpleCreateNuclearTrigger extends CriterionTriggerBase<SimpleCreateNuclearTrigger.Instance> {

    public SimpleCreateNuclearTrigger(String id) {
        super(id);
    }

    public void trigger(ServerPlayer player) {
        triggerWith(player, null);
    }

    public SimpleCreateNuclearTrigger.Instance instance() {
        return new SimpleCreateNuclearTrigger.Instance();
    }

    @Override
    public Codec<SimpleCreateNuclearTrigger.Instance> codec() {
        return SimpleCreateNuclearTrigger.Instance.CODEC;
    }

    public static class Instance extends CriterionTriggerBase.Instance {
        private static final Codec<SimpleCreateNuclearTrigger.Instance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(SimpleCreateNuclearTrigger.Instance::player)
        ).apply(instance, SimpleCreateNuclearTrigger.Instance::new));

        private final Optional<ContextAwarePredicate> player;

        public Instance() {
            player = Optional.empty();
        }

        public Instance(Optional<ContextAwarePredicate> player) {
            this.player = player;
        }

        @Override
        protected boolean test(@Nullable List<Supplier<Object>> suppliers) {
            return true;
        }

        @Override
        public Optional<ContextAwarePredicate> player() {
            return player;
        }
    }
}

