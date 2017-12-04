package com.wangxiandeng.floatball;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.Bundle;
import android.view.accessibility.AccessibilityEvent;

/**
 * Accessibility services should only be used to assist users with disabilities in using Android devices and apps.
 *  Such a service can optionally随意地 request the capability能力 for querying the content of the active window.
 *
 * Created by wangxiandeng on 2016/11/25.
 */

public class FloatBallService extends AccessibilityService {
    public static final int TYPE_ADD = 0;
    public static final int TYPE_DEL = 1;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    }

    @Override
    public void onInterrupt() {

    }

//    Called by the system every time a client explicitly starts the service by calling startService(Intent),
// providing the arguments it supplied and a unique integer token representing the start request.
// Do not call this method directly.
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle data = intent.getExtras();
        if (data != null) {
            int type = data.getInt("type");
            if (type == TYPE_ADD) {
                FloatWindowManager.addBallView(this);
            } else {
                FloatWindowManager.removeBallView(this);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
