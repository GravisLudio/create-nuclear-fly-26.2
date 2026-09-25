package net.nuclearteam.createnuclear.client;

import com.zurrtum.create.client.AllCasings;
import com.zurrtum.create.client.AllModels;
import com.zurrtum.create.client.content.decoration.encasing.EncasedCTBehaviour;
import com.zurrtum.create.client.infrastructure.model.CTModel;
import net.minecraft.world.level.block.Block;
import net.nuclearteam.createnuclear.CNBlocks;
import com.zurrtum.create.client.foundation.block.connected.CTSpriteShiftEntry;

/**
 * Connected textures and casing connectivity, which upstream chained onto registration in
 * {@code CNBlocks}:
 * <pre>{@code
 * .onRegister(CreateRegistrate.connectedTextures(() -> new EncasedCTBehaviour(CNSpriteShifts.REACTOR_CASING)))
 * .onRegister(casingConnectivity((block, cc) -> cc.makeCasing(block, CNSpriteShifts.REACTOR_CASING)))
 * }</pre>
 * Both are client classes and {@code CNBlocks} runs on both sides, so they moved here, in Create
 * Fly's split: {@code AllModels.register} binds the CT model, {@code AllCasings.make} the casing.
 * Same shape as the Connected port's {@code CCConnectedTextures}.
 */
public final class CNConnectedTextures {
    private CNConnectedTextures() {
    }

    public static void register() {
        casing(CNBlocks.REACTOR_CASING.get(), CNSpriteShifts.REACTOR_CASING);
        casing(CNBlocks.REINFORCED_GLASS.get(), CNSpriteShifts.REACTOR_GLASS);
    }

    private static void casing(Block block, CTSpriteShiftEntry shift) {
        AllModels.register(block, CTModel.of(new EncasedCTBehaviour(shift)));
        AllCasings.make(block, shift);
    }
}
