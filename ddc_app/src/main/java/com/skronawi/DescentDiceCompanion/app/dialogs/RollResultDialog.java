package com.skronawi.DescentDiceCompanion.app.dialogs;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.skronawi.DescentDiceCompanion.R;
import com.skronawi.DescentDiceCompanion.app.database.ItemContentProvider;
import com.skronawi.DescentDiceCompanion.app.database.ItemTable;
import com.skronawi.DescentDiceCompanion.app.database.RollResultCursorAdapter;
import com.skronawi.DescentDiceCompanion.app.icons.IconProvider;
import com.skronawi.DescentDiceCompanion.app.items.CategoryList;
import com.skronawi.DescentDiceCompanion.app.items.HeroList;
import com.skronawi.DescentDiceCompanion.app.items.RollResultReceiver;
import com.skronawi.DescentDiceCompanion.lib.dice.DiceProvider;
import com.skronawi.DescentDiceCompanion.lib.dice.DiceThrow;
import com.skronawi.DescentDiceCompanion.lib.dice.DieInfo;
import com.skronawi.DescentDiceCompanion.lib.dice.DieThrow;

import java.util.ArrayList;
import java.util.Map;

/*
v- cursoradapter um die items zu bekommen
v- linearlayout zum befüllen (normal und large) ODER listview ?
v- result und item per itemId "verbinden"
- layout für listen-zeile
    - links, bzw oben:´bild über namen
    - rechts, bzw drunter: zweizeilig jeweils 4 würfel
- auf jedem würfel ein click-listener
- wenn geclickt -> busy-dialog, dann die-side ändern gemäss würfel-ergebnis
- ausserdem das gesamt-ergebnis in der categoryList ändern
- ganz oben im dialog nochmal die akkumulierte ansicht
 */

@SuppressLint("ValidFragment")
public class RollResultDialog extends DialogFragment implements LoaderManager.LoaderCallbacks<Cursor>,
        RollResultReceiver{

    protected static final String[] PROJECTION;

    static {
        PROJECTION = new String[]{
                ItemTable.ItemColumn.ID.getName(),
                ItemTable.ItemColumn.CATEGORY.getName(),
                ItemTable.ItemColumn.SELECTED.getName(),
                ItemTable.ItemColumn.NAME.getName(),
                ItemTable.ItemColumn.DICE.getName(),
                ItemTable.ItemColumn.ICON.getName()};
    }

    private Map<Integer, DiceThrow> result;
    private final int titleId;
    private final CategoryList list;
    private final Integer category;
    private RollResultCursorAdapter itemAdapter;
    private ListView listView;
    private TextView rangeResultTextView;
    private TextView surgesResultTextView;
    private TextView pointsResultTextView;
    private Uri uri;
    private ProgressDialog rollingDialog;
    private int headerId;

    public RollResultDialog(Map<Integer, DiceThrow> result, int titleId, CategoryList categoryList, Integer category) {
        this.result = result;
        this.titleId = titleId;
        this.list = categoryList;
        this.category = category;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.rollresultdialog, container);
        getDialog().setTitle(titleId);

        rangeResultTextView = (TextView) view.findViewById(R.id.result_range);
        surgesResultTextView = (TextView) view.findViewById(R.id.result_surges);
        pointsResultTextView = (TextView) view.findViewById(R.id.result_damage);

        setupCategoryResources();

        setupView(view);

        final Button cancelButton = (Button) view.findViewById(R.id.button_ok);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                dismiss();
            }
        });

