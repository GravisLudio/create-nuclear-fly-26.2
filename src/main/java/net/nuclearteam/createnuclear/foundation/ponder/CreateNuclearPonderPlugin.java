package net.nuclearteam.createnuclear.foundation.ponder;

import com.zurrtum.create.client.ponder.api.registration.PonderPlugin;
import com.zurrtum.create.client.ponder.api.registration.PonderSceneRegistrationHelper;
import com.zurrtum.create.client.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.Identifier;
import net.nuclearteam.createnuclear.CreateNuclear;
import net.nuclearteam.createnuclear.infrastructure.ponder.CNCreateNuclearPonderTags;

public class CreateNuclearPonderPlugin implements PonderPlugin {
    @Override
    public String getModId() {
        return CreateNuclear.MOD_ID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<Identifier> helper) {
        CNPonderIndex.register(helper);
    }

    @Override
    public void registerTags(PonderTagRegistrationHelper<Identifier> helper) {
        CNCreateNuclearPonderTags.register(helper);
    }
}
