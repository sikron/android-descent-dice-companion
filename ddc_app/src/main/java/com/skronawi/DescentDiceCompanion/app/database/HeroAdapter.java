package com.skronawi.DescentDiceCompanion.app.database;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.View;
import com.skronawi.DescentDiceCompanion.R;
import com.skronawi.DescentDiceCompanion.app.items.CategoryList;
import com.skronawi.DescentDiceCompanion.app.items.HeroList;

public class HeroAdapter extends ItemCursorAdapter {


    public HeroAdapter(Context context, Cursor c, CategoryList list) {
        super(context, c, list);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder holder = (ViewHolder) view.getTag();

        int id = cursor.getInt(ItemTable.ItemColumn.ID.getIndex());
        String name = getName(id);

        String dice = cursor.getString(ItemTable.ItemColumn.DICE.getIndex());
        int selected = cursor.getInt(ItemTable.ItemColumn.SELECTED.getIndex());
        String iconName = cursor.getString(ItemTable.ItemColumn.ICON.getIndex());
        iconName = TextUtils.isEmpty(iconName) ? "icon_missing" : iconName;

        if (holder.name != null) {
            holder.name.setText(name);
        }
        if (holder.dice != null) {
            setDiceImages(holder.dice, convertToImages(dice));
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

    private String getName(int id) {
        if (id == HeroList.STANDUP_ITEM_ID) {
            return context.getResources().getString(R.string.icon_standup_name);
        } else {
            return context.getResources().getString(R.string.icon_characteristics_name);
        }
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public int getItemViewType(int position) {
        return ITEM_NORMAL;
    }
}
