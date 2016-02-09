package com.skronawi.DescentDiceCompanion.app.dice;

import com.skronawi.DescentDiceCompanion.R;
import com.skronawi.DescentDiceCompanion.lib.dice.DieInfo;

import java.util.HashMap;
import java.util.Map;

public class DiceImageResolver {

    private static final Map<DieInfo, int[]> sides = new HashMap<DieInfo, int[]>();
    static {
        sides.put(DieInfo.BLACK, new int[]{
                R.drawable.die_black_side0
                , R.drawable.die_black_side1
                , R.drawable.die_black_side2
                , R.drawable.die_black_side3
                , R.drawable.die_black_side4
                , R.drawable.die_black_side5
        });
        sides.put(DieInfo.GREY, new int[]{
                R.drawable.die_grey_side0
                , R.drawable.die_grey_side1
                , R.drawable.die_grey_side2
                , R.drawable.die_grey_side3
                , R.drawable.die_grey_side4
                , R.drawable.die_grey_side5
        });
        sides.put(DieInfo.BROWN, new int[]{
                R.drawable.die_brown_side0
                , R.drawable.die_brown_side1
                , R.drawable.die_brown_side2
                , R.drawable.die_brown_side3
                , R.drawable.die_brown_side4
                , R.drawable.die_brown_side5
        });
        sides.put(DieInfo.BLUE, new int[]{
                R.drawable.die_blue_side0
                , R.drawable.die_blue_side1
                , R.drawable.die_blue_side2
                , R.drawable.die_blue_side3
                , R.drawable.die_blue_side4
                , R.drawable.die_blue_side5
        });
        sides.put(DieInfo.RED, new int[]{
                R.drawable.die_red_side0
                , R.drawable.die_red_side1
                , R.drawable.die_red_side2
                , R.drawable.die_red_side3
                , R.drawable.die_red_side4
                , R.drawable.die_red_side5
        });
        sides.put(DieInfo.YELLOW, new int[]{
                R.drawable.die_yellow_side0
                , R.drawable.die_yellow_side1
                , R.drawable.die_yellow_side2
                , R.drawable.die_yellow_side3
                , R.drawable.die_yellow_side4
                , R.drawable.die_yellow_side5
        });
        sides.put(DieInfo.GREEN, new int[]{
                R.drawable.die_green_side0
                , R.drawable.die_green_side1
                , R.drawable.die_green_side2
                , R.drawable.die_green_side3
                , R.drawable.die_green_side4
                , R.drawable.die_green_side5
        });
    }

    public static int resolveDieImageId(DieInfo dieInfo) {

        int imageId = 0;

        switch (dieInfo) {
            case BLACK:
                imageId = R.drawable.die_black;
                break;
            case GREY:
                imageId = R.drawable.die_grey;
                break;
            case BROWN:
                imageId = R.drawable.die_brown;
                break;
            case BLUE:
                imageId = R.drawable.die_blue;
                break;
            case RED:
                imageId = R.drawable.die_red;
                break;
            case YELLOW:
                imageId = R.drawable.die_yellow;
                break;
            case GREEN:
                imageId = R.drawable.die_green;
                break;
        }
        return imageId;
    }

    public static int resolveDieX2ImageId(DieInfo dieInfo) {

        int imageId = 0;

        switch (dieInfo) {
            case BLACK:
                imageId = R.drawable.die_black_x2;
                break;
            case GREY:
                imageId = R.drawable.die_grey_x2;
                break;
            case BROWN:
                imageId = R.drawable.die_brown_x2;
                break;
            case BLUE:
                imageId = R.drawable.die_blue_x2;
                break;
            case RED:
                imageId = R.drawable.die_red_x2;
                break;
            case YELLOW:
                imageId = R.drawable.die_yellow_x2;
                break;
            case GREEN:
                imageId = R.drawable.die_green_x2;
                break;
        }
        return imageId;
    }

    public static int resolveDieSideImageId(DieInfo dieInfo, int sideIdx){

        return sides.get(dieInfo)[sideIdx];
    }
}
