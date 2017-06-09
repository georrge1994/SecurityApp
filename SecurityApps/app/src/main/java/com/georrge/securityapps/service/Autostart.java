package com.georrge.securityapps.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Георгий on 01.02.2017.
 */

public class Autostart extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent arg1) {
        Intent intent = new Intent(context,MonitorAppsService.class);
        context.startService(intent);
    }
}
