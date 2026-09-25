package net.nuclearteam.createnuclear;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.nuclearteam.createnuclear.content.effects.IodineEffect;
import net.nuclearteam.createnuclear.content.radiation.RadiationEffect;

public class CNEffects {
    public static final Holder<MobEffect> RADIATION = register("radiation",
            new RadiationEffect()
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED,
                            CreateNuclear.asResource("radiation"), -0.25f,
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    public static final Holder<MobEffect> IODINE = register("iodine", new IodineEffect());

    private static Holder<MobEffect> register(String name, MobEffect effect) {
        return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, CreateNuclear.asResource(name), effect);
    }

    /** Forces class loading from the initialiser; the fields above do the registering. */
    public static void register() {
    }
}
