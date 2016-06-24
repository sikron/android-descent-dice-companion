package com.skronawi.DescentDiceCompanion.app.tasks;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import com.skronawi.DescentDiceCompanion.R;
import com.skronawi.DescentDiceCompanion.app.database.ItemTable;
import com.skronawi.DescentDiceCompanion.app.items.RollResultReceiver;
import com.skronawi.DescentDiceCompanion.app.main.MainActivity;
import com.skronawi.DescentDiceCompanion.app.main.MainActivity.ResultOfAsyncTask;
import com.skronawi.DescentDiceCompanion.app.random.RandomnessProvider;
import com.skronawi.DescentDiceCompanion.lib.dice.*;
import com.skronawi.DescentDiceCompanion.lib.random.Randomness;

import java.util.HashMap;
import java.util.Map;


public class AsyncRollingTask extends AsyncTask<String, Integer, ResultOfAsyncTask> {

    public static final int SLEEP = 1000;

    private Map<Integer, DiceThrow> rollResult;
    private final Activity activity;
    private boolean dicePoolEmpty;
    private RollResultReceiver rollResultReceiver;
    private Map<Integer, ItemDice> pool;    //one diceThrow per item
    private Randomness randomness;
    private long startTime;
    private boolean error;
    private String dialogRandomnessString;
    private Cursor cursor;


    public AsyncRollingTask(Cursor c, RollResultReceiver rollResultReceiver, Activity activity) {

        this.cursor = c;
        this.rollResultReceiver = rollResultReceiver;
        this.activity = activity;
        this.startTime = System.currentTimeMillis();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        if (prefs.getBoolean("randomorg_preference", false)) {
            Log.v(getClass().getCanonicalName(), "using randomOrg randomness");
            randomness = RandomnessProvider.getExternalRandomness();
            dialogRandomnessString = activity.getResources().getString(
                    R.string.title_randomorg_preference) + " : Random.org";
        } else {
            Log.v(getClass().getCanonicalName(), "using java randomness");
            randomness = RandomnessProvider.getLocalRandomness();
            dialogRandomnessString = activity.getResources().getString(
                    R.string.title_randomorg_preference) + " : Android";
        }
    }

    @Override
    protected void onPreExecute() {

        pool = new HashMap<Integer, ItemDice>(10);
        boolean noItemsActive = true;

        while (cursor.moveToNext()) {
            if (cursor.getInt(ItemTable.ItemColumn.SELECTED.getIndex()) == 1) {
                noItemsActive = false;
                int itemId = cursor.getInt(ItemTable.ItemColumn.ID.getIndex());
                ItemDice itemDice = ItemDice.fromMask(cursor.getString(
                        ItemTable.ItemColumn.DICE.getIndex()));
                pool.put(itemId, itemDice);
            }
        }
        cursor.close();

        if (noItemsActive || (numberOfDice(pool) == 0)) {
            Log.v(getClass().getCanonicalName(), "no dice to roll");
            dicePoolEmpty = true;
        } else {
            rollResultReceiver.showRollingBusyDialog(true, dialogRandomnessString);
        }

        super.onPreExecute();
    }

    @Override
    protected ResultOfAsyncTask doInBackground(String... strData) {

        ResultOfAsyncTask taskResult = new ResultOfAsyncTask();

        try {

            if (!dicePoolEmpty) {

                Log.v(getClass().getCanonicalName(), "roll the dice");

                if (PreferenceManager.getDefaultSharedPreferences(
                        activity.getApplicationContext()).getBoolean("sound_preference", true)) {
                    ((MainActivity) activity).playSound();
                }

                /*
                roll per itemDice -> diceThrow
                result is a list of diceThrow
                 */

                rollResult = new HashMap<Integer, DiceThrow>(pool.size());
                for (Integer itemId : pool.keySet()) {

                    ItemDice itemDice = pool.get(itemId);
                    DiceThrow diceThrow = new DiceThrow();

                    int rolledSide;

                    for (Die die : itemDice) {

                        rolledSide = randomness.nextInt(DieInfo.NUMBER_OF_SIDES);
                        diceThrow.add(new DieThrow().setDie(die).setSideIdx(rolledSide));
                    }

                    rollResult.put(itemId, diceThrow);
                }

                Log.v(getClass().getCanonicalName(), "rolled dice : " + formatDice(pool));

                //compute remaining sleep-time; should be positive
                long duration = System.currentTimeMillis() - startTime;
                long remainingSleep = Math.max(SLEEP - duration, 0);
                Log.v(getClass().getCanonicalName(), "duration : " + duration);
                Log.v(getClass().getCanonicalName(), "remaining sleep time : " + remainingSleep);

                Thread.sleep(remainingSleep);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            //there was a problem with the randomness
            Log.e(getClass().getCanonicalName(), "exception when rolling dice : " + e.getMessage());
            error = true;
        }

        return taskResult;
    }

    private String formatDice(Map<Integer, ItemDice> pool) {

        String diceString = "";

        for (ItemDice itemDice : pool.values()) {
            for (Die die : itemDice) {
                diceString += die.getType().name() + ", ";
            }
        }

        if (diceString.length() == 0) {
            diceString = "none";
        }

        return diceString;
    }

    private int numberOfDice(Map<Integer, ItemDice> pool) {

        int numberOfDice = 0;
        for (ItemDice itemDice : pool.values()) {
            numberOfDice += itemDice.size();
        }
        return numberOfDice;
    }

    @Override
    protected void onPostExecute(ResultOfAsyncTask result) {

        if (error) {
            rollResultReceiver.setResult(rollResult);
            new AlertDialog.Builder(activity)
                    .setTitle(R.string.error).setMessage(R.string.error_no_net)
                    .setPositiveButton(R.string.button_ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).show();

        } else if (dicePoolEmpty) {
            rollResultReceiver.clearResult();
            rollResultReceiver.showToast(R.string.pool_empty, Toast.LENGTH_SHORT);

        } else {
            //Log.v(getClass().getCanonicalName(), "rolled result : " + formatResult(rollResult));
            rollResultReceiver.setResult(rollResult);
            boolean showRollDetails = PreferenceManager.getDefaultSharedPreferences(activity)
                    .getBoolean("openrolldetails_preference", false);
            if (showRollDetails) {
                rollResultReceiver.rollResult();
            }
        }

        rollResultReceiver.setRolling(false);
        rollResultReceiver.showRollingBusyDialog(false, null);
    }

    //TODO rollResult was computed wrong, -1 for miss was just subtracted i think
//    private String formatResult(int[] rollResult) {
//        return "[" + rollResult[0] + "," + rollResult[1] + "," + rollResult[2] + "]";
//    }

    @Override
    protected void onCancelled() {

        Log.v(getClass().getCanonicalName(), "roll canceled");
        rollResultReceiver.clearResult();

        rollResultReceiver.setRolling(false);
        rollResultReceiver.showRollingBusyDialog(false, null);

        super.onCancelled();
    }
}