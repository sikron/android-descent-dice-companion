package com.skronawi.DescentDiceCompanion.app.database;


import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.GridLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.skronawi.DescentDiceCompanion.R;
import com.skronawi.DescentDiceCompanion.app.items.CategoryList;
import com.skronawi.DescentDiceCompanion.lib.dice.Die;
import com.skronawi.DescentDiceCompanion.lib.dice.DieInfo;
import com.skronawi.DescentDiceCompanion.lib.dice.ItemDice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ItemCursorAdapter extends CursorAdapter {

    public static final int SELECTED = Color.parseColor("#277693");
    public static final int NOT_SELECTED = Color.TRANSPARENT;
    /*
    those must be smaller than getViewTypeCount:
    http://stackoverflow.com/questions/2596547/arrayindexoutofboundsexception-with-custom-android-adapter-for-multiple-views-in/2597318#2597318
     */
    public static final int ITEM_ADD = 1;
    public static final int ITEM_NORMAL = 0;

    private LayoutInflater inflater;
    private HashMap<DieInfo, Integer> diceIdMap;
    protected final Context context;
    private final CategoryList list;

    public ItemCursorAdapter(Context context, Cursor c, CategoryList list) {
        super(context, c);
        this.context = context;
        this.list = list;
        inflater = LayoutInflater.from(context);
    }

    static class ViewHolder {
        protected TextView name;
        protected GridLayout dice;
        protected ImageView icon;
        protected RelativeLayout background;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        View view;
        final ViewHolder viewHolder;

        if (getItemViewType(cursor.getPosition()) == ITEM_ADD) {
            view = inflater.inflate(R.layout.item_add, parent, false);
            viewHolder = new ViewHolder();

        } else {
            view = inflater.inflate(R.layout.item, parent, false);
            viewHolder = new ViewHolder();

            viewHolder.name = (TextView) view.findViewById(R.id.item_name);
            viewHolder.dice = (GridLayout) view.findViewById(R.id.item_dice);
            viewHolder.icon = (ImageView) view.findViewById(R.id.item_icon);
            viewHolder.background = (RelativeLayout) view;

        }
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        if (getItemViewType(cursor.getPosition()) == ITEM_NORMAL) {
            ViewHolder holder = (ViewHolder) view.getTag();

            String dice = cursor.getString(ItemTable.ItemColumn.DICE.getIndex());
            String name = cursor.getString(ItemTable.ItemColumn.NAME.getIndex());
            int selected = cursor.getInt(ItemTable.ItemColumn.SELECTED.getIndex());
            String iconName = cursor.getString(ItemTable.ItemColumn.ICON.getIndex());
            iconName = TextUtils.isEmpty(iconName) ? "icon_missing" : iconName;

            if (holder.name != null) {
                holder.name.setText(name);
            }
            if (holder.dice != null) {
                List<ImageView> diceImages = convertToImages(dice);
                setDiceImages(holder.dice, diceImages);
            }
            if (selected == 1) {
                holder.background.setBackgroundColor(SELECTED);
            } else {
                holder.background.setBackgroundColor(NOT_SELECTED);
            }
            if (holder.icon != null) {
                int iconResourceId = context.getResources().getIdentifier(iconName,
                        "drawable", context.getPackageName());
                Bitmap icon = BitmapFactory.decodeResource(context.getResources(), iconResourceId);
                holder.icon.setImageBitmap(icon);
            }
        }
    }

    protected List<ImageView> convertToImages(String mask) {
        return convertToImages(ItemDice.fromMask(mask));
    }

    protected void setDiceImages(GridLayout dice, List<ImageView> diceImages) {

        dice.removeAllViews();

        dice.setColumnCount(4);
        dice.setRowCount(2);

        int padding = list.getResources().getDimensionPixelSize(R.dimen.die_padding);
        for (ImageView iv : diceImages) {
            iv.setPadding(padding, padding, padding, padding);
            dice.addView(iv);
        }
    }

    private List<ImageView> convertToImages(ItemDice dice) {

        ArrayList<ImageView> images = new ArrayList<ImageView>(10);

        for (Die die : dice) {
            int dieImageId = getDieImageId(die.getType());
            ImageView iv = new ImageView(context);
            iv.setImageResource(dieImageId);
            images.add(iv);
        }
        return images;
    }

    private int getDieImageId(DieInfo dieInfo) {

        if (diceIdMap == null) {
            diceIdMap = new HashMap<DieInfo, Integer>(DieInfo.values().length);
            diceIdMap.put(DieInfo.BLACK, R.drawable.die_black);
            diceIdMap.put(DieInfo.BROWN, R.drawable.die_brown);
            diceIdMap.put(DieInfo.GREY, R.drawable.die_grey);
            diceIdMap.put(DieInfo.BLUE, R.drawable.die_blue);
            diceIdMap.put(DieInfo.RED, R.drawable.die_red);
            diceIdMap.put(DieInfo.YELLOW, R.drawable.die_yellow);
            diceIdMap.put(DieInfo.GREEN, R.drawable.die_green);
        }
        return diceIdMap.get(dieInfo);
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        int count = getCount();
        int type = 0;
        if (position == count - 1) {
            type = ITEM_ADD;
        } else {
            type = ITEM_NORMAL;
        }
        return type;
    }
}
