package com.skronawi.DescentDiceCompanion.app.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class ItemContentProvider extends ContentProvider {

    private SQLiteOpenHelper helper;

    // Used for the UriMacher
    private static final int ITEMS_ATTACK = 11;
    private static final int ITEMS_DEFENSE = 12;
    private static final int ITEMS_HERO = 13;
    private static final int ITEMS_ATTACK_ADD = 14;
    private static final int ITEMS_DEFENSE_ADD = 15;
    private static final int ITEM_ATTACK_ID = 21;
    private static final int ITEM_DEFENSE_ID = 22;
    private static final int ITEM_HERO_ID = 23;

    private static final String AUTHORITY = "com.skronawi.DescentDiceCompanion.itemContentProvider";
    private static final String BASE_PATH = "items";
    private static final String ADD_SUFFIX = "withaddplaceholder";

    public static final Uri ITEMS_ATTACK_INCL_ADD_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH + ADD_SUFFIX + "/"
            + ItemTable.CATEGORY_ATTACK);
    public static final Uri ITEMS_DEFENSE_INCL_ADD_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH + ADD_SUFFIX + "/"
            + ItemTable.CATEGORY_DEFENSE);

    public static final Uri ITEMS_ATTACK_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH + "/" +
            ItemTable.CATEGORY_ATTACK);
    public static final Uri ITEMS_DEFENSE_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH + "/" +
            ItemTable.CATEGORY_DEFENSE);

    public static final Uri ITEMS_HERO_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH + "/" +
            ItemTable.CATEGORY_HERO);

    public static final String SINGLE_RECORD_MIME_TYPE = "vnd.android.cursor.item/vnd.com.skronawi.DescentDiceCompanion.item";
    public static final String MULTIPLE_RECORDS_MIME_TYPE = "vnd.android.cursor.dir/vnd.com.skronawi.DescentDiceCompanion.item";

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + ADD_SUFFIX + "/" + ItemTable.CATEGORY_ATTACK, ITEMS_ATTACK_ADD);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + ADD_SUFFIX + "/" + ItemTable.CATEGORY_DEFENSE, ITEMS_DEFENSE_ADD);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/" + ItemTable.CATEGORY_ATTACK, ITEMS_ATTACK);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/" + ItemTable.CATEGORY_DEFENSE, ITEMS_DEFENSE);

        sURIMatcher.addURI(AUTHORITY, BASE_PATH + ADD_SUFFIX + "/" + ItemTable.CATEGORY_ATTACK + "/#", ITEM_ATTACK_ID);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + ADD_SUFFIX + "/" + ItemTable.CATEGORY_DEFENSE + "/#", ITEM_DEFENSE_ID);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/" + ItemTable.CATEGORY_ATTACK + "/#", ITEM_ATTACK_ID);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/" + ItemTable.CATEGORY_DEFENSE + "/#", ITEM_DEFENSE_ID);

        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/" + ItemTable.CATEGORY_HERO, ITEMS_HERO);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/" + ItemTable.CATEGORY_HERO + "/#", ITEM_HERO_ID);
    }

    @Override
    public boolean onCreate() {
        helper = new SQLiteOpenHelper(getContext(), ItemTable.DATABASE_NAME, null, ItemTable.DATABASE_VERSION) {

            @Override
            public void onCreate(SQLiteDatabase db) {
                ItemTable.onCreate(db);
            }

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                ItemTable.onUpgrade(db, oldVersion, newVersion);
            }
        };
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        queryBuilder.setTables(ItemTable.TABLE_ITEMS);

        String query;
        int uriType = sURIMatcher.match(uri);

        switch (uriType) {
            case ITEMS_ATTACK:
                queryBuilder.appendWhere(ItemTable.ItemColumn.CATEGORY.getName() + "=" + ItemTable.CATEGORY_ATTACK);
                query = queryBuilder.buildQuery(projection, selection, null, null, null, sortOrder, null);
                break;
            case ITEMS_DEFENSE:
                queryBuilder.appendWhere(ItemTable.ItemColumn.CATEGORY.getName() + "=" + ItemTable.CATEGORY_DEFENSE);
                query = queryBuilder.buildQuery(projection, selection, null, null, null, sortOrder, null);
                break;
            case ITEMS_ATTACK_ADD:
                queryBuilder.appendWhere(ItemTable.ItemColumn.CATEGORY.getName() + "=" + ItemTable.CATEGORY_ATTACK);
                query = queryBuilder.buildQuery(projection, selection, null, null, null, null, null);
                query = queryBuilder.buildUnionQuery(new String[]{query, "SELECT " + Integer.MAX_VALUE + ",0,0,'','',''"}, sortOrder, null);
                break;
            case ITEMS_DEFENSE_ADD:
                queryBuilder.appendWhere(ItemTable.ItemColumn.CATEGORY.getName() + "=" + ItemTable.CATEGORY_DEFENSE);
                query = queryBuilder.buildQuery(projection, selection, null, null, null, null, null);
                query = queryBuilder.buildUnionQuery(new String[]{query, "SELECT " + Integer.MAX_VALUE + ",0,0,'','',''"}, sortOrder, null);
                break;
            case ITEMS_HERO:
                queryBuilder.appendWhere(ItemTable.ItemColumn.CATEGORY.getName() + "=" + ItemTable.CATEGORY_HERO);
                query = queryBuilder.buildQuery(projection, selection, null, null, null, null, null);
                break;
            case ITEM_ATTACK_ID:
                queryBuilder.appendWhere(ItemTable.ItemColumn.CATEGORY.getName() + "=" + ItemTable.CATEGORY_ATTACK + " AND " +
                        ItemTable.ItemColumn.ID.getName() + "=" + uri.getLastPathSegment());
                query = queryBuilder.buildQuery(projection, selection, null, null, sortOrder, null, null);
                break;
            case ITEM_DEFENSE_ID:
                queryBuilder.appendWhere(ItemTable.ItemColumn.CATEGORY.getName() + "=" + ItemTable.CATEGORY_DEFENSE + " AND " +
                        ItemTable.ItemColumn.ID.getName() + "=" + uri.getLastPathSegment());
                query = queryBuilder.buildQuery(projection, selection, null, null, sortOrder, null, null);
                break;
            case ITEM_HERO_ID:
                queryBuilder.appendWhere(ItemTable.ItemColumn.CATEGORY.getName() + "=" + ItemTable.CATEGORY_HERO + " AND " +
                        ItemTable.ItemColumn.ID.getName() + "=" + uri.getLastPathSegment());
                query = queryBuilder.buildQuery(projection, selection, null, null, sortOrder, null, null);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.rawQuery(query + ";", selectionArgs);
//		Cursor cursor = queryBuilder.query(db, PROJECTION, selection,
//				selectionArgs, null, null, sortOrder);

        // Make sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {

        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case ITEMS_ATTACK:
            case ITEMS_DEFENSE:
            case ITEMS_ATTACK_ADD:
            case ITEMS_DEFENSE_ADD:
            case ITEMS_HERO:
                return MULTIPLE_RECORDS_MIME_TYPE;
            case ITEM_ATTACK_ID:
            case ITEM_DEFENSE_ID:
            case ITEM_HERO_ID:
                return SINGLE_RECORD_MIME_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = helper.getWritableDatabase();
        long id = 0;
        int category = 0;
        switch (uriType) {
            case ITEMS_ATTACK:
            case ITEMS_ATTACK_ADD:
                if (!values.containsKey(ItemTable.ItemColumn.CATEGORY.getName())) {
                    values.put(ItemTable.ItemColumn.CATEGORY.getName(), ItemTable.CATEGORY_ATTACK);
                }
                category = ItemTable.CATEGORY_ATTACK;
                break;
            case ITEMS_DEFENSE:
            case ITEMS_DEFENSE_ADD:
                if (!values.containsKey(ItemTable.ItemColumn.CATEGORY.getName())) {
                    values.put(ItemTable.ItemColumn.CATEGORY.getName(), ItemTable.CATEGORY_DEFENSE);
                }
                category = ItemTable.CATEGORY_DEFENSE;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        id = sqlDB.insert(ItemTable.TABLE_ITEMS, null, values);
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(BASE_PATH + "/" + category + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = helper.getWritableDatabase();
        int rowsDeleted = 0;
        String id = null;

        switch (uriType) {
            case ITEMS_ATTACK:
            case ITEMS_ATTACK_ADD:
                rowsDeleted = sqlDB.delete(ItemTable.TABLE_ITEMS,
                        ItemTable.ItemColumn.CATEGORY.getName() + " = " + ItemTable.CATEGORY_ATTACK + " and " + selection,
                        selectionArgs);
                break;
            case ITEMS_DEFENSE:
            case ITEMS_DEFENSE_ADD:
                rowsDeleted = sqlDB.delete(ItemTable.TABLE_ITEMS,
                        ItemTable.ItemColumn.CATEGORY.getName() + " = " + ItemTable.CATEGORY_DEFENSE + " and " + selection,
                        selectionArgs);
                break;
            case ITEM_ATTACK_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(ItemTable.TABLE_ITEMS,
                            ItemTable.ItemColumn.CATEGORY.getName() + " = " + ItemTable.CATEGORY_ATTACK + " and " +
                                    ItemTable.ItemColumn.ID.getName() + "=" + id,
                            null);
                } else {
                    rowsDeleted = sqlDB.delete(ItemTable.TABLE_ITEMS,
                            ItemTable.ItemColumn.CATEGORY.getName() + " = " + ItemTable.CATEGORY_ATTACK + " and " +
                                    ItemTable.ItemColumn.ID.getName() + "=" + id + " and " + selection,
                            selectionArgs);
                }
                break;
            case ITEM_DEFENSE_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(ItemTable.TABLE_ITEMS,
                            ItemTable.ItemColumn.CATEGORY.getName() + " = " + ItemTable.CATEGORY_DEFENSE + " and " +
                                    ItemTable.ItemColumn.ID.getName() + "=" + id,
                            null);
                } else {
                    rowsDeleted = sqlDB.delete(ItemTable.TABLE_ITEMS,
                            ItemTable.ItemColumn.CATEGORY.getName() + " = " + ItemTable.CATEGORY_DEFENSE + " and " +
                                    ItemTable.ItemColumn.ID.getName() + "=" + id + " and " + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = helper.getWritableDatabase();
        int rowsUpdated = 0;
        String id = null;
        switch (uriType) {
            case ITEMS_ATTACK:
            case ITEMS_DEFENSE:
            case ITEMS_ATTACK_ADD:
            case ITEMS_DEFENSE_ADD:
            case ITEMS_HERO:
                rowsUpdated = sqlDB.update(ItemTable.TABLE_ITEMS,
                        values,
                        selection,
                        selectionArgs);
                break;
            case ITEM_ATTACK_ID:
            case ITEM_DEFENSE_ID:
            case ITEM_HERO_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqlDB.update(ItemTable.TABLE_ITEMS,
                            values,
                            ItemTable.ItemColumn.ID.getName() + "=" + id,
                            null);
                } else {
                    rowsUpdated = sqlDB.update(ItemTable.TABLE_ITEMS,
                            values,
                            ItemTable.ItemColumn.ID.getName() + "=" + id + " and " + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

}
