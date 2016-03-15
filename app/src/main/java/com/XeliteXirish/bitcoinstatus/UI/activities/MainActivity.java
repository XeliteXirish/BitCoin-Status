package com.XeliteXirish.bitcoinstatus.UI.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.XeliteXirish.bitcoinstatus.BitCoinStatus;
import com.XeliteXirish.bitcoinstatus.Preferences.Prefs;
import com.XeliteXirish.bitcoinstatus.UI.BtcAmountDialog;
import com.XeliteXirish.shaun.bitcoinstatus.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity{

    public String TAG = BitCoinStatus.TAG;

    FloatingActionButton fabCurrency;

    TextView textViewValue;
    TextView textViewBtc;
    TextView textViewLastUpdated;

    Boolean isUpdating = false;
    Timer timer;
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            updateLastUpdatedLabel();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fabCurrency = (FloatingActionButton) findViewById(R.id.fabCurrency);
        fabCurrency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CurrencyPickerActivity.class);
                startActivity(intent);
            }
        });

        textViewValue = (TextView) findViewById(R.id.textViewValue);

        textViewBtc = (TextView) findViewById(R.id.textViewBitCoinAmount);
        textViewBtc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BtcAmountDialog btcAmountDialog = new BtcAmountDialog(MainActivity.this, MainActivity.this);
                btcAmountDialog.show();
            }
        });

        textViewLastUpdated = (TextView) findViewById(R.id.textViewUpdate);
        textViewLastUpdated.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refresh();
            }
        });

        checkFirstRun();
    }

    @Override
    public void onResume() {
        super.onResume();

        refresh();

        if (timer == null) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    handler.obtainMessage(1).sendToTarget();
                }
            }, 1000, 1000);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void refresh() {
        if (isUpdating) {
            return;
        }

        if (isNetworkAvailable()) {
            isUpdating = true;
            updateInterface();

            GetConversionTask task = new GetConversionTask();
            task.execute();
        } else {
            updateInterface();
            Toast.makeText(this, R.string.no_network, Toast.LENGTH_LONG).show();
        }
    }

    public void updateInterface() {
        double btc = Prefs.getBtc(this);
        double rate = Prefs.getRate(this);
        double value = btc * rate;

        // Value
        String code = Prefs.getCurrencyCode(this);
        NumberFormat format = NumberFormat.getCurrencyInstance();
        format.setCurrency(Currency.getInstance(code));
        textViewValue.setText(format.format(value));

        // BTC
        format = NumberFormat.getNumberInstance();
        format.setMaximumFractionDigits(10);
        textViewBtc.setText(format.format(btc) + " BTC");

        // Updated at
        updateLastUpdatedLabel();
    }

    public void updateLastUpdatedLabel() {
        if (isUpdating) {
            textViewLastUpdated.setText(R.string.updating);
        } else {
            long timestamp = Prefs.getUpdatedAtTimestamp(this);
            if (timestamp == 0) {
                textViewLastUpdated.setText(R.string.updated_never);
            } else {
                long now = System.currentTimeMillis();
                if (now - timestamp < 11000) {
                    textViewLastUpdated.setText(R.string.updated_just_now);
                } else {
                    CharSequence timeAgoInWords = DateUtils.getRelativeTimeSpanString(timestamp, now, 0);
                    textViewLastUpdated.setText(String.format(getString(R.string.updated_format), timeAgoInWords));
                }
            }
        }
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();
    }

    public class GetConversionTask extends AsyncTask<Object, Void, Object> {
        public String JSON_URL = "https://coinbase.com/api/v1/currencies/exchange_rates";

        @Override
        public Void doInBackground(Object... arg0) {
            try {
                // Connect
                String json = getJSON(JSON_URL, 1500);
                if (json == null) {
                    return null;
                }
                JSONObject data = new JSONObject(json);

                // Put values into map
                Iterator<String> keys = data.keys();

                SharedPreferences preferences = getSharedPreferences(Prefs.CONVERSION_PREFERENCES_NAME, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();

                while (keys.hasNext()) {
                    String key = keys.next();
                    if (key.startsWith("btc_to_")) {
                        String value = data.getString(key);
                        key = key.replace("btc_to_", "").toUpperCase();
                        editor.putString(key, value);
                    }
                }

                editor.putLong(Prefs.KEY_UPDATED_AT, System.currentTimeMillis());
                editor.commit();
            }
            catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, e.getClass().getSimpleName() + " - " + e.getMessage());
            }

            return null;
        }

        @Override
        public void onPostExecute(Object object) {
            isUpdating = false;
            updateInterface();
        }

        public String getJSON(String url, int timeout) {
            try {
                URL u = new URL(url);
                HttpURLConnection c = (HttpURLConnection) u.openConnection();
                c.setRequestMethod("GET");
                c.setRequestProperty("Content-length", "0");
                c.setUseCaches(false);
                c.setAllowUserInteraction(false);
                c.setConnectTimeout(timeout);
                c.setReadTimeout(timeout);
                c.connect();
                int status = c.getResponseCode();

                switch (status) {
                    case 200:
                    case 201:
                        BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line+"\n");
                        }
                        br.close();
                        return sb.toString();
                }

            } catch (MalformedURLException e) {
                Log.e(TAG, e.getMessage());
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
            return null;
        }
    }

    public void checkFirstRun(){
        boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("isFirstRun", true);

        if(isFirstRun){
            Toast.makeText(this, "Click on the BTC text to change the amount of BTC converting", Toast.LENGTH_LONG).show();

            getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putBoolean("isFirstRun", false).apply();
        }
    }
}
