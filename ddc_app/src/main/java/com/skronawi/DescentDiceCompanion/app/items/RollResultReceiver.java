package com.skronawi.DescentDiceCompanion.app.items;

import com.skronawi.DescentDiceCompanion.lib.dice.DiceThrow;

import java.util.Map;

public interface RollResultReceiver {

    public void showRollingBusyDialog(boolean doShow, String dialogRandomnessString);

    public void setResult(Map<Integer, DiceThrow> diceThrows);

    void clearResult();

    public void showToast(int resourceId, int length);

    public void setRolling(boolean isRolling);
}
