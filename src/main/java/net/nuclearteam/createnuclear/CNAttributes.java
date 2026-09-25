package net.nuclearteam.createnuclear;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

public class CNAttributes {
    public static final Holder<Attribute> IRRADIATED_RESISTANCE = Registry.registerForHolder(
        BuiltInRegistries.ATTRIBUTE,
        CreateNuclear.asResource("generic.irradiated_resistance"),
        new RangedAttribute("attribute.name.createnuclear.generic.irradiated_resistance", 0, 0, 1).setSyncable(true)
    );

    /** Forces class loading from the initialiser; the field above does the registering. */
    public static void register() {
    }
}
