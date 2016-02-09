package com.skronawi.DescentDiceCompanion.lib.dice;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ItemDice implements Iterable<Die> {

    private List<Die> dice;

    public ItemDice() {
        dice = new ArrayList<Die>(10);
    }

    public ItemDice add(Die die) {
        dice.add(die);
        return this;
    }

    public int size() {
        return dice.size();
    }

    public Die getAtIndex(int index) {
        return dice.get(index);
    }

    @Override
    public Iterator<Die> iterator() {
        return dice.iterator();
    }


    public int amountOf(DieInfo type) {
        int number = 0;
        for (Die die : dice) {
            if (die.getType() == type) {
                number++;
            }
        }
        return number;
    }

    /*
    --------------------------------------------------------------------------------------
    never change the order of DieInfo.values()
    --------------------------------------------------------------------------------------
     */
    public String toMask() {
        String mask = "";
        for (DieInfo dieInfo : DieInfo.values()) {
            mask += String.valueOf(amountOf(dieInfo));
        }
        return mask;
    }

    public static ItemDice fromMask(String mask) {

        ItemDice itemDice = new ItemDice();

        for (int i = 0; i < mask.length(); i++) {
            int number = Integer.parseInt(String.valueOf(mask.charAt(i)));
            DieInfo dieInfo = DieInfo.values()[i];
            for (int j = 0; j < number; j++) {
                itemDice.add(new Die(dieInfo));
            }
        }
        return itemDice;
    }
}
