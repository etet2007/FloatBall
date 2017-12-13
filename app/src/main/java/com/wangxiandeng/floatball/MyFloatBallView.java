package com.wangxiandeng.floatball;

import android.accessibilityservice.AccessibilityService;
import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Vibrator;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

/**
 * Created by stephenlau on 2017/12/5.
 */

public class MyFloatBallView extends View {
    public static final String TAG="lqt";

    private final int ballMoveDistance = 18;
    private Paint mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mBallPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean isFirstEvent=false;

    public float getBallCenterY() {
        return ballCenterY;
    }
    public void setBallCenterY(float ballCenterY) {
        this.ballCenterY = ballCenterY;
    }

    private float ballCenterX=0;
    private float ballCenterY=0;

    public float getBallCenterX() {
        return ballCenterX;
    }
    public void setBallCenterX(float ballCenterX) {
        this.ballCenterX = ballCenterX;
    }

    private boolean isScrolling=false;

    private float ballRadius=25;
    private float mBackgroundRadius=ballRadius+15;

    //MyFloatBallView宽高
    private int measuredWidth= (int) (mBackgroundRadius*2+20);
    private int measuredHeight=measuredWidth;

    public void changeFloatBallSizeWithRadius(int ballRadius){
        this.ballRadius=ballRadius;
        mBackgroundRadius=ballRadius+15;
        //View宽高
        measuredWidth= (int) (mBackgroundRadius*2+20);
        measuredHeight=measuredWidth;
        makeAnimator();
        createBitmapCrop();
        requestLayout();

    }
    private GESTURE_STATE currentGestureSTATE;
    public enum GESTURE_STATE {
        UP, DOWN, LEFT, RIGHT,NONE
    }

    private boolean isLongPress=false;
    private float mLastTouchEventX;
    private float mOffsetToParentY;
    private WindowManager.LayoutParams mLayoutParams;
    private int mStatusBarHeight;

    private GESTURE_STATE lastGestureSTATE = GESTURE_STATE.NONE;

    private GestureDetectorCompat mDetector;
    private AccessibilityService mService;

    private WindowManager mWindowManager;
    private ObjectAnimator onTouchAnimate;
    private ObjectAnimator unTouchAnimate;
    //Vibrator
    private Vibrator mVibrator;
    private long[] mPattern = {0, 100};

    private Bitmap bitmapRead;
    private Bitmap bitmapCrop;

    public void setLayoutParams(WindowManager.LayoutParams params) {
        mLayoutParams = params;
    }
    public  WindowManager.LayoutParams getLayoutParams() {
        return mLayoutParams;
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
    public int getOpacity(){
        return mBackgroundPaint.getAlpha();
    }


    public MyFloatBallView(Context context) {
        super(context);
        mService = (AccessibilityService) context;
        mWindowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        mDetector=new GestureDetectorCompat(context,new MyGestureListener());
        mVibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);

        mBackgroundPaint.setColor(Color.GRAY);
        mBackgroundPaint.setAlpha(80);

        mBallPaint.setColor(Color.WHITE);
        mBallPaint.setAlpha(150);

        //生成动画
        makeAnimator();

        getStatusBarHeight();

        //生成背景图，预设值 和 读取文件值
//        makeBackgroundBitmap(null);

    }

    public void makeBackgroundBitmap(String imagePath) {
        if(imagePath!=null) {
            bitmapRead = BitmapFactory.decodeFile(imagePath);
        }
        //读取不成功就取默认图片
        if(bitmapRead==null){
            Resources res=getResources();
            bitmapRead = BitmapFactory.decodeResource(res, R.drawable.joe_big);
        }
        String path = getContext().getFilesDir().toString();
        File file = new File(path, "ballBackground.png");
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            boolean isSucceed=bitmapRead.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            Log.d(TAG, "makeBackgroundBitmap: isSecceed:"+isSucceed);
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        createBitmapCrop();
    }

    private void createBitmapCrop() {
        int width=(int)ballRadius*2;
        int height=(int)ballRadius*2;
        if(bitmapRead==null){
            return;
        }
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmapRead, width, height, true);

        bitmapCrop = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmapCrop);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

