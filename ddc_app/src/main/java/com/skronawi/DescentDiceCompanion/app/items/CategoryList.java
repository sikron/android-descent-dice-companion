package com.skronawi.DescentDiceCompanion.app.items;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import com.skronawi.DescentDiceCompanion.R;
import com.skronawi.DescentDiceCompanion.app.database.IconItem;
import com.skronawi.DescentDiceCompanion.app.database.ItemContentProvider;
import com.skronawi.DescentDiceCompanion.app.database.ItemCursorAdapter;
import com.skronawi.DescentDiceCompanion.app.database.ItemTable;
import com.skronawi.DescentDiceCompanion.app.database.ItemTable.ItemColumn;
import com.skronawi.DescentDiceCompanion.app.dialogs.EditItemDialog;
import com.skronawi.DescentDiceCompanion.app.dialogs.RollResultDialog;
import com.skronawi.DescentDiceCompanion.app.icons.Icon;
import com.skronawi.DescentDiceCompanion.app.icons.IconProvider;
import com.skronawi.DescentDiceCompanion.app.probability.ProbabilityMatrixDialog;
import com.skronawi.DescentDiceCompanion.app.tasks.AsyncProbabilityTask;
import com.skronawi.DescentDiceCompanion.app.tasks.AsyncRollingTask;
import com.skronawi.DescentDiceCompanion.lib.dice.DiceProvider;
import com.skronawi.DescentDiceCompanion.lib.dice.DiceThrow;
import com.skronawi.DescentDiceCompanion.lib.dice.Die;
import com.skronawi.DescentDiceCompanion.lib.dice.ItemDice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CategoryList extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
RollResultReceiver {

    protected static final String CATEGORY = "category";
    protected static final String HEADER = "header";
    public static final String TAG = CategoryList.class.getSimpleName();

    protected ItemCursorAdapter itemAdapter;
    protected GridView gridView;

    protected boolean rolling;

    protected HashMap<String, Integer> resources;
    protected View view;
    protected List<Die> dice;
    protected List<Icon> icons;
    protected ProgressDialog rollingDialog;
    protected ProgressDialog probabilityDialog;
    protected Map<Integer, DiceThrow> result;
    protected Uri uriWithAdd;
    protected Uri uri;
    protected boolean computingProbability;
    protected static final String[] PROJECTION;

    static {
        PROJECTION = new String[]{
                ItemColumn.ID.getName(),
                ItemColumn.CATEGORY.getName(),
                ItemColumn.SELECTED.getName(),
                ItemColumn.NAME.getName(),
                ItemColumn.DICE.getName(),
                ItemColumn.ICON.getName()};
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.category, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null &&
                !savedInstanceState.containsKey(HeroList.BYHEROLIST) ||
                savedInstanceState == null) {

            //setRetainInstance auf true, ansonsten gibts ein problem, wenn die async-task laeuft und die
            //orientation geaendert wird. dann kann es sein, dass die asynctask auf ein fragment verweist, das
            //gar nicht mehr existiert, weil in der zwischenzeit die gui re-created wurde.
            setRetainInstance(true);

            setupCategoryResources();

            gridView = (GridView) view.findViewById(R.id.item_grid);
            setListCursor(gridView);

            registerForContextMenu(gridView);

            setupViewForCategory();

            //maybe to @Override:onviewstaterestored
            if (result != null) {
                setResult(result);
            }

//            try {
//                Thread.sleep(3000);
//            } catch (InterruptedException e) {
//            }
//            setupDemoItems(10, true);
        }
    }

