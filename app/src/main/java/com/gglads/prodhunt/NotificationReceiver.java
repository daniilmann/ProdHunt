package com.gglads.prodhunt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case Constants.TIME_UPDATE_ACTION:
                PHAPIHelper.updateTimeProducts(context);
                break;
        }
    }
}
