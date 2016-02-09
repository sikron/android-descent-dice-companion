package com.skronawi.DescentDiceCompanion.app.main;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TabHost;
import com.skronawi.DescentDiceCompanion.R;
import com.skronawi.DescentDiceCompanion.app.items.CategoryList;
import com.skronawi.DescentDiceCompanion.app.items.HeroList;
import com.skronawi.DescentDiceCompanion.app.preferences.PreferencesActivity;
import com.skronawi.DescentDiceCompanion.app.preferences.SetPreferencesFragmentActivity;
import com.skronawi.DescentDiceCompanion.app.random.RandomnessProvider;
import com.skronawi.DescentDiceCompanion.app.shake.RealSensorHandling;
import com.skronawi.DescentDiceCompanion.app.shake.SensorHandling;
import com.skronawi.DescentDiceCompanion.app.shake.ShakeDetector.OnShakeListener;
import com.skronawi.DescentDiceCompanion.lib.dice.DiceProvider;

import java.util.HashSet;

public class MainActivity extends FragmentActivity {

    public static final String TAG = MainActivity.class.getCanonicalName();

    private SoundPool soundPool;
    private HashSet<Integer> soundsLoaded;
    private int[] soundIDs;
    private TabHost tabHost;
    private CategoryList activeCategoryList;

    private SensorHandling sensorHandling;

    public static class ResultOfAsyncTask {
        int iErrorCode = 0;
    }

    private static boolean IS_VERSION_SMALLER_THAN_HONEYCOMB =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        if (DiceProvider.getInstance() == null) {
            DiceProvider.init();
        }

        if (soundsLoaded == null) {
            soundsLoaded = new HashSet<Integer>();
            setupSound();
        }

        setPreferences();

        RandomnessProvider.init(this);

        if (getResources().getBoolean(R.bool.portrait_only)) {
            Log.v(getClass().getCanonicalName(), "portrait only");
            //noinspection ResourceType
            setRequestedOrientation(Configuration.ORIENTATION_PORTRAIT);
        }

