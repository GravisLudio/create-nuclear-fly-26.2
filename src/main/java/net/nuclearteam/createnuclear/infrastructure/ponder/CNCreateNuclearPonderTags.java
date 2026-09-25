package net.nuclearteam.createnuclear.infrastructure.ponder;

import com.tterrag.registrate.util.entry.RegistryEntry;
import com.zurrtum.create.catnip.registry.RegisteredObjectsHelper;
import com.zurrtum.create.client.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ItemLike;
import net.nuclearteam.createnuclear.CNBlocks;
import net.nuclearteam.createnuclear.CreateNuclear;

import static com.zurrtum.create.client.infrastructure.ponder.AllCreatePonderTags.DISPLAY_SOURCES;
import static com.zurrtum.create.client.infrastructure.ponder.AllCreatePonderTags.KINETIC_SOURCES;


public class CNCreateNuclearPonderTags {
    private static Identifier loc(String id) {
        return CreateNuclear.asResource(id);
    }

    public static void register(PonderTagRegistrationHelper<Identifier> helper) {
        PonderTagRegistrationHelper<RegistryEntry<?, ?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);
        PonderTagRegistrationHelper<ItemLike> itemHelper = helper.withKeyFunction(
                RegisteredObjectsHelper::getKeyOrThrow);

        helper.registerTag(KINETIC_SOURCES)
                .addToIndex()
                .item(CNBlocks.REACTOR_CONTROLLER.asItem())
                .title("Kinetic Nuclear")
                .register();

        HELPER.addToTag(KINETIC_SOURCES)
                .add(CNBlocks.REACTOR_CONTROLLER)
        ;

        HELPER.addToTag(DISPLAY_SOURCES)
                .add(CNBlocks.REACTOR_CONTROLLER)
        ;

    }

}
