package com.skronawi.DescentDiceCompanion.app.shake;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import com.skronawi.DescentDiceCompanion.app.main.MainActivity;
import com.skronawi.DescentDiceCompanion.app.shake.ShakeDetector.OnShakeListener;

public class RealSensorHandling implements SensorHandling {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;


    public RealSensorHandling(MainActivity mainActivity, OnShakeListener onShakeListener) {

        mSensorManager = (SensorManager) mainActivity.getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector(onShakeListener);
    }


    @Override
    public void register() {
        mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
    }


    @Override
    public void unregister() {
        mSensorManager.unregisterListener(mShakeDetector);
    }

}
