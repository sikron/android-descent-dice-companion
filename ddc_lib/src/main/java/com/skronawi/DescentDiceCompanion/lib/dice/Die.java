package com.skronawi.DescentDiceCompanion.lib.dice;

public class Die {

    private DieInfo die;

    public Die(DieInfo die) {
        this.die = die;
    }

    public boolean isMiss(int sideIdx) {

        int[][] sides = die.getSides();
        if (sideIdx > sides.length) {
            throw new IllegalArgumentException("the given sideIdx value of " + sideIdx + "is invalid. " +
                    "a die has max " + sides.length + " sides");
        }

        int[] side = sides[sideIdx];
        boolean isMiss = true;
        for (int aSide : side) {
            if (aSide > -1) {
                isMiss = false;
                break;
            }
        }
        return isMiss;
    }

    public int getIndex() {
        return die.getIndex();
    }

    public DieInfo getType() {
        return die;
    }

    public int getRange(int sideIdx) {
        return getSideValue(sideIdx, 0);
    }

    public int getSurges(int sideIdx) {
        return getSideValue(sideIdx, 1);
    }

    public int getPoints(int sideIdx) {
        return getSideValue(sideIdx, 2);
    }

    private int getSideValue(int sideIdx, int valueIdx) {
        int[][] sides = die.getSides();
        if (sideIdx > sides.length) {
            throw new IllegalArgumentException("the given sideIdx value of " + sideIdx + "is invalid. " +
                    "a die has max " + sides.length + " sides");
        }
        int[] side = sides[sideIdx];
        if (valueIdx > side.length) {
            throw new IllegalArgumentException("the given valueIdx value of " + valueIdx + "is invalid. " +
                    "a die-side has max " + side.length + " values");
        }
        return side[valueIdx];
    }
}
