package com.skronawi.DescentDiceCompanion.lib.dice;

import java.util.ArrayList;
import java.util.List;

public class DiceProvider {

    private static DiceProvider instance;
    private List<Die> attack;
    private List<Die> defense;

    private DiceProvider() {

        attack = new ArrayList<Die>();
        attack.add(new Die(DieInfo.BLUE));
        attack.add(new Die(DieInfo.RED));
        attack.add(new Die(DieInfo.YELLOW));
        attack.add(new Die(DieInfo.GREEN));

        defense = new ArrayList<Die>();
        defense.add(new Die(DieInfo.BLACK));
        defense.add(new Die(DieInfo.GREY));
        defense.add(new Die(DieInfo.BROWN));
    }

    public static void init() {
        instance = new DiceProvider();
    }

    public static DiceProvider getInstance() {
        return instance;
    }

    public List<Die> getAllDice() {
        List<Die> dice = new ArrayList<Die>();
        dice.addAll(attack);
        dice.addAll(defense);
        return dice;
    }

    public List<Die> getDefenseDice() {
        return defense;
    }

    public List<Die> getAttackDice() {
        return attack;
    }
}
