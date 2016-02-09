package com.skronawi.DescentDiceCompanion.app.tasks;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import com.skronawi.DescentDiceCompanion.R;
import com.skronawi.DescentDiceCompanion.app.database.ItemTable;
import com.skronawi.DescentDiceCompanion.app.items.CategoryList;
import com.skronawi.DescentDiceCompanion.app.main.MainActivity.ResultOfAsyncTask;
import com.skronawi.DescentDiceCompanion.lib.dice.Die;
import com.skronawi.DescentDiceCompanion.lib.dice.ItemDice;
import com.skronawi.DescentDiceCompanion.lib.probability.DamageRangeKey;
import com.skronawi.DescentDiceCompanion.lib.probability.NumberOfPathsFinder;

import java.util.*;


public class AsyncProbabilityTask extends AsyncTask<String, Integer, ResultOfAsyncTask> {

    public static final int SLEEP = 1000;
    private CategoryList list;
    private long startTime;
    private Collection<ItemDice> pool;
    private Cursor cursor;
    private boolean dicePoolEmpty;
    private boolean error;
    private long duration;
    private Map<DamageRangeKey, Long> pathsPerDamageAndRange;
    private Die[] dice;
    private Double[][] aggregatedProbabilities;
    private boolean diceNumberExceeded;

    private static final int MAX_NUMBER = 9;


    public AsyncProbabilityTask(Cursor c, CategoryList list) {

        this.cursor = c;
        this.list = list;
        this.startTime = System.currentTimeMillis();
    }

    @Override
    protected void onPreExecute() {

        pool = new ArrayList<ItemDice>(10);
        boolean noItemsActive = true;

        while (cursor.moveToNext()) {

            if (cursor.getInt(ItemTable.ItemColumn.SELECTED.getIndex()) == 1) {
                noItemsActive = false;
                ItemDice itemDice = ItemDice.fromMask(cursor.getString(
                        ItemTable.ItemColumn.DICE.getIndex()));
                pool.add(itemDice);
            }
        }
        cursor.close();

        if (noItemsActive || (numberOfDice(pool) == 0)) {
            Log.v(getClass().getCanonicalName(), "no dice to compute probability for");
            dicePoolEmpty = true;

        } else {
            dice = poolToDice(pool);

            if (dice.length > MAX_NUMBER) {
                diceNumberExceeded = true;
            } else {
                list.showProbabilityBusyDialog(true);
            }
        }

        super.onPreExecute();
    }

    private Die[] poolToDice(Collection<ItemDice> pool) {

        List<Die> dice = new ArrayList<Die>();
        for (ItemDice itemDice : pool){
            for (Die die : itemDice){
                dice.add(die);
            }
        }
        Die[] asArray = new Die[dice.size()];
        asArray = dice.toArray(asArray);
        return asArray;
    }

    @Override
    protected ResultOfAsyncTask doInBackground(String... strData) {

        ResultOfAsyncTask taskResult = new ResultOfAsyncTask();

        try {

            if (!dicePoolEmpty && !diceNumberExceeded) {

                String diceString = "";
                for (Die d : dice) {
                    diceString += d.getType().name() + "  ";
                }
                Log.v(getClass().getCanonicalName(), "computing probabilities for dice : " + diceString);

                NumberOfPathsFinder nopf = new NumberOfPathsFinder(dice);

                pathsPerDamageAndRange = nopf.gather();

                //compute remaining sleep-time; should be positive
                duration = System.currentTimeMillis() - startTime;
                long remainingSleep = Math.max(SLEEP - duration, 0);
                Log.v(getClass().getCanonicalName(), "duration : " + duration);
                Log.v(getClass().getCanonicalName(), "remaining sleep time : " + remainingSleep);
                Thread.sleep(remainingSleep);

                Log.v(getClass().getCanonicalName(), "probabilities computed");

                writeLogs();

                aggregatedProbabilities = aggregateProbabilities(pathsPerDamageAndRange);
                aggregatedProbabilities[0][0] = 1.0;
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            //there was a problem
            Log.e(getClass().getCanonicalName(), "exception when computing probabilities : " + e.getMessage());
            error = true;
        }

        return taskResult;
    }

    private int numberOfDice(Collection<ItemDice> pool) {

        int numberOfDice = 0;
        for (ItemDice itemDice : pool){
            numberOfDice += itemDice.size();
        }
        return numberOfDice;
    }

    @Override
    protected void onPostExecute(ResultOfAsyncTask result) {

        if (error) {
            new AlertDialog.Builder(list.getActivity())
                    .setTitle(R.string.error).setMessage(R.string.error_probabilities)
                    .setPositiveButton(R.string.button_ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).show();

        } else if (dicePoolEmpty) {
            list.showToast(R.string.pool_empty, Toast.LENGTH_SHORT);

        } else if (diceNumberExceeded) {
            list.showToast(list.getResources().getString(
                    R.string.item_max_dice_number, MAX_NUMBER), Toast.LENGTH_SHORT);

        } else {
            list.showProbabilityResult(aggregatedProbabilities);
        }

        list.setComputingProbability(false);
        list.showProbabilityBusyDialog(false);
    }

    private void writeLogs() {

        Log.v(getClass().getCanonicalName(), "combinatoric number of paths : " +
                Math.pow(dice[0].getType().getSides().length, dice.length));

        Log.v(getClass().getCanonicalName(), "duration [ms] " + duration);

        Log.v(getClass().getCanonicalName(), "range -> damage : number of paths \n" +
                numberMatrix(pathsPerDamageAndRange, new IdentityFormatter()));

        Log.v(getClass().getCanonicalName(), "range -> damage : number of paths (%) \n" +
                numberMatrix(pathsPerDamageAndRange, new PercentFormatter(dice)));

        Log.v(getClass().getCanonicalName(), "range -> damage : number of paths aggregated over all submatrices \n" +
                aggregateMatrix(pathsPerDamageAndRange, new IdentityFormatter()));

        Log.v(getClass().getCanonicalName(), "range -> damage : number of paths aggregated over all submatrices (%)\n" +
                aggregateMatrix(pathsPerDamageAndRange, new PercentFormatter(dice)));
    }

    @Override
    protected void onCancelled() {

        Log.v(getClass().getCanonicalName(), "probability computation canceled");

        list.setRolling(false);
        list.showProbabilityBusyDialog(false);

        super.onCancelled();
    }

    private void addDice(int[] dice, int[] others) {

        for (int i = 0; i < dice.length; i++) {
            dice[i] += others[i];
        }
    }

    private Double[][] aggregateProbabilities(Map<DamageRangeKey, Long> pathes) {

        Long[][] aggregatedMatrix = aggregate(pathes);
        Double[][] aggregatedPercentages = new Double[aggregatedMatrix.length][aggregatedMatrix[0].length];
        PercentFormatter formatter = new PercentFormatter(dice);
        for (int i = 0; i < aggregatedMatrix.length; i++) {
            for (int j = 0; j < aggregatedMatrix[0].length; j++) {
                aggregatedPercentages[i][j] = formatter.format(aggregatedMatrix[i][j]);
            }
        }
        return aggregatedPercentages;
    }

    private String aggregateMatrix(Map<DamageRangeKey, Long> damagePaths,
                                   ResultFormatter formatter) {

        Long[][] matrix = aggregate(damagePaths);
        return matrixToString(matrix, formatter, " >= ");
    }

    /*
     * imagine a matrix range -> damage.
     * now ALL sub-matrices are aggregated into one value.
     * start-coords are all (i,j), aggregation for this (i,j)-cell is over all cells (x,y),
     * with i <= x and j <= y
     *
     * when the user asks "whats the probability for damage for range 3" than he actually wants
     * to know not only the probability for every single damage-value, but the probability for
     * the damage >= every single damage
     */
    private Long[][] aggregate(Map<DamageRangeKey, Long> damagePaths) {

        Long[][] matrix = matrix(damagePaths);
        Long[][] aggregatedMatrix = new Long[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                aggregatedMatrix[i][j] = aggregateSubMatrix(matrix, i, j);
            }
        }
        return aggregatedMatrix;
    }

