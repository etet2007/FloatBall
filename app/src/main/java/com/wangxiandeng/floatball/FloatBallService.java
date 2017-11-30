package com.wangxiandeng.floatball;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.Bundle;
import android.view.accessibility.AccessibilityEvent;

/**
 * Accessibility services should only be used to assist users with disabilities in using Android devices and apps.
 *  Such a service can optionally request the capability for querying the content of the active window.
 *  Development of an accessibility service requires extending this class and implementing its abstract methods.

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
