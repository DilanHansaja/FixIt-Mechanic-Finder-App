package com.dilanhansaja.fixit.model;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.dilanhansaja.fixit.R;

public class FixItBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (Intent.ACTION_POWER_CONNECTED.equals(intent.getAction())) {
            Log.d("FixItLog", "Device is charging");

            MyAlert myAlert = MyAlert.getMyAlert();

            myAlert.showOkAlert(context,
                    "Charging Detected",
                    "Your device is now charging while using location services. This may cause battery drain and increased device temperature."
                    , R.drawable.alert_warning
            );

        }
    }
}
