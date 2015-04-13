package com.mobilejohnny.multiwiiremote.remote;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by admin on 2015/4/6.
 */
public class ProgressBarView extends View {

    private Paint mPaint;
    private float value;
    private float a;
    private int padding;
    private Rect rectBar;

    public ProgressBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setTextSize(64);
        initBar();
    }

    private void initBar()
    {
        padding = 10;
        rectBar = new Rect(padding,padding,getWidth()-padding,getHeight() - padding);
         a = 1000 / (float)(getHeight() - padding - padding) ;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initBar();
    }

    public void setValue(int value)
    {
        value = Math.max(0,Math.min(value,1000));
        rectBar.top = (int) (getHeight() - padding - value / a);
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setColor(Color.GREEN);

        drawLineRect(0,0,getWidth(),getHeight(),mPaint,canvas);
        mPaint.setStyle(Paint.Style.FILL);

        canvas.drawRect(rectBar,mPaint);
    }

    private void drawLineRect(int left,int top,int width,int height,Paint mPaint,Canvas canvas)
    {

        mPaint.setStyle(Paint.Style.STROKE);
        Path path = new Path();
        path.lineTo(width,0);
        path.lineTo(width,height);
        path.lineTo(0,height);
        path.lineTo(0,0);
        path.close();

        canvas.drawPath(path,mPaint);
    }
}
