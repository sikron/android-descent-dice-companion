package com.skronawi.DescentDiceCompanion.app.database;


import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.GridLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.skronawi.DescentDiceCompanion.R;
import com.skronawi.DescentDiceCompanion.app.dialogs.RollResultDialog;
import com.skronawi.DescentDiceCompanion.app.dice.DiceImageResolver;
import com.skronawi.DescentDiceCompanion.app.items.CategoryList;
import com.skronawi.DescentDiceCompanion.app.items.HeroList;
import com.skronawi.DescentDiceCompanion.app.tasks.AsyncRollingSingleTask;
import com.skronawi.DescentDiceCompanion.app.tasks.AsyncRollingTask;
import com.skronawi.DescentDiceCompanion.lib.dice.DiceThrow;
import com.skronawi.DescentDiceCompanion.lib.dice.DieThrow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class RollResultCursorAdapter extends CursorAdapter {

//    private final Map<Integer, DiceThrow> result;
    private final RollResultDialog rollResultDialog;
    private LayoutInflater inflater;
    private final Activity activity;

    public RollResultCursorAdapter(Cursor c, Activity activity, //Map<Integer, DiceThrow> result,
                                   RollResultDialog rollResultDialog) {
        super(activity.getApplicationContext(), c);
        this.activity = activity;
        //this.result = result;
        inflater = LayoutInflater.from(activity.getApplicationContext());
        this.rollResultDialog = rollResultDialog;
    }

    static class ViewHolder {
        protected TextView name;
        protected GridLayout dice;
        protected ImageView icon;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        View view;
        final ViewHolder viewHolder;

        view = inflater.inflate(R.layout.rollresultitem, parent, false);
        viewHolder = new ViewHolder();

        viewHolder.name = (TextView) view.findViewById(R.id.item_name);
        viewHolder.dice = (GridLayout) view.findViewById(R.id.item_dice);
        viewHolder.icon = (ImageView) view.findViewById(R.id.item_icon);
        view.setTag(viewHolder);

        return view;
    }



    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder holder = (ViewHolder) view.getTag();

        Integer id = cursor.getInt(ItemTable.ItemColumn.ID.getIndex());
        String name = cursor.getString(ItemTable.ItemColumn.NAME.getIndex());
        String iconName = cursor.getString(ItemTable.ItemColumn.ICON.getIndex());
        iconName = TextUtils.isEmpty(iconName) ? "icon_missing" : iconName;

        name = determineName(id, name);

        if (holder.name != null) {
            holder.name.setText(name);
        }
        if (holder.dice != null) {
            List<ImageView> diceImages = convertToImages(rollResultDialog.getResult().get(id));
            setDiceImages(holder.dice, diceImages);
        }
        if (holder.icon != null) {
            int iconResourceId = context.getResources().getIdentifier(iconName,
                    "drawable", context.getPackageName());
            Bitmap icon = BitmapFactory.decodeResource(context.getResources(), iconResourceId);
            holder.icon.setImageBitmap(icon);
        }
    }

    private String determineName(int id, String defaultName) {
        if (id == HeroList.STANDUP_ITEM_ID) {
            return rollResultDialog.getActivity().getResources().getString(R.string.icon_standup_name);
        } else if (id == HeroList.CHAR_ITEM_ID){
            return rollResultDialog.getActivity().getResources().getString(R.string.icon_characteristics_name);
        } else {
            return defaultName;
        }
    }

    protected void setDiceImages(GridLayout dice, List<ImageView> diceImages) {

        dice.removeAllViews();

        dice.setColumnCount(4);
        dice.setRowCount(2);

        int padding = activity.getResources().getDimensionPixelSize(R.dimen.die_padding) * 3;
        for (ImageView iv : diceImages) {
            iv.setPadding(padding, padding, padding, padding);
            dice.addView(iv);
        }
    }

    private List<ImageView> convertToImages(final DiceThrow diceThrow) {

        ArrayList<ImageView> images = new ArrayList<ImageView>(10);

        int idx = 0;
        for (final DieThrow dieThrow : diceThrow) {

            int dieImageId = DiceImageResolver.resolveDieX2ImageId(dieThrow.getDie().getType());
            int dieSideImageId = DiceImageResolver.resolveDieSideImageId(
                    dieThrow.getDie().getType(), dieThrow.getSideIdx());
            Bitmap bitmap = combine(dieImageId, dieSideImageId);
            ImageView iv = new ImageView(activity.getApplicationContext());
            iv.setImageBitmap(bitmap);
            images.add(iv);

            final int finalIdx = idx;
            iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AsyncRollingSingleTask(rollResultDialog.getResult(), diceThrow, finalIdx, rollResultDialog,
                            activity).execute("");
                }
            });

            idx++;
        }

        return images;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    private Bitmap combine(int lower, int upper) {

        Bitmap bm1 = null;
        Bitmap bm2 = null;
        Bitmap newBitmap = null;

        bm1 = BitmapFactory.decodeResource(activity.getResources(), lower);
        bm2 = BitmapFactory.decodeResource(activity.getResources(), upper);

        int w = bm1.getWidth();
        int h = bm1.getHeight();

        Bitmap.Config config = bm1.getConfig();
        if (config == null) {
            config = Bitmap.Config.ARGB_8888;
        }

        newBitmap = Bitmap.createBitmap(w, h, config);
        Canvas newCanvas = new Canvas(newBitmap);

        newCanvas.drawBitmap(bm1, 0, 0, null);

        Paint paint = new Paint();
        newCanvas.drawBitmap(bm2, 0, 0, paint);

        return newBitmap;
    }
}
