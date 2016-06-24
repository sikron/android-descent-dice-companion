package com.skronawi.DescentDiceCompanion.app.items;

import com.skronawi.DescentDiceCompanion.lib.dice.DiceThrow;

import java.util.Map;

public interface RollResultReceiver {

    void showRollingBusyDialog(boolean doShow, String dialogRandomnessString);

    void setResult(Map<Integer, DiceThrow> diceThrows);

    void clearResult();

    void showToast(int resourceId, int length);

    void setRolling(boolean isRolling);

    void rollResult();
}
