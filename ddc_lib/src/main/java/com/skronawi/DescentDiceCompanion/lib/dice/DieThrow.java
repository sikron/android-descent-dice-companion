package com.skronawi.DescentDiceCompanion.lib.dice;

public class DieThrow {

    public static final String MISS = "X";

    private Die die;
    private int sideIdx;

    public Die getDie() {
        return die;
    }

    public DieThrow setDie(Die die) {
        this.die = die;
        return this;
    }

    public int getSideIdx() {
        return sideIdx;
    }

    public DieThrow setSideIdx(int sideIdx) {
        this.sideIdx = sideIdx;
        return this;
    }

    public boolean isMiss() {
        return die.isMiss(sideIdx);
    }

    public int getRangeResult() {
        return die.getRange(sideIdx);
    }

    public int getSurgesResult() {
        return die.getSurges(sideIdx);
    }

    public int getPointsResult() {
        return die.getPoints(sideIdx);
    }
}
