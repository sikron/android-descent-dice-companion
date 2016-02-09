package com.skronawi.DescentDiceCompanion.app.preferences;


import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import com.skronawi.DescentDiceCompanion.R;

@SuppressLint("NewApi")
public class PreferencesFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //noinspection ResourceType
        addPreferencesFromResource(R.layout.preferences);

        if (getResources().getBoolean(R.bool.portrait_only)) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (!getResources().getBoolean(R.bool.portrait_only)) {
            //noinspection ResourceType
            getActivity().setRequestedOrientation(newConfig.orientation);
        }
    }
}