//        setResult(result);

        String[] accDiceThrows = DiceThrow.accumulate(result.values());
        setResult(accDiceThrows[0], accDiceThrows[1], accDiceThrows[2]);
        itemAdapter = new RollResultCursorAdapter(null, list.getActivity(), this);
        listView = (ListView) view.findViewById(R.id.list);
        listView.setAdapter(itemAdapter);
        getLoaderManager().initLoader(0, null, this);

        return view;
    }

    public Map<Integer, DiceThrow> getResult(){
        return result;
    }

    private void setupCategoryResources() {
        if (category == ItemTable.CATEGORY_ATTACK) {
            uri = ItemContentProvider.ITEMS_ATTACK_URI;
            headerId = R.string.items_attack;
        } else if (category == ItemTable.CATEGORY_DEFENSE) {
            uri = ItemContentProvider.ITEMS_DEFENSE_URI;
            headerId = R.string.items_defense;
        } else {
            uri = ItemContentProvider.ITEMS_HERO_URI;
            headerId = R.string.items_hero;
        }
    }

    private void setupView(View view) {

        if (category == ItemTable.CATEGORY_DEFENSE) {

            view.findViewById(R.id.icon_range).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.result_range).setVisibility(View.INVISIBLE);

            view.findViewById(R.id.icon_surge).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.result_surges).setVisibility(View.INVISIBLE);

            ((ImageView) view.findViewById(R.id.icon_damage)).setImageResource(R.drawable.shield);

        } else if (category == ItemTable.CATEGORY_HERO){

            view.findViewById(R.id.icon_range).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.result_range).setVisibility(View.INVISIBLE);

            //determine, whether the result is for a attribute check or stand up (contains red dice)
            boolean containsRedDie = false;
            for (DiceThrow diceThrow : result.values()){
                for (DieThrow dieThrow : diceThrow){
                    if (dieThrow.getDie().getType() == DieInfo.RED){
                        containsRedDie = true;
                    }
                }
            }

            if (containsRedDie) {
                view.findViewById(R.id.icon_surge).setVisibility(View.VISIBLE);
                view.findViewById(R.id.result_surges).setVisibility(View.VISIBLE);
                ((ImageView) view.findViewById(R.id.icon_damage)).setImageResource(R.drawable.heart);
            } else {
                view.findViewById(R.id.icon_surge).setVisibility(View.INVISIBLE);
                view.findViewById(R.id.result_surges).setVisibility(View.INVISIBLE);
                ((ImageView) view.findViewById(R.id.icon_damage)).setImageResource(R.drawable.shield);
            }
        }
    }

    @Override
    public void showRollingBusyDialog(boolean doShow, String dialogRandomnessString) {

        if (doShow) {
            rollingDialog = ProgressDialog.show(getActivity(), "",
                    list.getActivity().getResources().getString(R.string.roll_dialog) + " : " +
                            list.getActivity().getResources().getString(headerId) + "\n" +
                            dialogRandomnessString);
        } else {
            if (rollingDialog != null) {
                rollingDialog.dismiss();
            }
        }
    }

    public void setResult(Map<Integer, DiceThrow> diceThrows) {

        result = diceThrows;

        String[] accDiceThrows = DiceThrow.accumulate(diceThrows.values());
        setResult(accDiceThrows[0], accDiceThrows[1], accDiceThrows[2]);

        //when called by the AsyncRollingSingleTask
        itemAdapter = new RollResultCursorAdapter(null, list.getActivity(), this);
        listView.setAdapter(itemAdapter);
        getLoaderManager().initLoader(0, null, this);
        list.setResult(diceThrows);
    }

    @Override
    public void clearResult() {
        throw new IllegalStateException("clearResult on a RollResultDialog should not have been called");
    }

    @Override
    public void showToast(int resourceId, int length) {
        Toast.makeText(list.getActivity().getApplicationContext(),
                getResources().getString(resourceId), length).show();
    }

    @Override
    public void setRolling(boolean isRolling) {
        //rolling = isRolling;
    }

    @Override
    public void rollResult() {
        //don't open the roll-result dialog, as we are currently in the dialog
    }

    public void setResult(String rangeResult, String surgesResult, String pointsResult) {
        rangeResultTextView.setText(rangeResult);
        surgesResultTextView.setText(surgesResult);
        pointsResultTextView.setText(pointsResult);
    }

    // Creates a new loader after the initLoader () call
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String placeholders = createPlaceholders(result);
        String[] ids = createIds(result);
        CursorLoader cursorLoader = new CursorLoader(getActivity(),
                uri, PROJECTION, "_id in ("+placeholders+")", ids, ItemTable.ItemColumn.ID.getName() + " ASC");
        return cursorLoader;
    }

    private String[] createIds(Map<Integer, DiceThrow> result) {
        ArrayList<String> ids = new ArrayList<String>(result.keySet().size());
        for (Integer id : result.keySet()){
            ids.add(String.valueOf(id));
        }
        return ids.toArray(new String[ids.size()]);
    }

    private String createPlaceholders(Map<Integer, DiceThrow> result) {
        String s = "";
        for (Integer i : result.keySet()){
            s += "?,";
        }
        return s.substring(0, s.length() - 1);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        ((RollResultCursorAdapter) listView.getAdapter()).swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // data is not available anymore, delete reference
        ((RollResultCursorAdapter) listView.getAdapter()).swapCursor(null);
    }
}