    private Long aggregateSubMatrix(Long[][] matrix, int iStart,
                                    int jStart) {

        Long value = new Long(0);
        for (int i = iStart; i < matrix.length; i++) {
            for (int j = jStart; j < matrix[0].length; j++) {
                value += matrix[i][j];
            }
        }
        return value;
    }

    private String numberMatrix(Map<DamageRangeKey, Long> damagePaths, ResultFormatter formatter) {

        Long[][] matrix = matrix(damagePaths);
        return matrixToString(matrix, formatter, " = ");
    }

    private Long[][] matrix(Map<DamageRangeKey, Long> damagePaths) {

        int maxRange = findMaxRange(damagePaths.keySet());
        int maxDamage = findMaxDamage(damagePaths.keySet());
        Long[][] matrix = new Long[maxRange + 1][maxDamage + 1];  //from 0 to maxRange INCL!!
        for (int i = 0; i <= maxRange; i++) {
            for (int j = 0; j <= maxDamage; j++) {
                Long numberOfPaths = damagePaths.get(new DamageRangeKey(j, i));
                if (numberOfPaths == null) {
                    numberOfPaths = new Long(0);
                }
                matrix[i][j] = Long.valueOf(numberOfPaths);
            }
        }
        return matrix;
    }

    private int findMaxDamage(Set<DamageRangeKey> keySet) {

        List<Integer> damage = new ArrayList<Integer>(keySet.size());
        for (DamageRangeKey k : keySet) {
            damage.add(k.getDamage());
        }
        return max(damage);
    }

    private int max(List<Integer> values) {

        Integer[] array = new Integer[values.size()];
        array = values.toArray(array);
        Arrays.sort(array);
        return array[array.length - 1];
    }

    private int findMaxRange(Set<DamageRangeKey> keySet) {

        List<Integer> ranges = new ArrayList<Integer>(keySet.size());
        for (DamageRangeKey k : keySet) {
            ranges.add(k.getRange());
        }
        return max(ranges);
    }

    private String matrixToString(Long[][] matrix, ResultFormatter formatter, String comp) {

        //erste zeile
        StringBuffer line = new StringBuffer("\t");
        for (int i = 0; i < matrix[0].length; i++) {
            line.append("     d").append(comp).append(i).append("\t|");
        }
        line.append("\n");

        //jede zeile mit range als header
        for (int i = 0; i < matrix.length; i++) {
            line.append("r").append(comp).append(i).append("\t |");
            for (int j = 0; j < matrix[i].length; j++) {
                line.append("    ").append(formatter.format(matrix[i][j])).append("\t|");
            }
            line.append("\n");
        }
        return line.toString();
    }


    interface ResultFormatter {
        double format(long value);
    }

    private class IdentityFormatter implements ResultFormatter {

        @Override
        public double format(long value) {
            return value;
        }
    }

    private class PercentFormatter implements ResultFormatter {

        private double allPaths;

        public PercentFormatter(Die[] dice) {
            allPaths = Math.pow(dice[0].getType().getSides().length, dice.length);
        }

        @Override
        public double format(long value) {
            return Math.max(((double) value) / allPaths, 0);
            //some little computational errors with negative probs are summed up and produce big errors
        }
    }
}