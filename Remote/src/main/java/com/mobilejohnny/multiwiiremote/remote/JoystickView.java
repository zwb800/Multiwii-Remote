package com.mobilejohnny.multiwiiremote.remote;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by admin2 on 2015/3/14.
 */
public class JoystickView extends View {

    private Paint mPaint;
    private RectF padRectF;

    public JoystickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();

    }

    private void init()
    {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mPaint.setTextSize(64);

    }

    private void initPad() {
        int width = getWidth() / 4;
        int height = getHeight()  / 4;

        int left = (getWidth() - width)/ 2;
        int top = (getHeight() - height)/ 2;

        int right = left+ width;
        int bottom = top+ height;
        padRectF =  new RectF(left,top,right,bottom);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {


        if(event.getAction()==MotionEvent.ACTION_MOVE)
        {
            padRectF.left = event.getX();
            padRectF.top = event.getY();
            padRectF.set(padRectF.left,padRectF.top,padRectF.right,padRectF.bottom);
            invalidate();
            return true;
        }

        return super.onTouchEvent(event);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        setMeasuredDimension(Math.max(widthMeasureSpec,300), Math.max(widthMeasureSpec,300));
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initPad();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

//        canvas.drawRect(0,0,getWidth(),getHeight(),mPaint);
        mPaint.setColor(Color.DKGRAY);

        canvas.drawArc(new RectF(0,0,getWidth(),getHeight()),0,360,false,mPaint);

        mPaint.setColor(Color.GRAY);

        canvas.drawArc(padRectF,0,360,false,mPaint);
    }
}
