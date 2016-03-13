package com.XeliteXirish.bitcoinstatus.UI;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.XeliteXirish.bitcoinstatus.Preferences.Prefs;
import com.XeliteXirish.bitcoinstatus.UI.activities.MainActivity;
import com.XeliteXirish.shaun.bitcoinstatus.R;

public class BtcAmountDialog extends Dialog{

    Context context;
    MainActivity INSTANCE;
    EditText btcAmount;
    Button buttonSubmit;

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

        this.btcAmount = (EditText) findViewById(R.id.editTextBtcAmount);
        this.btcAmount.setText(Prefs.getBtcString(context));

        this.buttonSubmit = (Button) findViewById(R.id.buttonSubmit);
        this.buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Prefs.setBtc(context, btcAmount.getText().toString());
                INSTANCE.refresh();
                dismiss();
            }
        });
    }


}
