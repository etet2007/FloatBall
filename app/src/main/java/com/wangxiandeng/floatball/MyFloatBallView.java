package com.wangxiandeng.floatball;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.view.View;

/**
 * Created by stephenlau on 2017/12/5.
 */

public class MyFloatBallView extends View {
    Paint backgroundPaint = new Paint();
    Paint ballPaint = new Paint();
    float backgroundRadius=40;
    float ballRadius=20;

    public MyFloatBallView(Context context) {
        super(context);

//        backgroundRadius=50;
        backgroundPaint.setColor(Color.WHITE);
        backgroundPaint.setAlpha(50);

        ballPaint.setColor(Color.WHITE);
        ballPaint.setAlpha(90);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 绘制一个圆
        canvas.drawCircle(300, 300, backgroundRadius, backgroundPaint);

        canvas.drawCircle(300, 300, ballRadius, ballPaint);
    }
}
