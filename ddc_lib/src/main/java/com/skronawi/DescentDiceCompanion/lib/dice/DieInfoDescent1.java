package com.skronawi.DescentDiceCompanion.lib.dice;


public enum DieInfoDescent1 {
    //order is: range, surges, points (damage/defense).
    //(-1,-1,-1) = miss

    POWER(0,
            new int[]{0, 0, 0},
            new int[]{0, 1, 0},
            new int[]{0, 1, 0},
            new int[]{1, 0, 1},  // 1 range XOR damage
            new int[]{1, 0, 1},  // 1 range XOR damage
            new int[]{1, 0, 1}),  // 1 range XOR damage
    GREEN(1,
            new int[]{0, 0, 3},
            new int[]{0, 0, 3},
            new int[]{0, 1, 2},
            new int[]{0, 1, 2},
            new int[]{1, 0, 1},
            new int[]{1, 0, 2}),
    WHITE(2,
            new int[]{-1, -1, -1},
            new int[]{1, 1, 3},
            new int[]{1, 1, 3},
            new int[]{2, 0, 2},
            new int[]{3, 1, 1},
            new int[]{3, 1, 1}),
    BLUE(3,
            new int[]{-1, -1, -1},
            new int[]{1, 0, 2},
            new int[]{2, 0, 2},
            new int[]{3, 1, 1},
            new int[]{3, 1, 1},
            new int[]{4, 1, 0}),
    RED(4,
            new int[]{-1, -1, -1},
            new int[]{0, 0, 4},
            new int[]{1, 0, 3},
            new int[]{1, 1, 3},
            new int[]{2, 1, 1},
            new int[]{2, 0, 2}),
    YELLOW(5,
            new int[]{1, 0, 1},
            new int[]{2, 0, 1},
            new int[]{2, 1, 0},
            new int[]{2, 1, 0},
            new int[]{3, 0, 0},
            new int[]{3, 0, 0})

////NOT READY  !!!!! -------------------------------------------      
//    ,SILVER(6,
//            new int[]{0,0,0},
//            new int[]{0,2,0},
//            new int[]{0,2,0},     // 2 x POWER
//            new int[]{2,0,2},
//            new int[]{2,0,2},
//            new int[]{2,0,2}),
////NOT READY  !!!!! -------------------------------------------
//    GOLD(7,
//            new int[]{0,0,0},
//            new int[]{0,3,0},
//            new int[]{0,3,0},     // 3 x POWER
//            new int[]{3,0,3},
//            new int[]{3,0,3},
//            new int[]{3,0,3}),            
//    STEALTH(8,
//            new int[]{-1,-1,-1},
//            new int[]{-1,-1,-1},
//            new int[]{0,0,0},
//            new int[]{0,0,0},
//            new int[]{0,0,0},
//            new int[]{0,0,0})
    ;


    private final int[][] sides;
    private final int idx;

    DieInfoDescent1(int idx, int[]... sides) {
        this.idx = idx;
        this.sides = sides;
    }

    public int[][] getSides() {
        return sides;
    }

    public int getIndex() {
        return idx;
    }

    public static DieInfoDescent1 byIndex(int index) {
        for (DieInfoDescent1 die : values()) {
            if (die.idx == index)
                return die;
        }
        return null;
    }

    public static boolean isMiss(int[] side) {
        boolean isMiss = true;
        for (int i = 0; i < side.length; i++) {
            if (side[i] > -1) {
                isMiss = false;
                break;
            }
        }
        return isMiss;
    }
}