//      免得再算一次  canvas.drawCircle(width/2, height/2, ballRadius, paint);
        canvas.drawCircle(ballRadius, ballRadius, ballRadius, paint);
        paint.reset();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(scaledBitmap, 0, 0, paint);
    }

    private void makeAnimator() {
        Keyframe kf0 = Keyframe.ofFloat(0f, ballRadius);
        Keyframe kf1 = Keyframe.ofFloat(.7f, ballRadius+6);
        Keyframe kf2 = Keyframe.ofFloat(1f, ballRadius+7);
        PropertyValuesHolder onTouch = PropertyValuesHolder.ofKeyframe("ballRadius", kf0,kf1,kf2);
        onTouchAnimate = ObjectAnimator.ofPropertyValuesHolder(this, onTouch);
        onTouchAnimate.setDuration(300);
        onTouchAnimate.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                invalidate();
            }
        });

        Keyframe kf3 = Keyframe.ofFloat(0f, ballRadius+7);
        Keyframe kf4 = Keyframe.ofFloat(0.3f, ballRadius+7);
        Keyframe kf5 = Keyframe.ofFloat(1f, ballRadius);
        PropertyValuesHolder unTouch = PropertyValuesHolder.ofKeyframe("ballRadius", kf3,kf4,kf5);
        unTouchAnimate = ObjectAnimator.ofPropertyValuesHolder(this, unTouch);
        unTouchAnimate.setDuration(400);
        unTouchAnimate.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                invalidate();
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.translate(measuredWidth/2,measuredHeight/2);
        canvas.drawCircle(0, 0, mBackgroundRadius, mBackgroundPaint);
        canvas.drawCircle(ballCenterX, ballCenterY, ballRadius, mBallPaint);

        canvas.drawBitmap(bitmapCrop,-bitmapCrop.getWidth()/2+ballCenterX,-bitmapCrop.getHeight()/2+ballCenterY,mBallPaint);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.mDetector.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //球放大动画
                onTouchAnimate.start();
            case MotionEvent.ACTION_MOVE:
                //移动模式中
                if (isLongPress){
                    //getX()、getY()返回的则是触摸点相对于View的位置。
                    //getRawX()、getRawY()返回的是触摸点相对于屏幕的位置
                    if(!isFirstEvent){
                        isFirstEvent=true;
                        mLastTouchEventX = event.getX();
                        mOffsetToParentY = event.getY() + mStatusBarHeight;
                    }
                    mLayoutParams.x = (int) (event.getRawX() - mLastTouchEventX);
                    mLayoutParams.y = (int) (event.getRawY() - mOffsetToParentY);

                    mWindowManager.updateViewLayout(MyFloatBallView.this, mLayoutParams);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                //球缩小动画
                unTouchAnimate.start();
                if(isScrolling){
                    doGesture();
                    //球移动动画
                    moveFloatBallBack();
                    currentGestureSTATE = GESTURE_STATE.NONE;
                    lastGestureSTATE = GESTURE_STATE.NONE;
                    isScrolling=false;
                }
                isLongPress=false;
                isFirstEvent=false;
                break;
        }
        return true;
    }

    private void doGesture() {
        switch (currentGestureSTATE) {
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

            if (angle > -Math.PI/4 && angle < Math.PI/4) {
                currentGestureSTATE = GESTURE_STATE.RIGHT;
            } else if (angle > Math.PI/4 && angle < Math.PI*3/4) {
                currentGestureSTATE = GESTURE_STATE.DOWN;
            } else  if (angle > -Math.PI*3/4 && angle < -Math.PI/4) {
                currentGestureSTATE = GESTURE_STATE.UP;
            }
              else{
                currentGestureSTATE = GESTURE_STATE.LEFT;
            }
            if(currentGestureSTATE != lastGestureSTATE){
                moveFloatBall();
                lastGestureSTATE = currentGestureSTATE;
            }
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            mVibrator.vibrate(mPattern, -1);
            isLongPress=true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    }
    private void moveFloatBall() {
        switch (currentGestureSTATE){
            case UP:
                ballCenterX=0;
                ballCenterY=-ballMoveDistance;
                break;
            case DOWN:
                ballCenterY= ballMoveDistance;
                ballCenterX=0;
                break;
            case LEFT:
                ballCenterX=-ballMoveDistance;
                ballCenterY=0;
                break;
            case RIGHT:
                ballCenterX= ballMoveDistance;
                ballCenterY=0;
                break;
            case NONE:
                ballCenterX=0;
                ballCenterY=0;
                break;
        }

        invalidate();
    }

    private void moveFloatBallBack() {
        PropertyValuesHolder pvh1=PropertyValuesHolder.ofFloat("ballCenterX",0);
        PropertyValuesHolder pvh2=PropertyValuesHolder.ofFloat("ballCenterY",0);
        ObjectAnimator.ofPropertyValuesHolder(this, pvh1, pvh2).setDuration(300).start();
    }

    /**
     * 获取通知栏高度
     * @return
     */
    private void getStatusBarHeight() {
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object o = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = (Integer) field.get(o);
            mStatusBarHeight = getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int dip2px(float dip) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dip, getContext().getResources().getDisplayMetrics()
        );
    }
}
