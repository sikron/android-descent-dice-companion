package com.skronawi.DescentDiceCompanion.app.preferences;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;

public class SetPreferencesFragmentActivity extends Activity {

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new PreferencesFragment()).commit();
    }

}
