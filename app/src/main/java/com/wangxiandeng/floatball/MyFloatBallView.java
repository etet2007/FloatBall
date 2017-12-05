package com.wangxiandeng.floatball;

import android.accessibilityservice.AccessibilityService;
import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by stephenlau on 2017/12/5.
 */

public class MyFloatBallView extends View {
    public static final String TAG="lqt";
    private Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint ballPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float backgroundRadius=40;

    public float getBallRadius() {
        return ballRadius;
    }

    public void setBallRadius(float ballRadius) {
        this.ballRadius = ballRadius;
    }

    private float ballRadius=25;

    private GestureDetectorCompat mDetector;
    private AccessibilityService mService;

    private WindowManager mWindowManager;
    ObjectAnimator clickAnim;
    public MyFloatBallView(Context context) {
        super(context);
        mService = (AccessibilityService) context;
        mWindowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        mDetector=new GestureDetectorCompat(context,new MyGestureListener());


        backgroundPaint.setColor(Color.GRAY);
        backgroundPaint.setAlpha(80);

        ballPaint.setColor(Color.WHITE);
        ballPaint.setAlpha(150);


        Keyframe kf0 = Keyframe.ofFloat(0f, 25);
        Keyframe kf1 = Keyframe.ofFloat(.4f, 35);
        Keyframe kf2 = Keyframe.ofFloat(.6f, 35);
        Keyframe kf3 = Keyframe.ofFloat(1f, 25);
        PropertyValuesHolder click = PropertyValuesHolder.ofKeyframe("ballRadius", kf0, kf1,kf2,kf3);
        clickAnim = ObjectAnimator.ofPropertyValuesHolder(this, click);
        clickAnim.setDuration(500);
        clickAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                invalidate();
            }
        });

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawCircle(50, 50, backgroundRadius, backgroundPaint);
        canvas.drawCircle(50, 50, ballRadius, ballPaint);

//        Log.d("lqt", "onDraw: ");
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(100,100);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.mDetector.onTouchEvent(event);

//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                break;
//            case MotionEvent.ACTION_MOVE:
//                break;
//            case MotionEvent.ACTION_CANCEL:
//            case MotionEvent.ACTION_UP:
//                break;
//        }
        return true;
    }


    private class MyGestureListener implements GestureDetector.OnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {

        }

        //单击
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Log.d(TAG, "onSingleTapUp: ");
            AccessibilityUtil.doBack(mService);
            clickAnim.start();
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            Log.d(TAG, "onScroll: ");
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            Log.d(TAG, "onLongPress: ");
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    }
}
