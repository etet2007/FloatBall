package com.wangxiandeng.floatball;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

/**
 * 管理WindowManager BallView的类
 * Created by wangxiandeng on 2016/11/25.
 */

public class FloatWindowManager {
    //View
    private static MyFloatBallView mBallView;
    //WindowManager
    private static WindowManager mWindowManager;


    public static void addBallView(Context context) {
        if (mBallView == null) {
//            screenWidth screenHeight
            //WindowManager: The interface that apps use to talk to the window manager.
            WindowManager windowManager = getWindowManager(context);
            Point size = new Point();
            windowManager.getDefaultDisplay().getSize(size);
            int screenWidth = size.x;
            int screenHeight = size.y;
            //初始化FloatBallView
            mBallView = new MyFloatBallView(context);

            LayoutParams params = new LayoutParams();
            params.x = screenWidth / 2;
            params.y = screenHeight / 2;
            params.width = WindowManager.LayoutParams.WRAP_CONTENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.gravity = Gravity.START | Gravity.TOP;
            params.type = LayoutParams.TYPE_PHONE;
            params.format = PixelFormat.RGBA_8888;
            params.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | LayoutParams.FLAG_NOT_FOCUSABLE;
            mBallView.setLayoutParams(params);
            windowManager.addView(mBallView, params);
        }
    }

    public static void removeBallView(Context context) {
        if (mBallView != null) {
            WindowManager windowManager = getWindowManager(context);
            windowManager.removeView(mBallView);
            mBallView = null;
        }
    }

    private static WindowManager getWindowManager(Context context) {
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }
        return mWindowManager;
    }

}
