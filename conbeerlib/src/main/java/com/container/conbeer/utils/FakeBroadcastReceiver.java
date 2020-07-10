package com.container.conbeer.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.container.conbeer.BuildConfig;

public class FakeBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        // an Intent broadcast.
        if(BuildConfig.DEBUG){
            Log.d("CONBEER", "***** INTENT RECEIVED ******:");
        }

        // Update sharedprefs
        SharedPreferences settings = context.getSharedPreferences("Prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("received", true);
        editor.apply();
    }
}
