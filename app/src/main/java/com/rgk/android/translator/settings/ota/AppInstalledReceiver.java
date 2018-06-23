package com.rgk.android.translator.settings.ota;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.rgk.android.translator.utils.Logger;

public class AppInstalledReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        String action = intent.getAction();
        if (action != null && action.equals(Intent.ACTION_PACKAGE_ADDED)) {
            String packageName = intent.getData().getSchemeSpecificPart();
            if (packageName.equals("com.rgk.android.translator")) {
                startHomeActivity(context);
            }

        }
    }

    private void startHomeActivity(Context context) {
        Intent intent = new Intent();
        intent.setClassName("com.rgk.android.translator","com.rgk.android.translator.HomeActivity");
        context.startActivity(intent);
    }
}
