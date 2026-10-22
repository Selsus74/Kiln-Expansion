package de.selsus74.kilnexpansion.multiblock;

import de.selsus74.kilnexpansion.registry.ModBlockTags;
import de.selsus74.kilnexpansion.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public final class CharcoalPileStructure {

    //---in layers bottom to top, each layer front (Controller) to back---
    //---columns from left to right, looking at controller front---
    //---B = block in valid tag, C = Controller, empty = empty---
    private static final String[][] PATTERN = {
            {"  BCB  ", " BWWWB ", "BWWWWWB", "VWWWWWV", "BWWWWWB", " BWWWB ", "  BVB  "},
            {"   B   ", " BBWBB ", " BWWWB ", "BWWWWWB", " BWWWB ", " BBWBB ", "   B   "},
            {"       ", "  BBB  ", " BWWWB ", " BWWWB ", " BWWWB ", "  BBB  ", "       "},
            {"       ", "   B   ", "  BWB  ", " BWWWB ", "  BWB  ", "   B   ", "       "},
            {"       ", "       ", "  BBB  ", "  BWB  ", "  BBB  ", "       ", "       "},
            {"       ", "       ", "   B   ", "  BWB  ", "   B   ", "       ", "       "},
            {"       ", "       ", "       ", "   B   ", "       ", "       ", "       "}
    };

    //---controller block position in pattern---
    private static final int C_LAYER = 0;
    private static final int C_ROW = 0;
    private static final int C_COL = 3;

    private CharcoalPileStructure() {}

    //---is valid boolean checking the pattern---
    public static boolean isValid(Level level, BlockPos controller, Direction facing) {
        Direction back = facing.getOpposite();
        Direction right = facing.getCounterClockWise(); // looking at Controller front
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int layer = 0; layer < PATTERN.length; layer++) {
            for (int row = 0; row < PATTERN[layer].length; row++) {
                for (int col = 0; col < PATTERN[layer][row].length(); col++) {
                    char c = PATTERN[layer][row].charAt(col);
                    if (c == ' ' || c == 'C') continue;

                    pos.set(controller)
                            .move(right, col - C_COL)
                            .move(Direction.UP, layer - C_LAYER)
                            .move(back, row - C_ROW);

                    switch(c){
                        case 'B':
                            if (!level.getBlockState(pos).is(Blocks.PACKED_MUD)) {
                                return false;
                            }
                            break;
                        case 'V':
                            if (!level.getBlockState(pos).is(ModBlocks.CHARCOAL_PILE_VENT.get())) {
                                return false;
                            }
                            break;
                        case 'W':
                            continue;
                    }
                }
            }
        }
        return true;
    }
}