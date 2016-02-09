package com.skronawi.DescentDiceCompanion.lib.items;

import com.skronawi.DescentDiceCompanion.lib.dice.Die;
import com.skronawi.DescentDiceCompanion.lib.dice.ItemDice;

public class Item {

    private String name;
    private boolean selected;
    private ItemDice dice;
    private final long id;

    public Item(String name, long id) {
        this.name = name;
        selected = false;
        this.id = id;
        dice = new ItemDice();
    }

    public Item(String name, long id, boolean selected) {
        this(name, id);
        setSelected(selected);
    }

    public long getId() {
        return id;
    }

    public int getNumberOfDice(Die die) {
        int number = 0;
        for (Die itemDie : dice) {
            if (itemDie.getType() == die.getType()) {
                number++;
            }
        }
        return number;
    }

    public void setNumberOfDice(Die die, int number) {

        ItemDice tmpDice = new ItemDice();

        for (Die d : dice) {
            if (d.getType() != die.getType()) {
                tmpDice.add(d);
            }
        }
        for (int i = 0; i < number; i++) {
            tmpDice.add(die);
        }
        dice = tmpDice;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void setDice(ItemDice dice) {
        this.dice = dice;
    }

    public ItemDice getDice() {
        return dice;
    }

    public Item copy() {
        Item i = new Item(getName(), getId());
        i.setSelected(isSelected());
        i.setDice(getDice());
        return i;
    }
}
