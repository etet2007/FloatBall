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
    public static final int TYPE_SIZE =3;
    public static final int TYPE_IMAGE =4;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        FloatBallManager.addBallView(this);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    }
    @Override
    public void onInterrupt() {
        Log.d("lqt", "onInterrupt");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        FloatBallManager.saveFloatBallData();
    }

    //    Called by the system every time a client explicitly starts the service by calling startService(Intent),
// providing the arguments it supplied and a unique integer token representing the start request.
// Do not call this method directly.
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) {
            Bundle data = intent.getExtras();
            if (data != null) {
                int type = data.getInt("type");
                if (type == TYPE_ADD) {
                    //可以说在这里管理VIEW的生命周期了
                    FloatBallManager.addBallView(this);
                }
                if(type== TYPE_DEL){
                    FloatBallManager.saveFloatBallData();

                    FloatBallManager.removeBallView(this);

                }
                if(type==TYPE_OPACITY){
                    FloatBallManager.setOpacity(this,data.getInt("opacity"));

                }
                if (type == TYPE_SIZE) {
                    FloatBallManager.setSize(this,data.getInt("size"));

                }
                if (type == TYPE_IMAGE) {
                    FloatBallManager.setBackgroundPic(this,data.getString("imagePath"));
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
