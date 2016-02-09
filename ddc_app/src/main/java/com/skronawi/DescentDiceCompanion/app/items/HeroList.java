package com.skronawi.DescentDiceCompanion.app.items;


import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.skronawi.DescentDiceCompanion.R;
import com.skronawi.DescentDiceCompanion.app.database.HeroAdapter;
import com.skronawi.DescentDiceCompanion.app.database.ItemContentProvider;
import com.skronawi.DescentDiceCompanion.app.database.ItemTable;
import com.skronawi.DescentDiceCompanion.app.probability.ProbabilityMatrixDialog;
import com.skronawi.DescentDiceCompanion.lib.dice.DiceThrow;

import java.util.HashMap;
import java.util.Map;

public class HeroList extends CategoryList {

    public static final String BYHEROLIST = "byherolist";
    public static final int STANDUP_ITEM_ID = 1000001;
    public static final int CHAR_ITEM_ID = 1000002;
    public static final String TAG = HeroList.class.getName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.hero, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        if (savedInstanceState == null) {
            savedInstanceState = new Bundle();
        }
        savedInstanceState.putBoolean(BYHEROLIST, true);
        super.onActivityCreated(savedInstanceState);

        setRetainInstance(true);

        setupCategoryResources();

        gridView = (GridView) view.findViewById(R.id.item_grid);
        setListCursor(gridView);

        setupViewForCategory();

        if (result != null) {
            setResult(result);
        }
    }

    protected void setupViewForCategory() {

        //done in xml
//        view.findViewById(R.id.icon_range).setVisibility(View.INVISIBLE);
//        view.findViewById(R.id.result_range).setVisibility(View.INVISIBLE);
    }

    protected void setupCategoryResources() {
        resources = new HashMap<String, Integer>();
        resources.put(HEADER, R.string.items_hero);
        resources.put(CATEGORY, ItemTable.CATEGORY_HERO);
        uriWithAdd = ItemContentProvider.ITEMS_HERO_URI;
        uri = uriWithAdd;
    }

    protected void setListCursor(GridView gridView) {

        itemAdapter = new HeroAdapter(getActivity().getApplicationContext(), null, this);

        ((TextView) view.findViewById(R.id.list_header)).setText(getResources().getString(
                resources.get(HEADER)));

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                Cursor cursor = getActivity().getContentResolver().query(Uri.parse(uri + "/" + id),
                        PROJECTION, null, null, null);
                cursor.moveToFirst();

                ContentValues values = new ContentValues();
                int currentlySelected = cursor.getInt(ItemTable.ItemColumn.SELECTED.getIndex());
                values.put(ItemTable.ItemColumn.SELECTED.getName(), currentlySelected == 1 ? 0 : 1);
                cursor.close();
                getActivity().getContentResolver().update(Uri.parse(uri + "/" + id), values, null, null);

                if (currentlySelected == 0) {
                    deselectOtherItems(id);
                }
            }

            private void deselectOtherItems(long itemId) {
                int itemToDeselect = STANDUP_ITEM_ID;
                if (itemId == STANDUP_ITEM_ID) {
                    itemToDeselect = CHAR_ITEM_ID;
                }
                Cursor cursor = getActivity().getContentResolver().query(Uri.parse(uri + "/" + itemToDeselect),
                        PROJECTION, null, null, null);
                cursor.moveToFirst();

                ContentValues values = new ContentValues();
                values.put(ItemTable.ItemColumn.SELECTED.getName(), 0);
                cursor.close();
                getActivity().getContentResolver().update(Uri.parse(uri + "/" + itemToDeselect), values, null, null);
            }
        });

        gridView.setAdapter(itemAdapter);
        updateListViaLoader();

        Button rollButton = (Button) view.findViewById(R.id.button_roll);
        rollButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.v(TAG, "rolled by button-click");
                roll();
            }
        });

        Button probabilityButton = (Button) view.findViewById(R.id.button_probability);
        probabilityButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.v(TAG, "probabilities by button-click");
                computeProbabilities();
            }
        });

        Button rollResultButton = (Button) view.findViewById(R.id.button_rollresult);
        rollResultButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.v(TAG, "roll result by button-click");
                rollResult();
            }
        });
    }

    private void determineResultView() {

        Cursor cursor = getActivity().getContentResolver().query(Uri.parse(uri + "/" + STANDUP_ITEM_ID),
                PROJECTION, null, null, null);
        cursor.moveToFirst();
        int standupSelected = cursor.getInt(ItemTable.ItemColumn.SELECTED.getIndex());
        cursor.close();

        //currently not needed as there are only two items
//        cursor = getActivity().getContentResolver().query(Uri.parse(uri + "/" + CHAR_ITEM_ID),
//                PROJECTION, null, null, null);
//        cursor.moveToFirst();
//        int characteristicsSelected = cursor.getInt(ItemTable.ItemColumn.SELECTED.getIndex());
//        cursor.close();

        if (standupSelected == 1) {
            view.findViewById(R.id.icon_surge).setVisibility(View.VISIBLE);
            view.findViewById(R.id.result_surges).setVisibility(View.VISIBLE);
            ((ImageView) view.findViewById(R.id.icon_damage)).setImageResource(R.drawable.heart);
        } else { //if (characteristicsSelected == 1) {
            view.findViewById(R.id.icon_surge).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.result_surges).setVisibility(View.INVISIBLE);
            ((ImageView) view.findViewById(R.id.icon_damage)).setImageResource(R.drawable.shield);
        }
    }

    @Override
    public void setResult(Map<Integer, DiceThrow> diceThrows) {

        determineResultView();

        super.setResult(diceThrows);
    }

    public void showProbabilityResult(Double[][] probabilities) {

        FragmentManager fm = getActivity().getSupportFragmentManager();

        if (fm.findFragmentByTag("pmd") == null) {

            Cursor cursor = getActivity().getContentResolver().query(Uri.parse(uri + "/" + STANDUP_ITEM_ID),
                    PROJECTION, null, null, null);
            cursor.moveToFirst();
            int standupSelected = cursor.getInt(ItemTable.ItemColumn.SELECTED.getIndex());
            cursor.close();

//            cursor = getActivity().getContentResolver().query(Uri.parse(uri + "/" + CHAR_ITEM_ID),
//                    PROJECTION, null, null, null);
//            cursor.moveToFirst();
//            int characteristicsSelected = cursor.getInt(ItemTable.ItemColumn.SELECTED.getIndex());
//            cursor.close();

            int icon;
            if (standupSelected == 1) {
                icon = R.drawable.heart;
            } else {
                icon = R.drawable.shield;
            }

            ProbabilityMatrixDialog pmd = new ProbabilityMatrixDialog(probabilities,
                    R.string.probabilities_dialog_title, icon);
            pmd.show(fm, "pmd");
        }
    }
}
