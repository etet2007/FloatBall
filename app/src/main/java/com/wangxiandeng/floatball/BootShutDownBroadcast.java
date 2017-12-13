package com.wangxiandeng.floatball;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by stephenlau on 2017/12/12.
 */

public class BootShutDownBroadcast extends BroadcastReceiver {
    private static final String TAG = "BootShutDownBroadcast";

    private static final String ACTION_SHUTDOWN = "android.intent.action.ACTION_SHUTDOWN";
    private static final String ACTION_BOOT = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Shut down this system, ShutdownBroadcastReceiver onReceive()");
        if (intent.getAction() == null) {
            return;
        }
        if (intent.getAction().equals(ACTION_SHUTDOWN)) {
            Log.d(TAG, "ACTION_SHUTDOWN");
            Toast.makeText(context, "ACTION_SHUTDOWN", Toast.LENGTH_LONG).show();

            Intent intent2 = new Intent(context, FloatBallService.class);
            Bundle data = new Bundle();
            data.putInt("type", FloatBallService.TYPE_DEL);
            intent2.putExtras(data);
            context.startService(intent2);

        }

        if (intent.getAction().equals(ACTION_BOOT)) {
            Log.d(TAG, "ACTION_BOOT");
            Toast.makeText(context, "ACTION_BOOT", Toast.LENGTH_LONG).show();

            Intent intent2 = new Intent(context, FloatBallService.class);
            Bundle data = new Bundle();
            data.putInt("type", FloatBallService.TYPE_ADD);
            intent2.putExtras(data);
            context.startService(intent2);

            Intent intent3 = new Intent(context, MainActivity.class);
            context.startActivity(intent3);

        }

    }
}
