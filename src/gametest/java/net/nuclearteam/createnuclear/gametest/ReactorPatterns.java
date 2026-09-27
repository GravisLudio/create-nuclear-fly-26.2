package net.nuclearteam.createnuclear.gametest;

import lib.multiblock.SimpleMultiBlockAislePatternBuilder;
import lib.multiblock.impl.IMultiBlockPattern;
import net.nuclearteam.createnuclear.CNBlocks;

/** The reactor patterns of CNMultiblock with block providers, so the tests can build them. */
final class ReactorPatterns {
    private ReactorPatterns() {}

    static final String[][] REACTOR_5 = {
        {"OOOOO", "OAAAO", "OAAAO", "OAAAO", "OOOOO"},
        {"OABAO", "ADDDA", "BDCDB", "ADDDA", "OABAO"},
        {"OABAO", "ADDDA", "BDCDB", "ADDDA", "OABAO"},
        {"OABAO", "ADDDA", "BDCDB", "ADDDA", "OA*AO"},
        {"OABAO", "ADDDA", "BDCDB", "ADDDA", "OABAO"},
        {"OABAO", "ADDDA", "BDCDB", "ADDDA", "OABAO"},
        {"OOOOO", "OAAAO", "OAAAO", "OAAAO", "OOOOO"}};
    static final String[][] REACTOR_7 = {
        {"OOOOOOO", "OAAAAAO", "OAAAAAO", "OAAAAAO", "OAAAAAO", "OAAAAAO", "OOOOOOO"},
        {"OABABAO", "ADDDDDA", "BDCDCDB", "ADDDDDA", "BDCDCDB", "ADDDDDA", "OABABAO"},
        {"OABABAO", "ADDDDDA", "BDCDCDB", "ADDDDDA", "BDCDCDB", "ADDDDDA", "OABABAO"},
        {"OABABAO", "ADDDDDA", "BDCDCDB", "ADDDDDA", "BDCDCDB", "ADDDDDA", "OABABAO"},
        {"OABABAO", "ADDDDDA", "BDCDCDB", "ADDDDDA", "BDCDCDB", "ADDDDDA", "OAB*BAO"},
        {"OABABAO", "ADDDDDA", "BDCDCDB", "ADDDDDA", "BDCDCDB", "ADDDDDA", "OABABAO"},
        {"OABABAO", "ADDDDDA", "BDCDCDB", "ADDDDDA", "BDCDCDB", "ADDDDDA", "OABABAO"},
        {"OABABAO", "ADDDDDA", "BDCDCDB", "ADDDDDA", "BDCDCDB", "ADDDDDA", "OABABAO"},
        {"OOOOOOO", "OAAAAAO", "OAAAAAO", "OAAAAAO", "OAAAAAO", "OAAAAAO", "OOOOOOO"}};
    static final String[] R9_CAP = {"OOOOOOOOO", "OAAAAAAAO", "OAAAAAAAO", "OAAAAAAAO", "OAAAAAAAO", "OAAAAAAAO", "OAAAAAAAO", "OAAAAAAAO", "OOOOOOOOO"};
    static final String[] R9_MID = {"OBAABAABO", "BDDDDDDDB", "ADCDCDCDA", "ADDDDDDDA", "BDCDCDCDB", "ADDDDDDDA", "ADCDCDCDA", "BDDDDDDDB", "OBAABAABO"};
    static final String[] R9_CTRL = {"OBAABAABO", "BDDDDDDDB", "ADCDCDCDA", "ADDDDDDDA", "BDCDCDCDB", "ADDDDDDDA", "ADCDCDCDA", "BDDDDDDDB", "OBAA*AABO"};
    static final String[][] REACTOR_9 = {R9_CAP, R9_MID, R9_MID, R9_MID, R9_MID, R9_CTRL, R9_MID, R9_MID, R9_MID, R9_MID, R9_CAP};

    static IMultiBlockPattern build(String[][] aisles) {
        SimpleMultiBlockAislePatternBuilder builder = SimpleMultiBlockAislePatternBuilder.start();
        for (String[] aisle : aisles) {
            builder.aisle(aisle);
        }
        return builder
            .where('A', b -> true).where('B', b -> true).where('C', b -> true)
            .where('D', b -> true).where('O', b -> true).where('*', b -> true)
            .block('A', () -> CNBlocks.REACTOR_CASING.getDefaultState())
            .block('B', () -> CNBlocks.REACTOR_FRAME.getDefaultState())
            .block('C', () -> CNBlocks.REACTOR_CORE.getDefaultState())
            .block('D', () -> CNBlocks.REACTOR_COOLER.getDefaultState())
            .block('O', () -> CNBlocks.REACTOR_CASING.getDefaultState())
            .block('*', () -> CNBlocks.REACTOR_CONTROLLER.getDefaultState())
            .build();
    }
}
