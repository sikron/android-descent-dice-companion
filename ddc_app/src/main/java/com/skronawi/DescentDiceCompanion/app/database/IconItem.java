package com.skronawi.DescentDiceCompanion.app.database;

import com.skronawi.DescentDiceCompanion.lib.items.Item;

public class IconItem extends Item {

    private String iconName;

    public IconItem(String name, long id) {
        super(name, id);
    }

    public IconItem(String name, long id, boolean selected) {
        super(name, id, selected);
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }
}
