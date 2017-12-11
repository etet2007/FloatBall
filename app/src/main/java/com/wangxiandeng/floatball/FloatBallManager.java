package com.wangxiandeng.floatball;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

/**
 * 管理WindowManager BallView的类
 * Created by wangxiandeng on 2016/11/25.
 */

public class FloatBallManager {
    //View
    private static MyFloatBallView mBallView;
    //WindowManager
    private static WindowManager mWindowManager;


    public static void addBallView(Context context) {
        if (mBallView == null) {
//          Get screenWidth screenHeight
            WindowManager windowManager = getWindowManager(context);
            Point size = new Point();
            windowManager.getDefaultDisplay().getSize(size);
            int screenWidth = size.x;
            int screenHeight = size.y;

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

            //初始化FloatBallView
            mBallView = new MyFloatBallView(context);
            //初始化LayoutParams
            LayoutParams params = new LayoutParams();
            params.x=prefs.getInt("paramsX",screenWidth / 2);
            params.y=prefs.getInt("paramsY",screenHeight / 2);

//            params.x = screenWidth / 2;
//            params.y = screenHeight / 2;
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
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = prefs.edit();

            //记录LayoutParams
            LayoutParams params = mBallView.getLayoutParams();
            editor.putInt("paramsX",params.x);
            editor.putInt("paramsY",params.y);
            editor.apply();

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
    public static void setOpacity(Context context,int opacity) {
        if (mBallView != null) {
            mBallView.setOpacity(opacity);
            mBallView.invalidate();
        }
    }
    public static void setSize(Context context,int size) {
        if (mBallView != null) {
            mBallView.changeFloatBallSizeWithRadius(size);
            mBallView.invalidate();
        }

    }
    public static void setBackgroundPic(Context context,String imagePath){
        if (mBallView != null) {
            mBallView.makeBackgroundBitmap(imagePath);
            mBallView.invalidate();
        }
    }
}

