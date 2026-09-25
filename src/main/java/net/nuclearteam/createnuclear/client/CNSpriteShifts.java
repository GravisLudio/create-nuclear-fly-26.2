package net.nuclearteam.createnuclear.client;

import net.nuclearteam.createnuclear.CreateNuclear;

import com.zurrtum.create.client.foundation.block.connected.AllCTTypes;
import com.zurrtum.create.client.foundation.block.connected.CTSpriteShiftEntry;
import com.zurrtum.create.client.foundation.block.connected.CTSpriteShifter;
import com.zurrtum.create.client.foundation.block.connected.CTType;

public class CNSpriteShifts {
    public static final CTSpriteShiftEntry REACTOR_CASING = omni("reactor/casing/reactor_casing");
    public static final CTSpriteShiftEntry REACTOR_GLASS = omni("reactor/reinforced/glass");

    // Autunite palette, as upstream's PaletteBlockPattern.CTs built them.
    public static final CTSpriteShiftEntry AUTUNITE_LAYERED = palette(AllCTTypes.HORIZONTAL_KRYPPERS, "layered");
    public static final CTSpriteShiftEntry AUTUNITE_PILLAR = palette(AllCTTypes.RECTANGLE, "pillar");
    public static final CTSpriteShiftEntry AUTUNITE_CAP = palette(AllCTTypes.OMNIDIRECTIONAL, "cap");

    private static CTSpriteShiftEntry palette(CTType type, String texture) {
        return getCT(type, "palettes/stone_types/" + texture + "/autunite_cut_" + texture);
    }

    private static CTSpriteShiftEntry omni(String name) {
        return getCT(AllCTTypes.OMNIDIRECTIONAL, name);
    }

    private static CTSpriteShiftEntry getCT(CTType type, String blockTextureName, String connectedTextureName) {
        return CTSpriteShifter.getCT(type, CreateNuclear.asResource("block/" + blockTextureName),
                CreateNuclear.asResource("block/" + connectedTextureName + "_connected"));
    }

    private static CTSpriteShiftEntry getCT(CTType type, String blockTextureName) {
        return getCT(type, blockTextureName, blockTextureName);
    }
}
