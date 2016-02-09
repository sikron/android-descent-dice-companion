package com.skronawi.DescentDiceCompanion.lib.dice;


public enum DieInfo {
    //order is: range, surges, points (damage/defense).
    //(-1,-1,-1) = miss

    /*
    --------------------------------------------------------------------------------------
    never change the order of DieInfo.values()
    --------------------------------------------------------------------------------------
     */

    BLACK(0,
            new int[]{0, 0, 4},
            new int[]{0, 0, 3},
            new int[]{0, 0, 2},
            new int[]{0, 0, 2},
            new int[]{0, 0, 2},
            new int[]{0, 0, 0}),
    GREY(1,
            new int[]{0, 0, 3},
            new int[]{0, 0, 2},
            new int[]{0, 0, 1},
            new int[]{0, 0, 1},
            new int[]{0, 0, 1},
            new int[]{0, 0, 0}),
    BROWN(2,
            new int[]{0, 0, 2},
            new int[]{0, 0, 1},
            new int[]{0, 0, 1},
            new int[]{0, 0, 0},
            new int[]{0, 0, 0},
            new int[]{0, 0, 0}),
    BLUE(3,
            new int[]{6, 1, 1},
            new int[]{5, 0, 1},
            new int[]{4, 0, 2},
            new int[]{3, 0, 2},
            new int[]{2, 1, 2},
            new int[]{-1, -1, -1}), //miss
    RED(4,
            new int[]{0, 1, 3},
            new int[]{0, 0, 3},
            new int[]{0, 0, 2},
            new int[]{0, 0, 2},
            new int[]{0, 0, 2},
            new int[]{0, 0, 1}),
    YELLOW(5,
            new int[]{2, 0, 1},
            new int[]{1, 0, 1},
            new int[]{1, 1, 0},
            new int[]{0, 1, 2},
            new int[]{0, 0, 2},
            new int[]{0, 1, 1}),
    GREEN(6,
            new int[]{0, 1, 1},
            new int[]{0, 0, 1},
            new int[]{1, 1, 0},
            new int[]{1, 1, 1},
            new int[]{0, 1, 0},
            new int[]{1, 0, 1});

    public static final int NUMBER_OF_SIDES = 6;

    private final int[][] sides;
    private final int idx;

    DieInfo(int idx, int[]... sides) {
        this.idx = idx;
        this.sides = sides;
    }

    public int[][] getSides() {
        return sides;
    }

    public int getIndex() {
        return idx;
    }

    public static DieInfo byIndex(int index) {
        for (DieInfo die : values()) {
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
