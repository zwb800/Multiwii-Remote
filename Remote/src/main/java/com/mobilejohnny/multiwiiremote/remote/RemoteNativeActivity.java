package com.mobilejohnny.multiwiiremote.remote;

import android.content.pm.ActivityInfo;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

public class RemoteNativeActivity extends RemoteActivity {

    private long time;
    private TextView txtThrottle;
    private TextView txtRoll;
    private TextView txtPitch;
    private TextView txtYaw;
    private TextView txtAUX1;
    private TextView txtFPS;
    private Switch switchArm;
    private int halfWidth;
    private float mult;
    private JoystickView joyStick;
    private ProgressBarView progressBarThrottle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if(getRequestedOrientation()!= ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
            return;

        setContentView(R.layout.activity_remote_native);

        txtThrottle = (TextView)findViewById(R.id.txtThrottle);
        txtRoll = (TextView)findViewById(R.id.txtRoll);
        txtPitch = (TextView)findViewById(R.id.txtPitch);
        txtYaw = (TextView)findViewById(R.id.txtYaw);
        txtAUX1 = (TextView)findViewById(R.id.txtAUX1);
        txtFPS = (TextView)findViewById(R.id.txtFPS);
        joyStick = (JoystickView)findViewById(R.id.joyStick);
        progressBarThrottle = (ProgressBarView)findViewById(R.id.progressBarThrottle);

        switchArm = (Switch)findViewById(R.id.switchArm);
        switchArm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b)
                {
                    rcAUX1 = 2000;
                }
                else
                {
                    rcAUX1 = 1000;
                }

                rcThrottle = 1000;
                compoundButton.setText(b?"Armed":"Disarm");
            }
        });

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                halfWidth = decorView.getWidth() / 2;
                mult = 540 /(float) decorView.getHeight();
            }
        });

        decorView.setOnTouchListener(new View.OnTouchListener() {
            public float lastY;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int action = motionEvent.getAction() & MotionEvent.ACTION_MASK;
                int index = (motionEvent.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK )>> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
//               if(action!=MotionEvent.ACTION_MOVE)
//                Log.i("RemoteNative",action+" "+index+" "+motionEvent.getPointerCount());

                if(action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN)
                {
                    float x = 0,y=0;
                    if(action == MotionEvent.ACTION_DOWN) {
                        x = motionEvent.getX();
                        y = motionEvent.getY();
                    }
                    else if(action == MotionEvent.ACTION_POINTER_DOWN)
                    {
                        int id = motionEvent.getPointerId(index);
                        x = motionEvent.getX(id);
                        y = motionEvent.getY(id);
                    }
                    if (x < halfWidth) {
                        lastY = y;
                    } else {
                        unLock();
                    }
                }
                else if (action == MotionEvent.ACTION_MOVE ) {
                    for (int i=0;i<motionEvent.getPointerCount();i++)
                    {
                        if(motionEvent.getX(i) < halfWidth )
                        {
                            float y = motionEvent.getY(i);
                            rcThrottle += (int)((lastY - y) * mult);
                            rcThrottle = constrain(rcThrottle,1000,2000);
                            lastY = y;
                            break;
                        }
                    }
                }
                else if(action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP)
                {
                    float x = 0,y=0;
                    if(action == MotionEvent.ACTION_UP) {
                        x = motionEvent.getX();
                        y = motionEvent.getY();
                    }
                    else if(action == MotionEvent.ACTION_POINTER_UP)
                    {
                        int id = motionEvent.getPointerId(index);
                        x = motionEvent.getX(id);
                        y = motionEvent.getY(id);
                    }

                    if (x < halfWidth) {

                    } else {
                        lock();
                    }

                }
                return false;
            }
        });
    }



    @Override
    protected void updateUI() {

        txtThrottle.setText(rcThrottle+"");
        txtRoll.setText(rcRoll+"");
        txtPitch.setText(rcPitch+"");
        txtYaw.setText(rcYaw+"");
        txtAUX1.setText(rcAUX1+"");
        joyStick.setPadPosition(rcRoll - 1000,2000 - rcPitch);
        progressBarThrottle.setValue((rcThrottle - 1000));

        long currentTime = System.currentTimeMillis();
        long dur = currentTime - time;

        fps = (int) (1000.0 / (float)dur);
        txtFPS.setText(fps+"");

        time = currentTime;
    }


}
