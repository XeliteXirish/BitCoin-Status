package com.XeliteXirish.bitcoinstatus.UI;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.XeliteXirish.bitcoinstatus.Preferences.Prefs;
import com.XeliteXirish.bitcoinstatus.UI.activities.MainActivity;
import com.XeliteXirish.shaun.bitcoinstatus.R;

public class BtcAmountDialog extends Dialog{

    Context context;
    MainActivity INSTANCE;
    TextView textViewProgress;
    SeekBar seekBarAmount;
    Button buttonSubmit;

    Integer intSeekbarProgress;

    public BtcAmountDialog(Context context, MainActivity instance) {
        super(context);
        this.context = context;
        this.INSTANCE = instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.btc_amount_dialog);

        setTitle(R.string.btc_amount_dialog_title);
        intSeekbarProgress = Prefs.getBtcInt(context);

        this.seekBarAmount = (SeekBar) findViewById(R.id.seekBarAmount);
        this.seekBarAmount.setMax(20);
        this.seekBarAmount.setProgress(intSeekbarProgress);
        this.seekBarAmount.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                intSeekbarProgress = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                textViewProgress.setText("Covered: " + intSeekbarProgress + "/" + seekBar.getMax());
            }
        });

        this.textViewProgress = (TextView) findViewById(R.id.textViewViewProgress);
        textViewProgress.setText("Covered: " + intSeekbarProgress + "/" + seekBarAmount.getMax());



        this.buttonSubmit = (Button) findViewById(R.id.buttonSubmit);
        this.buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (intSeekbarProgress != null) {
                    Prefs.setBtc(context, intSeekbarProgress.toString());
                    INSTANCE.refresh();
                    dismiss();
                }
            }
        });
    }

    @Override
    public void setOnDismissListener(OnDismissListener listener) {
        super.setOnDismissListener(listener);

        Prefs.setBtc(context, intSeekbarProgress.toString());
        INSTANCE.refresh();
        dismiss();
    }
}
