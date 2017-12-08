package com.wangxiandeng.floatball;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
    public static final int TYPE_OPACITY =2;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    }
    @Override
    public void onInterrupt() {
        Log.d("lqt", "onInterrupt");
    }
//    Called by the system every time a client explicitly starts the service by calling startService(Intent),
// providing the arguments it supplied and a unique integer token representing the start request.
// Do not call this method directly.d
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) {
            Bundle data = intent.getExtras();
            if (data != null) {
                int type = data.getInt("type");
                if (type == TYPE_ADD) {
                    FloatBallManager.addBallView(this);
                }
                if(type== TYPE_DEL){
                    FloatBallManager.removeBallView(this);
                }
                if(type==TYPE_OPACITY){
                    FloatBallManager.setOpacity(this,data.getInt("opacity"));
                }
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }
/*
public enum CropType {
        CIRCLE(1), RECTANGLE(2);

        private int mValue;

        CropType(int value) {
            this.mValue = value;
        }

        public int value() {
            return mValue;
        }

        public static CropType valueOf(int value) {
            switch (value) {
                case CropImageBorderView.CIRCLE:
                    return CIRCLE;
                case CropImageBorderView.RECTANGLE:
                    return RECTANGLE;
                default:
                    return null;
            }
        }
    }
 */

}
