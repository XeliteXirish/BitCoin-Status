package com.XeliteXirish.bitcoinstatus.UI.activities;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.XeliteXirish.bitcoinstatus.BitCoinStatus;
import com.XeliteXirish.bitcoinstatus.Preferences.Prefs;
import com.XeliteXirish.shaun.bitcoinstatus.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CurrencyPickerActivity extends AppCompatActivity {

    public static String TAG = BitCoinStatus.TAG;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_picker);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        CurrencyPickerFragment fragment = new CurrencyPickerFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayoutList, fragment).commit();

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    public static class CurrencyPickerFragment extends ListFragment {

        JSONObject jsonCurrencies;

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            this.getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);

            try {
                InputStream inputStream = getResources().openRawResource(R.raw.currencies);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line + "\n");
                }
                bufferedReader.close();

                jsonCurrencies = new JSONObject(stringBuilder.toString());
                JSONObject lookup = jsonCurrencies.getJSONObject("currencies");

                JSONArray order = jsonCurrencies.getJSONArray("order");
                String[] names = new String[order.length()];
                String selectedKey = Prefs.getCurrencyCode(getActivity());

                int selectedIndex = 0;
                for (int i = 0; i < order.length(); i++) {
                    String key = order.getString(i);
                    if (key.equals(selectedKey)) {
                        selectedIndex = i;
                    }
                    names[i] = lookup.getString(key);
                }

                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_checked, names);
                setListAdapter(arrayAdapter);
                getListView().setItemChecked(selectedIndex, true);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            super.onListItemClick(l, v, position, id);

            for (int i = 0; i < getListAdapter().getCount(); i++) {
                if (i != position) {
                    l.setItemChecked(i, false);
                }
            }

            try {
                JSONArray order = jsonCurrencies.getJSONArray("order");
                String code = order.getString(position);
                Prefs.setCurrencyCode(getActivity(), code);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }

            //finish();
        }
    }
}
