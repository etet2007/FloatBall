package com.wangxiandeng.floatball;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import static com.wangxiandeng.floatball.MyFloatBallView.TAG;

/**
 * 管理WindowManager BallView的类，使用Static的成员MyFloatBallView WindowManager
 * 应该改成单例吗？
 * Created by wangxiandeng on 2016/11/25.
 */

public class FloatBallManager {
    //View
    private static MyFloatBallView mBallView;
    //WindowManager
    private static WindowManager mWindowManager;
    private static SharedPreferences defaultSharedPreferences;


    public static void addBallView(Context context) {
        if (mBallView == null) {
            //初始化FloatBallView
            mBallView = new MyFloatBallView(context);
//          Get screenWidth screenHeight
            WindowManager windowManager = getWindowManager(context);
            Point size = new Point();
            windowManager.getDefaultDisplay().getSize(size);
            int screenWidth = size.x;
            int screenHeight = size.y;


            defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            //初始化LayoutParams
            LayoutParams params = new LayoutParams();
            params.x=defaultSharedPreferences.getInt("paramsX",screenWidth / 2);
            params.y=defaultSharedPreferences.getInt("paramsY",screenHeight / 2);


            params.width = LayoutParams.WRAP_CONTENT;
            params.height = LayoutParams.WRAP_CONTENT;
            params.gravity = Gravity.START | Gravity.TOP;
            params.type = LayoutParams.TYPE_PHONE;
            params.format = PixelFormat.RGBA_8888;
            params.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | LayoutParams.FLAG_NOT_FOCUSABLE;
            mBallView.setLayoutParams(params);
            windowManager.addView(mBallView, params);

            mBallView.setOpacity(defaultSharedPreferences.getInt("opacity",125));
            mBallView.changeFloatBallSizeWithRadius(defaultSharedPreferences.getInt("size",25));

            String path = Environment.getExternalStorageDirectory().toString();
            mBallView.makeBackgroundBitmap(path+"/ballBackground.png");
            SharedPreferences.Editor editor = defaultSharedPreferences.edit();
            editor.putBoolean("isOpenBall",true);
            editor.apply();
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
    public static void saveFloatBallData(){
        if(defaultSharedPreferences==null || mBallView==null){
            return;
        }

        SharedPreferences.Editor editor = defaultSharedPreferences.edit();

        //remove 才调用saveFloatBallData

        editor.putBoolean("isOpenBall",false);

        LayoutParams params = mBallView.getLayoutParams();
        editor.putInt("paramsX",params.x);
        editor.putInt("paramsY",params.y);

//        editor.putBoolean("isOpenBall", isOpenBall); // value to store
        editor.putInt("opacity",mBallView.getOpacity());
        editor.putInt("size", (int) mBallView.getBallRadius());
        editor.apply();
    }
}

