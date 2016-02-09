package com.skronawi.DescentDiceCompanion.app.icons;

import java.util.Arrays;
import java.util.List;

public class IconProvider {

    private static List<Icon> attackIcons;

    static {
        attackIcons = Arrays.asList(Icon.AXE, Icon.BOW, Icon.STAFF, Icon.SWORD, Icon.HAMMER,
                Icon.DAGGER, Icon.RUNE, Icon.SLING, Icon.SPEAR, Icon.CROSSBOW, Icon.DIE);
    }

    private static List<Icon> defenseIcons;

    static {
        defenseIcons = Arrays.asList(Icon.SHIELD, Icon.PLATE, Icon.CLOAK, Icon.BOOTS,
                Icon.GAUNTLETS, Icon.HELMET, Icon.RING, Icon.DIE);
    }

    public static List<Icon> getAttackIcons() {
        return attackIcons;
    }

    public static List<Icon> getDefenseIcons() {
        return defenseIcons;
    }
}