//    private void setupDemoItems(int number, boolean deleteExisting) {
//
//        if (deleteExisting) {
//            Cursor c = getActivity().getContentResolver().query(uri,
//                    PROJECTION, null, null, null);
//            while (c.moveToNext()) {
//                getActivity().getContentResolver().delete(Uri.parse(uri + "/" + c.getInt(ItemTable.ItemColumn.ID.getIndex())),
//                        null, null);
//            }
//            c.close();
//        }
//        for (int i = 0; i < number; i++) {
//            ContentValues values = new ContentValues();
//            values.put(ItemColumn.SELECTED.getName(), i % 2 == 0);
//            values.put(ItemColumn.NAME.getName(), "item " + i);
//            values.put(ItemColumn.DICE.getName(), toMask(new int[]{
//                    Math.min(i % 1, 1),
//                    Math.min(i % 2, 1),
//                    Math.min(i % 3, 1),
//                    Math.min(i % 4, 1),
//                    Math.min(i % 5, 1),
//                    Math.min(i % 6, 1),
//                    Math.min(i % 7, 1)}));
//            getActivity().getContentResolver().insert(uri, values);
//        }
//        updateListViaLoader();
//    }

    protected void setupViewForCategory() {

        if (resources.get(CATEGORY) == ItemTable.CATEGORY_DEFENSE) {

            view.findViewById(R.id.icon_range).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.result_range).setVisibility(View.INVISIBLE);

            view.findViewById(R.id.icon_surge).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.result_surges).setVisibility(View.INVISIBLE);

            ((ImageView) view.findViewById(R.id.icon_damage)).setImageResource(R.drawable.shield);

            //for tablets
            ImageView headerIcon = ((ImageView) view.findViewById(R.id.header_icon));
            if (headerIcon != null) {
                headerIcon.setImageResource(R.drawable.defense_selected);
            }
        }
    }

    protected void setupCategoryResources() {

        resources = new HashMap<String, Integer>();

        FragmentManager fm = getActivity().getSupportFragmentManager();

        Fragment categoryAttack = fm.findFragmentByTag(getResources().getString(
                R.string.items_attack));
        Fragment categoryDefense = fm.findFragmentByTag(getResources().getString(
                R.string.items_defense));

        if (this == categoryAttack) {
            resources.put(HEADER, R.string.items_attack);
            resources.put(CATEGORY, ItemTable.CATEGORY_ATTACK);
            dice = DiceProvider.getInstance().getAttackDice();
            uriWithAdd = ItemContentProvider.ITEMS_ATTACK_INCL_ADD_URI;
            uri = ItemContentProvider.ITEMS_ATTACK_URI;
            icons = IconProvider.getAttackIcons();

        } else if (this == categoryDefense) {
            resources.put(HEADER, R.string.items_defense);
            resources.put(CATEGORY, ItemTable.CATEGORY_DEFENSE);
            dice = DiceProvider.getInstance().getDefenseDice();
            uriWithAdd = ItemContentProvider.ITEMS_DEFENSE_INCL_ADD_URI;
            uri = ItemContentProvider.ITEMS_DEFENSE_URI;
            icons = IconProvider.getDefenseIcons();
        } else {
            throw new IllegalStateException("could not determine category of fragment. " +
                    "this.class = " + TAG);
        }
    }

    public void setResult(Map<Integer, DiceThrow> diceThrows) {

        result = diceThrows;

        String[] accDiceThrows = DiceThrow.accumulate(diceThrows.values());

		/*
         * do not save the textviews, as view can be recreated
		 */
        setResult(accDiceThrows[0], accDiceThrows[1], accDiceThrows[2]);
    }

    public void clearResult(){
        result = null;
        setResult("", "", "");
    }

    private void setResult(String rangeResult, String surgesResult, String pointsResult) {
        ((TextView) view.findViewById(R.id.result_range)).setText(rangeResult);
        ((TextView) view.findViewById(R.id.result_surges)).setText(surgesResult);
        ((TextView) view.findViewById(R.id.result_damage)).setText(pointsResult);
    }

    protected void setListCursor(GridView gridView) {

        itemAdapter = new ItemCursorAdapter(getActivity().getApplicationContext(), null, this);

        ((TextView) view.findViewById(R.id.list_header)).setText(getResources().getString(
                resources.get(HEADER)));

        gridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if (id == Integer.MAX_VALUE) {
                    IconItem item = new IconItem("", ItemTable.NEW_ITEM_ID,
                            PreferenceManager.getDefaultSharedPreferences(
                                    getActivity().getApplicationContext()).getBoolean(
                                    "newitemselected_preference", true));
                    showEditDialog(item, R.string.edit_item_dialog_title_new);

                } else {

                    Cursor cursor = getActivity().getContentResolver().query(Uri.parse(uriWithAdd + "/" + id),
                            PROJECTION, null, null, null);
                    cursor.moveToFirst();

                    ContentValues values = new ContentValues();
                    values.put(ItemColumn.SELECTED.getName(), cursor.getInt(
                            ItemColumn.SELECTED.getIndex()) == 1 ? 0 : 1);
                    cursor.close();
                    getActivity().getContentResolver().update(Uri.parse(uriWithAdd + "/" + id), values, null, null);
                }
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

    protected void rollResult() {
        if (result == null) {
            showToast(R.string.no_roll_result, Toast.LENGTH_SHORT);
        } else {
            FragmentManager fm = getActivity().getSupportFragmentManager();
            if (fm.findFragmentByTag("rrd") == null) {
                RollResultDialog rrd = new RollResultDialog(result, R.string.roll_result_dialog_title, this,
                        resources.get(CATEGORY));
                rrd.show(fm, "rrd");
            }
        }
    }

    public void roll() {
        if (!isBusy()) {
            rolling = true;
            Cursor c = getActivity().getContentResolver().query(uriWithAdd, PROJECTION, null, null, null);
            new AsyncRollingTask(c, CategoryList.this, getActivity()).execute("");
        }
    }

    public void updateListViaLoader() {
        getLoaderManager().initLoader(0, null, this);
    }

    private void showEditDialog(IconItem item, int titleId) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        if (fm.findFragmentByTag("eid") == null) {
            EditItemDialog eid = new EditItemDialog(item, titleId, this, dice, icons);
            eid.show(fm, "eid");
        }
    }

    public void showToast(int resourceId, int length) {
        Toast.makeText(getActivity().getApplicationContext(),
                getResources().getString(resourceId), length).show();
    }

    public void showToast(String text, int length) {
        Toast.makeText(getActivity().getApplicationContext(), text, length).show();
    }

    public void setRolling(boolean isRolling) {
        rolling = isRolling;
    }

    public void setItem(IconItem item) {

        long id = item.getId();

        ContentValues values = new ContentValues();
        values.put(ItemColumn.SELECTED.getName(), item.isSelected() ? 1 : 0);
        values.put(ItemColumn.NAME.getName(), item.getName());
        values.put(ItemColumn.DICE.getName(), item.getDice().toMask());
        values.put(ItemColumn.ICON.getName(), item.getIconName());

        if (id == ItemTable.NEW_ITEM_ID) {
            //item must have been a new one
            getActivity().getContentResolver().insert(uriWithAdd, values);

        } else {
            //already in list
            getActivity().getContentResolver().update(Uri.parse(uriWithAdd + "/" + id),
                    values, null, null);
        }
        updateListViaLoader();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) menuInfo;

        if (info.id != Integer.MAX_VALUE) {

            MenuInflater inflater = getActivity().getMenuInflater();
            //noinspection ResourceType
            inflater.inflate(R.layout.contextmenu, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo menuInfo =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

		/*
         * !!!!!!!!!!!!!
		 * 
		 * http://stackoverflow.com/questions/5297842/how-to-handle-oncontextitemselected-in-a-multi-fragment-activity
		 * 
		 * the context-menu-item for each fragment is asked!
		 * 
		 *  so the edit-query must determine if the queried id matches the current category-list-type.
		 *  only if it matches (attack-item handled by attack-category-list) then return true.
		 *  otherwise the EditItemDialog maybe gets the wrong "dice".
		 */

        // handle item selection
        switch (item.getItemId()) {

            case R.id.menu_delete:

                if (getActivity().getContentResolver().delete(Uri.parse(uriWithAdd + "/" + menuInfo.id),
                        null, null) > 0) {
                    updateListViaLoader();
                    return true;
                } else {
                    return false; //item NOT YET handled
                }

            case R.id.menu_edit:

                //edit on a copy and update real item later on OK

                Cursor c = getActivity().getContentResolver().query(Uri.parse(uriWithAdd + "/" + menuInfo.id),
                        PROJECTION, null, null, null);

                if (c.moveToFirst()) {

                    String name = c.getString(ItemColumn.NAME.getIndex());
                    String diceMask = c.getString(ItemColumn.DICE.getIndex());
                    int selected = c.getInt(ItemColumn.SELECTED.getIndex());
                    String iconName = c.getString(ItemColumn.ICON.getIndex());
                    c.close();

                    IconItem copy = new IconItem(name, menuInfo.id);
                    copy.setDice(ItemDice.fromMask(diceMask));
                    copy.setSelected(selected == 1);
                    copy.setIconName(iconName);

                    showEditDialog(copy, R.string.edit_item_dialog_title_edit);

                    return true; //item handled
                } else {
                    return false; //item NOT YET handled
                }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void showRollingBusyDialog(boolean doShow, String dialogRandomnessString) {

        if (doShow) {
            rollingDialog = ProgressDialog.show(getActivity(), "",
                    getActivity().getResources().getString(R.string.roll_dialog) + " : " +
                            getActivity().getResources().getString(resources.get(HEADER)) + "\n" +
                            dialogRandomnessString);
        } else {
            if (rollingDialog != null) {
                rollingDialog.dismiss();
            }
        }
    }

    public void showProbabilityBusyDialog(boolean doShow) {
        if (doShow) {
            probabilityDialog = ProgressDialog.show(getActivity(), "",
                    getActivity().getResources().getString(R.string.computing_probability));
        } else {
            if (probabilityDialog != null) {
                probabilityDialog.dismiss();
            }
        }
    }

    public void setComputingProbability(boolean isComputing) {
        computingProbability = isComputing;
    }

    public void showProbabilityResult(Double[][] probabilities) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        if (fm.findFragmentByTag("pmd") == null) {
            int icon;
            if (resources.get(CATEGORY) == ItemTable.CATEGORY_ATTACK) {
                icon = R.drawable.heart;
            } else {
                icon = R.drawable.shield;
            }
            ProbabilityMatrixDialog pmd = new ProbabilityMatrixDialog(probabilities,
                    R.string.probabilities_dialog_title, icon);
            pmd.show(fm, "pmd");
        }
    }

    public void computeProbabilities() {
        if (!isBusy()) {
            computingProbability = true;
            Cursor c = getActivity().getContentResolver().query(uriWithAdd, PROJECTION, null, null, null);
            new AsyncProbabilityTask(c, CategoryList.this).execute("");
        }
    }

    protected boolean isBusy() {
        return computingProbability
                || rolling
                || getActivity().getSupportFragmentManager().findFragmentByTag("eid") != null
                || getActivity().getSupportFragmentManager().findFragmentByTag("pmd") != null
                || getActivity().getSupportFragmentManager().findFragmentByTag("rrd") != null;
    }


    // Creates a new loader after the initLoader () call
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cursorLoader = new CursorLoader(getActivity(),
                uriWithAdd, PROJECTION, null, null, ItemColumn.ID.getName() + " ASC");
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        ((ItemCursorAdapter) gridView.getAdapter()).swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // data is not available anymore, delete reference
        ((ItemCursorAdapter) gridView.getAdapter()).swapCursor(null);
    }
}
