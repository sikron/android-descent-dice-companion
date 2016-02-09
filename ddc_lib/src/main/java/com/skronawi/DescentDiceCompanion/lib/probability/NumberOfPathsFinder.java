package com.skronawi.DescentDiceCompanion.lib.probability;

import com.skronawi.DescentDiceCompanion.lib.dice.Die;
import com.skronawi.DescentDiceCompanion.lib.dice.DieInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

//import android.util.Log;

/*
 * finds the number of paths, which are possible for a certain set of dice, for 
 * all combinations of damage and range
 */
public class NumberOfPathsFinder {

    private Die[] dice;

    //level -> ( (damage,range) -> number of paths)
    private Map<Integer, Map<DamageRangeKey, Long>> pathCache;


    public NumberOfPathsFinder(Die[] dice) {
        this.dice = dice;
        pathCache = new HashMap<Integer, Map<DamageRangeKey, Long>>(dice.length * 2);
    }

    public Map<DamageRangeKey, Long> gather() {

        int startLevel = 0;

//		Log.v(getClass().getCanonicalName(), "start gathering");

        //damage -> number of paths, where exactly this damage can be reached
        Map<DamageRangeKey, Long> paths = recursiveGather(startLevel);

        //may be null
        return paths;
    }

    private Map<DamageRangeKey, Long> recursiveGather(int currentLevel) {

//		Log.v(getClass().getCanonicalName(), "gathering pathes at level "+currentLevel+" of "+dice.length);

        //iterate over all sides of dice at index 'currentLevel' and see, if the range and
        //damage of this side score

        Map<DamageRangeKey, Long> currPathsNumber = new HashMap<DamageRangeKey, Long>(30);

        for (int i = 0; i < DieInfo.NUMBER_OF_SIDES; i++) {

//			Log.v(getClass().getCanonicalName(), "evaluating die-side "+i+" on level "+currentLevel);

            DieInfo currentDie = dice[currentLevel].getType();
            int[] dieSide = currentDie.getSides()[i];
            int dieRange = dieSide[0];
            int dieDamage = dieSide[2];

//			Log.v(getClass().getCanonicalName(), "range "+dieRange+", damage "+dieDamage);

            //any path with a MISS on it can not contribute to the pathNumber on this path !!
            if (dieRange == -1) {
                /*
                 * z.b zwei wuerfel. der 2. wirft ein miss, somit darf der 1. auch nicht die subtree-paths
				 * ab dem MISS zu den damagePaths integrieren.
				 * aber alle siblings zaehlen!
				 */

//				Log.v(getClass().getCanonicalName(), "miss - do not go on gathering on this path");

                continue;
            }

            Map<DamageRangeKey, Long> subTreePathsNumber = null;
            //only go to the next level, if there is still a dice there
            if (currentLevel + 1 < dice.length) {

                //check whether this subtree has already been walked. if so, use the walk-result.
                //if not, descend recursively
                subTreePathsNumber = pathCache.get(Integer.valueOf(currentLevel + 1));
                if (subTreePathsNumber == null) {

//					Log.v(getClass().getCanonicalName(), "descend recursively to next level for gathering pathes");

                    subTreePathsNumber = recursiveGather(currentLevel + 1);
                } else {

//					Log.v(getClass().getCanonicalName(), "using subtree from cache");
                }
            }

            integrate(currPathsNumber, subTreePathsNumber, dieDamage, dieRange);
        }

//		Log.v(getClass().getCanonicalName(), "putting subtree into cache for level "+currentLevel);

        pathCache.put(Integer.valueOf(currentLevel), currPathsNumber);

        //mappings from (damage, range) to numberOfPathes, where damage > 0; damage not necessarily consecutive!!
        return currPathsNumber;
    }

    private void integrate(Map<DamageRangeKey, Long> currPathsNumber,
                           Map<DamageRangeKey, Long> subTreePathsNumber, Integer damage, Integer range) {

        if (subTreePathsNumber == null) {

//			Log.v(getClass().getCanonicalName(), "integrating values from a leaf");

            //then damage stems from a leaf, so increase the paths for this damage and range by 1

            DamageRangeKey damageRangeKey = new DamageRangeKey(damage, range);
            Long currentNumberOfPaths = currPathsNumber.get(damageRangeKey);
            if (currentNumberOfPaths == null) {
                currentNumberOfPaths = new Long(0);
            }
            currentNumberOfPaths = currentNumberOfPaths + 1;
            currPathsNumber.put(damageRangeKey, currentNumberOfPaths);

        } else {
            /*
             * then damage stems from a root of a subtree, which has already gathered damagePaths;
			 * the currPathsNumber and the subTreePathsNumber cannot just be added: the mappings from
			 * the subtree must be added with their keys modified by damage and range to the currPathsNumber!
			 */

//			Log.v(getClass().getCanonicalName(), "integrating values from a subtree");

            Set<DamageRangeKey> subTreeKeys = subTreePathsNumber.keySet();
            for (DamageRangeKey subTreeKey : subTreeKeys) {

                DamageRangeKey insertKey = new DamageRangeKey(
                        subTreeKey.getDamage() + damage, subTreeKey.getRange() + range);
                Long pathsToAdd = currPathsNumber.get(insertKey);
                if (pathsToAdd == null) {
                    pathsToAdd = new Long(0);
                }
                pathsToAdd = pathsToAdd + subTreePathsNumber.get(subTreeKey);
                currPathsNumber.put(insertKey, pathsToAdd);
            }
        }
    }
}
