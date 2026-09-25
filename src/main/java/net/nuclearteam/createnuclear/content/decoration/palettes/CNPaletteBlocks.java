package net.nuclearteam.createnuclear.content.decoration.palettes;

import net.nuclearteam.createnuclear.foundation.registrate.CNRegistrate;
import net.nuclearteam.createnuclear.CNCreativeModeTabs;
import net.nuclearteam.createnuclear.CreateNuclear;

public class CNPaletteBlocks {
    private static final CNRegistrate REGISTRATE = CreateNuclear.REGISTRATE;

    static {
        REGISTRATE.setCreativeTab(CNCreativeModeTabs.MAIN);
        CNPaletteStoneTypes.register(REGISTRATE);
    }

    public static void register() {}


}