        if (getResources().getBoolean(R.bool.show_tabs)) {

            //then the main.xml for the phones is loaded with the tabhost-layout
            Log.v(getClass().getCanonicalName(), "layout for phones (tabs)");
            setupPhoneLayout(getResources().getString(R.string.items_attack),
                    getResources().getString(R.string.items_defense),
                    getResources().getString(R.string.items_hero));

            setupShakeDetection();

        } else {

            //otherwise the main.xml for tablets is loaded - portrait or landscape.
            //fragments must be set programmatically, otherwise handling orientation myself won't work

            Log.v(getClass().getCanonicalName(), "layout for tablets (three-list)");

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            fragmentTransaction.add(R.id.category_attack_container, new CategoryList(),
                    getResources().getString(R.string.items_attack));

            fragmentTransaction.add(R.id.category_defense_container, new CategoryList(),
                    getResources().getString(R.string.items_defense));

            fragmentTransaction.add(R.id.category_hero_container, new HeroList(),
                    getResources().getString(R.string.items_hero));

            //"can not perform this action after onSaveInstanceState" => commit() -> commitAllowingStateLoss()
            fragmentTransaction.commitAllowingStateLoss();
        }
    }

    private void setupShakeDetection() {

		/*
         * from   http://code.google.com/p/openintents/wiki/SensorSimulator :
		 * 
		 * Note 2: Whenever you are not connected to the simulator, you will get real device sensor data: 
		 * the org.openintents.hardware.SensorManagerSimulator class transparently calls the SensorManager 
		 * returned by the system service in this case.
		 * 
		 * but then the recognition does not work the first times. so i use the RealSensorHandling
		 */
        sensorHandling = new RealSensorHandling(this, new OnShakeListener() {
            @Override
            public void onShake() {

                Log.v(getClass().getName(), "shake detected");

                if (activeCategoryList != null) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                    if (prefs.getBoolean("shake_preference", false)) {
                        Log.v(getClass().getName(), "rolled by shaking");
                        activeCategoryList.roll();
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorHandling != null) {
            sensorHandling.register();
            Log.v(getClass().getName(), "ShakeDetector registered on resume");
        }
    }

    @Override
    protected void onPause() {
        if (sensorHandling != null) {
            sensorHandling.unregister();
            Log.v(getClass().getName(), "ShakeDetector un-registered on pause");
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (sensorHandling != null) {
            sensorHandling.unregister();
            Log.v(getClass().getName(), "ShakeDetector un-registered on stop");
        }
        super.onStop();
    }

    private void setupPhoneLayout(final String attack, final String defense, final String hero) {

        tabHost = (TabHost) findViewById(android.R.id.tabhost);
        tabHost.setup();

        //usually phones with > 4.0.3 do not show icons, only text

        TabHost.OnTabChangeListener tabChangeListener = new TabHost.OnTabChangeListener() {

            @Override
            public void onTabChanged(String tabId) {

                FragmentManager fm = getSupportFragmentManager();
                CategoryList attackFragment = (CategoryList) fm.findFragmentByTag(attack);
                CategoryList defenseFragment = (CategoryList) fm.findFragmentByTag(defense);
                HeroList heroFragment = (HeroList) fm.findFragmentByTag(hero);
                FragmentTransaction ft = fm.beginTransaction();

                if (attackFragment != null)
                    ft.hide(attackFragment);

                if (defenseFragment != null)
                    ft.hide(defenseFragment);

                if (heroFragment != null)
                    ft.hide(heroFragment);

                if (tabId.equalsIgnoreCase(attack)) {
                    if (attackFragment == null) {
                        attackFragment = new CategoryList();
                        ft.add(R.id.realtabcontent, attackFragment, attack);
                    } else {
                        ft.show(attackFragment);
                    }
                    activeCategoryList = attackFragment;

                } else if (tabId.equalsIgnoreCase(defense)) {
                    if (defenseFragment == null) {
                        defenseFragment = new CategoryList();
                        ft.add(R.id.realtabcontent, defenseFragment, defense);
                    } else {
                        ft.show(defenseFragment);
                    }
                    activeCategoryList = defenseFragment;
                } else {
                    if (heroFragment == null) {
                        heroFragment = new HeroList();
                        ft.add(R.id.realtabcontent, heroFragment, hero);
                    } else {
                        ft.show(heroFragment);
                    }
                    activeCategoryList = heroFragment;
                }

                ft.commit();
            }
        };

        tabHost.setOnTabChangedListener(tabChangeListener);

        TabHost.TabSpec tSpecAttack = tabHost.newTabSpec(attack);
        TabHost.TabSpec tSpecDefense = tabHost.newTabSpec(defense);
        TabHost.TabSpec tSpecHero = tabHost.newTabSpec(hero);

        tSpecAttack.setIndicator(attack, getResources().getDrawable(R.drawable.attack));
        tSpecAttack.setContent(new DummyTabContent(getBaseContext()));
        tabHost.addTab(tSpecAttack);

        tSpecDefense.setIndicator(defense, getResources().getDrawable(R.drawable.defense));
        tSpecDefense.setContent(new DummyTabContent(getBaseContext()));
        tabHost.addTab(tSpecDefense);

        tSpecHero.setIndicator(hero, getResources().getDrawable(R.drawable.hero));
        tSpecHero.setContent(new DummyTabContent(getBaseContext()));
        tabHost.addTab(tSpecHero);
    }

    private void setPreferences() {

        String firstTime = "firstTime";

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean(firstTime, true)) {
            prefs.edit().putBoolean(firstTime, false);
            prefs.edit().commit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);

        //To make it simple, always re-load Preference setting.
        setPreferences();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);

        if (getResources().getBoolean(R.bool.portrait_only)) {
            //do not rotate for phones
            //noinspection ResourceType
            setRequestedOrientation(Configuration.ORIENTATION_PORTRAIT);
        } else {

            //use layout for tablets - landscape or portrait.
            //save fragments for re-use in new layout!

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            CategoryList attackFragment = (CategoryList) fragmentManager.findFragmentByTag(
                    getResources().getString(R.string.items_attack));
            CategoryList defenseFragment = (CategoryList) fragmentManager.findFragmentByTag(
                    getResources().getString(R.string.items_defense));
            HeroList heroFragment = (HeroList) fragmentManager.findFragmentByTag(
                    getResources().getString(R.string.items_hero));

            fragmentTransaction.detach(attackFragment);
            fragmentTransaction.detach(defenseFragment);
            fragmentTransaction.detach(heroFragment);

            fragmentTransaction.commitAllowingStateLoss();
            fragmentManager.executePendingTransactions();

            //load appropriate layout
            Log.v(getClass().getCanonicalName(), "re-load layout for tablets");
            setContentView(R.layout.main);

            Log.v(getClass().getCanonicalName(), "re-use fragments");
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.attach(defenseFragment);
            fragmentTransaction.attach(attackFragment);
            fragmentTransaction.attach(heroFragment);

            fragmentTransaction.commitAllowingStateLoss();
            fragmentManager.executePendingTransactions();
        }
    }

    private void setupSound() {
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                soundsLoaded.add(new Integer(sampleId));
                Log.v(TAG, "soundSample loaded with id " + sampleId);
            }
        });
        soundIDs = new int[3];
        soundIDs[0] = soundPool.load(this, R.raw.roll_1, 1);
        soundIDs[1] = soundPool.load(this, R.raw.roll_2, 1);
        soundIDs[2] = soundPool.load(this, R.raw.roll_3, 1);
    }

    public void playSound() {
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        float actualVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        float maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float volume = actualVolume / maxVolume;

        int soundIdxToPlay;
        try {
            soundIdxToPlay = RandomnessProvider.getLocalRandomness().nextInt(3);
            if (soundsLoaded.contains(new Integer(soundIDs[soundIdxToPlay]))) {
                soundPool.play(soundIDs[soundIdxToPlay], volume, volume, 1, 0, 1f);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        //noinspection ResourceType
        inflater.inflate(R.layout.optionsmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.manual:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.manual_title).setMessage(R.string.manual)
                        .setPositiveButton(R.string.button_ok,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                }).show();
                return true;

            case R.id.preferences:
                //fragments only for 3.0 and above
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, IS_VERSION_SMALLER_THAN_HONEYCOMB ?
                        PreferencesActivity.class : SetPreferencesFragmentActivity.class);
                startActivityForResult(intent, 0);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
