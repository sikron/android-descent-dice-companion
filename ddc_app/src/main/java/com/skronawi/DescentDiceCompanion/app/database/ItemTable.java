package com.skronawi.DescentDiceCompanion.app.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ItemTable {

    public static final String DATABASE_NAME = "d2c.db";
    public static final int DATABASE_VERSION = 4;
    /*
    history
    2: green die added in db-version 2 -> bitmask of dice + "0"
    3: added the column for the icon name
    4: added special items for hero: standup and characteristics
     */

    public static final String TABLE_ITEMS = "item";

    public enum ItemColumn {
        ID(0, "_id", "INTEGER PRIMARY KEY AUTOINCREMENT"),
        CATEGORY(1, "category", "INTEGER"),   //attack, defense
        SELECTED(2, "selected", "INTEGER"),
        NAME(3, "name", "TEXT"),
        DICE(4, "dice", "TEXT"),  //mask for the dice, e.g. 0210000 (2 grey dice and one brown)
        ICON(5, "icon", "TEXT"); //the name of the icon picture, without the extension

        private final int index;
        private final String name;
        private final String type;

        ItemColumn(int index, String name, String type) {
            this.index = index;
            this.name = name;
            this.type = type;
        }

        public int getIndex() {
            return index;
        }

        public String getName() {
            return name;
        }
    }

    public static final int CATEGORY_ATTACK = 1;
    public static final int CATEGORY_DEFENSE = 2;
    public static final int CATEGORY_HERO = 3;

    public static final long NEW_ITEM_ID = -1;

    private static final String TABLE_ITEMS_CREATE =
            "CREATE TABLE " + TABLE_ITEMS + " (" +
                    ItemColumn.ID.name + " " + ItemColumn.ID.type + ", " +
                    ItemColumn.CATEGORY.name + " " + ItemColumn.CATEGORY.type + ", " +
                    ItemColumn.SELECTED.name + " " + ItemColumn.SELECTED.type + ", " +
                    ItemColumn.NAME.name + " " + ItemColumn.NAME.type + ", " +
                    ItemColumn.DICE.name + " " + ItemColumn.DICE.type + ", " +
                    ItemColumn.ICON.name + " " + ItemColumn.ICON.type + ");";

    private static final String TABLE_ITEMS_DROP =
            "DROP TABLE IF EXISTS " + TABLE_ITEMS + ";";


    public static void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_ITEMS_CREATE);
        addHeroItems(db);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (oldVersion < 2) {
            Log.i(ItemTable.class.getSimpleName(), "adding the green die to the database entries.");
            addGreenDieToMask(db);
        }

        if (oldVersion < 3) {
            Log.i(ItemTable.class.getSimpleName(), "adding a column for the icon-name.");
            addIconNameColumn(db);
        }

        if (oldVersion < 4) {
            Log.i(ItemTable.class.getSimpleName(), "adding hero items for standup and characteristics.");
            addHeroItems(db);
        }
    }

    private static void addHeroItems(SQLiteDatabase db) {
        String insertSql = "INSERT INTO " + TABLE_ITEMS + " (" + ItemColumn.ID.name + "," + ItemColumn.CATEGORY.name + "," +
                ItemColumn.SELECTED.name + "," + ItemColumn.NAME.name + "," + ItemColumn.DICE.name + "," + ItemColumn.ICON.name + ") ";
        db.execSQL(insertSql + "VALUES (1000001, 3, 0, '', '0000200', 'icon_hero_standup');");
        db.execSQL(insertSql + "VALUES (1000002, 3, 0, '', '1100000', 'icon_hero_characteristics');");
    }

    private static void addIconNameColumn(SQLiteDatabase db) {
        String sql = "ALTER TABLE " + TABLE_ITEMS + " ADD COLUMN " + ItemColumn.ICON.name + " TEXT;";
        db.execSQL(sql);
        sql = "UPDATE " + TABLE_ITEMS + " SET " + ItemColumn.ICON.name + " = 'icon_missing';";
        db.execSQL(sql);
    }

    private static void addGreenDieToMask(SQLiteDatabase db) {
        String sql = "UPDATE " + TABLE_ITEMS + " SET " + ItemColumn.DICE.name + " = " +
                ItemColumn.DICE.name + " || \"0\";";
        db.execSQL(sql);
    }
}
