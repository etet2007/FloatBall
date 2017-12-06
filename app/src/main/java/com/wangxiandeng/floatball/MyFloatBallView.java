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
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;

import java.lang.reflect.Field;

/**
 * Created by stephenlau on 2017/12/5.
 */

public class MyFloatBallView extends View {
    public static final String TAG="lqt";
    private Paint mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mBallPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float ballCenterY=50;
    private float ballCenterX=50;
    private boolean isScrolling=false;
    private GESTURE currentGesture;

    public enum GESTURE {

        UP, DOWN, LEFT, RIGHT,NONE

    }

    private float mBackgroundRadius=40;
    private float ballRadius=25;

    private boolean isLongPress=false;
    private int mOffsetToParent;
    private int mOffsetToParentY;
    private WindowManager.LayoutParams mLayoutParams;
    private int mStatusBarHeight;

    private int mTouchSlop;

    private GESTURE lastGesture=GESTURE.NONE;

    private GestureDetectorCompat mDetector;
    private AccessibilityService mService;

    private WindowManager mWindowManager;
    private ObjectAnimator onTouchAnimat;
    private ObjectAnimator unTouchAnimat;

//    private float firstScrollX;
//    private float firstScrollY;


    public void setLayoutParams(WindowManager.LayoutParams params) {
        mLayoutParams = params;
    }

    public float getBallRadius() {
        return ballRadius;
    }

    public void setBallRadius(float ballRadius) {
        this.ballRadius = ballRadius;
    }
    public float getmBackgroundRadius() {
        return mBackgroundRadius;
    }

    public void setmBackgroundRadius(float mBackgroundRadius) {
        this.mBackgroundRadius = mBackgroundRadius;
    }

    public void setOpacity(int opacity){
        mBackgroundPaint.setAlpha(opacity);
        mBallPaint.setAlpha(opacity);
    }
    public MyFloatBallView(Context context) {
        super(context);
        mService = (AccessibilityService) context;
        mWindowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        mDetector=new GestureDetectorCompat(context,new MyGestureListener());

        mBackgroundPaint.setColor(Color.GRAY);
        mBackgroundPaint.setAlpha(80);

        mBallPaint.setColor(Color.WHITE);
        mBallPaint.setAlpha(150);

        //生成动画
        Keyframe kf0 = Keyframe.ofFloat(0f, ballRadius);
        Keyframe kf1 = Keyframe.ofFloat(.7f, ballRadius+7);
        Keyframe kf2 = Keyframe.ofFloat(1f, ballRadius+8);
        PropertyValuesHolder onTouch = PropertyValuesHolder.ofKeyframe("ballRadius", kf0,kf1,kf2);
        onTouchAnimat = ObjectAnimator.ofPropertyValuesHolder(this, onTouch);
        onTouchAnimat.setDuration(300);
        onTouchAnimat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                invalidate();
            }
        });


        Keyframe kf3 = Keyframe.ofFloat(0f, ballRadius+8);
        Keyframe kf4 = Keyframe.ofFloat(0.3f, ballRadius+8);
        Keyframe kf5 = Keyframe.ofFloat(1f, ballRadius);
        PropertyValuesHolder unTouch = PropertyValuesHolder.ofKeyframe("ballRadius", kf3,kf4,kf5);
        unTouchAnimat = ObjectAnimator.ofPropertyValuesHolder(this, unTouch);
        unTouchAnimat.setDuration(400);
        unTouchAnimat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                invalidate();
            }
        });

        mStatusBarHeight = getStatusBarHeight();
        mOffsetToParent = dip2px(mBackgroundRadius /2);
        mOffsetToParentY = mStatusBarHeight + mOffsetToParent;

        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawCircle(50, 50, mBackgroundRadius, mBackgroundPaint);
        canvas.drawCircle(ballCenterX, ballCenterY, ballRadius, mBallPaint);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(100,100);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.mDetector.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "onTouchEvent: Down");
                onTouchAnimat.start();
            case MotionEvent.ACTION_MOVE:
                //移动模式中
                if (isLongPress){
                    mLayoutParams.x = (int) (event.getRawX() - mOffsetToParent);
                    mLayoutParams.y = (int) (event.getRawY() - mOffsetToParentY);
                    mWindowManager.updateViewLayout(MyFloatBallView.this, mLayoutParams);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "onTouchEvent: up");
                unTouchAnimat.start();
                if(isScrolling){
                    doGesture();
                    currentGesture=GESTURE.NONE;
                    lastGesture=GESTURE.NONE;
                    moveFloatBall();
                    isScrolling=false;
                }
                isLongPress=false;
                break;
        }
        return true;
    }

    private void doGesture() {
        switch (currentGesture) {
            case UP:
                AccessibilityUtil.doPullUp(mService);
                break;
            case DOWN:
                AccessibilityUtil.doPullDown(mService);
                break;
            case LEFT:
                AccessibilityUtil.doLeftOrRight(mService);
                break;
            case RIGHT:
                AccessibilityUtil.doLeftOrRight(mService);
                break;
            case NONE:
                break;
        }
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
//            Log.d(TAG, "onSingleTapUp: ");
            AccessibilityUtil.doBack(mService);
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            isScrolling=true;
//            Log.d(TAG, "onScroll: distanceX:"+distanceX+" distanceY"+distanceY);
//            Log.d(TAG, "onScroll: e1:"+e1.toString()+" e2:"+e2.toString());
            float firstScrollX = e1.getX();
            float firstScrollY = e1.getY();

            float lastScrollX = e2.getX();
            float lastScrollY = e2.getY();

            float deltaX = lastScrollX - firstScrollX;
            float deltaY = lastScrollY - firstScrollY;

            double angle = Math.atan2(deltaY, deltaX);
//            Log.d(TAG, "onScroll: angle:"+angle);

            if (angle > -Math.PI/4 && angle < Math.PI/4) {
                currentGesture = GESTURE.RIGHT;
            } else if (angle > Math.PI/4 && angle < Math.PI*3/4) {
                currentGesture = GESTURE.DOWN;
            } else  if (angle > -Math.PI*3/4 && angle < -Math.PI/4) {
                currentGesture = GESTURE.UP;
            }
              else{
                currentGesture = GESTURE.LEFT;
            }
            Log.d(TAG, "onScroll: gesture:"+currentGesture);
            if(currentGesture!=lastGesture){
                moveFloatBall();
                lastGesture=currentGesture;
            }
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            Log.d(TAG, "onLongPress: ");
            isLongPress=true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            return false;
        }
    }
//应该使用动画，要用变量记下位置
    private void moveFloatBall() {
        switch (currentGesture){
            case UP:
                ballCenterX=50;
                ballCenterY=50-20;
                break;
            case DOWN:
                ballCenterY=50+20;
                ballCenterX=50;
                break;
            case LEFT:
                ballCenterX=50-20;
                ballCenterY=50;
                break;
            case RIGHT:
                ballCenterX=50+20;
                ballCenterY=50;
                break;
            case NONE:
                ballCenterX=50;
                ballCenterY=50;
                break;
        }
        invalidate();
    }

    /**
     * 获取通知栏高度
     *
     * @return
     */
    private int getStatusBarHeight() {
        int statusBarHeight = 0;
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object o = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = (Integer) field.get(o);
            statusBarHeight = getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statusBarHeight;
    }
    public int dip2px(float dip) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dip, getContext().getResources().getDisplayMetrics()
        );
    }
}
