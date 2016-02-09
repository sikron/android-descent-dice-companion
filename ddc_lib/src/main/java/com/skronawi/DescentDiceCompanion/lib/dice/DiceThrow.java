package com.skronawi.DescentDiceCompanion.lib.dice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/*
intention is "per item". a diceThrow for several items is then a List<DiceThrow>.
 */
public class DiceThrow implements Iterable<DieThrow> {

    private ArrayList<DieThrow> dieThrows;

    public DiceThrow() {
        dieThrows = new ArrayList<DieThrow>(10);
    }

    public static String[] accumulate(Collection<DiceThrow> diceThrows) {


        for (DiceThrow diceThrow : diceThrows) {
            if (diceThrow.isMiss()) {
                return new String[]{DieThrow.MISS, DieThrow.MISS, DieThrow.MISS};
            }
        }

        int[] accInt = new int[3];
        for (DiceThrow diceThrow : diceThrows) {
            accInt[0] += Integer.valueOf(diceThrow.getRangeResult());
            accInt[1] += Integer.valueOf(diceThrow.getSurgeResult());
            accInt[2] += Integer.valueOf(diceThrow.getPointsResult());
        }
        String[] acc = new String[3];
        for (int i = 0; i < acc.length; i++) {
            acc[i] = String.valueOf(accInt[i]);
        }
        return acc;
    }

    public DiceThrow add(DieThrow dieThrow) {
        dieThrows.add(dieThrow);
        return this;
    }

    @Override
    public Iterator<DieThrow> iterator() {
        return dieThrows.iterator();
    }

    public int size() {
        return dieThrows.size();
    }

    public boolean isMiss() {

        for (DieThrow dieThrow : dieThrows) {
            if (dieThrow.isMiss()) {
                return true;
            }
        }
        return false;
    }

    public String getRangeResult() {

        if (isMiss()) {
            return DieThrow.MISS;
        }

        int rangeResult = 0;

        for (DieThrow dieThrow : dieThrows) {
            rangeResult += dieThrow.getRangeResult();
        }

        return String.valueOf(rangeResult);
    }

    public String getSurgeResult() {

        if (isMiss()) {
            return DieThrow.MISS;
        }

        int surgeResult = 0;

        for (DieThrow dieThrow : dieThrows) {
            surgeResult += dieThrow.getSurgesResult();
        }

        return String.valueOf(surgeResult);
    }

    public String getPointsResult() {

        if (isMiss()) {
            return DieThrow.MISS;
        }

        int pointsResult = 0;

        for (DieThrow dieThrow : dieThrows) {
            pointsResult += dieThrow.getPointsResult();
        }

        return String.valueOf(pointsResult);
    }

    public DieThrow get(int idx) {
        return dieThrows.get(idx);
    }
}
