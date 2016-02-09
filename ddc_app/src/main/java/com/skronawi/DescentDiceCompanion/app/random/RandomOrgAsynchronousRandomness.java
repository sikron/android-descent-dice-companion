package com.skronawi.DescentDiceCompanion.app.random;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import com.skronawi.DescentDiceCompanion.app.main.MainActivity;
import com.skronawi.DescentDiceCompanion.lib.random.Randomness;

import org.apache.http.Header;

import java.nio.charset.Charset;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class RandomOrgAsynchronousRandomness implements Randomness {
    
    public static final String TAG = RandomOrgAsynchronousRandomness.class.getCanonicalName();

    private static final int QUEUE_SIZE = 200;
    private static final int LIMIT = 40;
    private static final int TIMEOUT = 10000;
    private static final String randomOrgUrl = "http://www.random.org/integers";

    private MainActivity main;
    private ArrayBlockingQueue<Integer> numberQueue;
    private boolean requestOngoing;

    public RandomOrgAsynchronousRandomness(MainActivity main) {
        this.main = main;
        numberQueue = new ArrayBlockingQueue<Integer>(QUEUE_SIZE);
    }

    @Override
    public int nextInt(int upper) throws Exception {

        if (numberQueue.size() <= LIMIT) {

            Log.v(TAG, "numberQueue.size is " + numberQueue.size() + ", so <= limit " + LIMIT +
                    ", so trying to fetch new " + QUEUE_SIZE);

            if (!requestOngoing) {

                if (isNetworkAvailable()) {

                    Log.v(TAG, "network is available");

                    SyncHttpClient client = new SyncHttpClient();
                    client.setTimeout(TIMEOUT);
                    RequestParams params = new RequestParams();
                    params.put("num", String.valueOf(QUEUE_SIZE - numberQueue.size()));
                    params.put("min", String.valueOf(0));
                    params.put("max", String.valueOf(5));
                    params.put("col", String.valueOf(1));
                    params.put("base", String.valueOf(10));
                    params.put("format", "plain");
                    params.put("rnd", "new");
                    client.get(randomOrgUrl, params, new AsyncHttpResponseHandler() {

                        @Override
                        public void onFinish() {
                            super.onFinish();
                            requestOngoing = false;
                        }

                        @Override
                        public void onStart() {
                            super.onStart();
                            requestOngoing = true;
                        }

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            Log.v(TAG, "fetching successfull");
                            addNumbers(new String(responseBody));
                            Log.v(TAG, "queue completely filled");
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            //erst, wenn wirklich keine zufallszahl mehr vorhanden ist, fehler in gui anzeigen lassen.
                            //das passiert dann, wenn die poll() aus-timed!
                            Log.e(TAG,
                                    "fetching failure, cause.getMessage : " + error.getMessage() + " , " +
                                            "message : " + error);
                        }

                    });
                    Log.v(TAG, "requested : " + AsyncHttpClient.getUrlWithQueryString(false,
                            randomOrgUrl, params));
                }
            } else {
                Log.v(TAG, "request already ongoing");
            }
        }

        Log.v(TAG, "polling numberQueue");
        Integer number = numberQueue.poll(TIMEOUT, TimeUnit.MILLISECONDS);
        if (number == null) {
            Log.e(TAG, "polling timed out");
            throw new Exception("numberQueue poll timed out without result");
        } else {
            Log.v(TAG, "got result from numberQueue");
            Log.v(TAG, "count of remaining numbers in numberQueue : " + numberQueue.size());
            return number;
        }
    }

    private void addNumbers(String numberString) {
        String[] numbers = numberString.split("[\\r\\n]+");
        for (String n : numbers) {
            //so even before addNumbers() is completely finished, there are numbers in the queue, which can be taken!!
            //so there are several tries for the request to be sent, which must be controlled by requestOngoing
            numberQueue.add(Integer.valueOf(n));
        }
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) main.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // if no network is available networkInfo will be null
        // otherwise check if we are connected
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }
}
