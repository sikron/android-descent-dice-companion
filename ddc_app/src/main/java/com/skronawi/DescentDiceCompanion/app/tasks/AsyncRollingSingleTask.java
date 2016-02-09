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


public class AsyncRollingSingleTask extends AsyncTask<String, Integer, ResultOfAsyncTask> {

    public static final int SLEEP = 1000;

    private final int dieThrowIdx;
    private final DiceThrow diceThrow;
    private Map<Integer, DiceThrow> rollResult;
    private final Activity activity;
    private RollResultReceiver rollResultReceiver;
    private Randomness randomness;
    private long startTime;
    private boolean error;
    private String dialogRandomnessString;


    public AsyncRollingSingleTask(Map<Integer, DiceThrow> rollResult, DiceThrow diceThrow, int dieThrowIdx,
                                  RollResultReceiver rollResultReceiver, Activity activity) {

        this.rollResultReceiver = rollResultReceiver;
        this.activity = activity;
        this.startTime = System.currentTimeMillis();
        this.dieThrowIdx = dieThrowIdx;
        this.diceThrow = diceThrow;
        this.rollResult = rollResult;

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

        rollResultReceiver.showRollingBusyDialog(true, dialogRandomnessString);
        super.onPreExecute();
    }

    @Override
    protected ResultOfAsyncTask doInBackground(String... strData) {

        ResultOfAsyncTask taskResult = new ResultOfAsyncTask();

        try {

            if (PreferenceManager.getDefaultSharedPreferences(
                    activity.getApplicationContext()).getBoolean("sound_preference", true)) {
                ((MainActivity) activity).playSound();
            }

            int rolledSide = randomness.nextInt(DieInfo.NUMBER_OF_SIDES);
            diceThrow.get(dieThrowIdx).setSideIdx(rolledSide);

//            Log.v(getClass().getCanonicalName(), "rolled dice : " + formatDice(pool));

            //compute remaining sleep-time; should be positive
            long duration = System.currentTimeMillis() - startTime;
            long remainingSleep = Math.max(SLEEP - duration, 0);
            Log.v(getClass().getCanonicalName(), "duration : " + duration);
            Log.v(getClass().getCanonicalName(), "remaining sleep time : " + remainingSleep);

            Thread.sleep(remainingSleep);

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

        } else {
            //Log.v(getClass().getCanonicalName(), "rolled result : " + formatResult(rollResult));
            rollResultReceiver.setResult(rollResult);
        }

        rollResultReceiver.setRolling(false);
        rollResultReceiver.showRollingBusyDialog(false, null);
    }

    @Override
    protected void onCancelled() {

        Log.v(getClass().getCanonicalName(), "roll canceled");
        rollResultReceiver.clearResult();

        rollResultReceiver.setRolling(false);
        rollResultReceiver.showRollingBusyDialog(false, null);

        super.onCancelled();
    }
}