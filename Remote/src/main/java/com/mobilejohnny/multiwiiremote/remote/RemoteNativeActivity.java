package com.mobilejohnny.multiwiiremote.remote;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.util.Log;
import android.view.InputDevice;
import android.widget.*;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import org.xml.sax.InputSource;

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
    private float scaleYaw;
    private JoystickView joyStick;
    private ProgressBarView progressBarThrottle;
    private TextView txtAUX2;
    private TextView txtAUX3;
    private TextView txtAUX4;

    private float lockGen;
    private long lastARMTime;
    private TextView txtVbat;
    private boolean isSet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_remote_native);

        txtThrottle = (TextView)findViewById(R.id.txtThrottle);
        txtRoll = (TextView)findViewById(R.id.txtRoll);
        txtPitch = (TextView)findViewById(R.id.txtPitch);
        txtYaw = (TextView)findViewById(R.id.txtYaw);
        txtAUX1 = (TextView)findViewById(R.id.txtAUX1);
        txtAUX2 = (TextView)findViewById(R.id.txtAUX2);
        txtAUX3 = (TextView)findViewById(R.id.txtAUX3);
        txtAUX4 = (TextView)findViewById(R.id.txtAUX4);
        txtFPS = (TextView)findViewById(R.id.txtFPS);
        joyStick = (JoystickView)findViewById(R.id.joyStick);
        progressBarThrottle = (ProgressBarView)findViewById(R.id.progressBarThrottle);
        txtVbat = (TextView)findViewById(R.id.txtVbat);

        switchArm = (Switch)findViewById(R.id.switchArm);
        switchArm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    arm(b);
            }
        });

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                halfWidth = decorView.getWidth() / 2;
                mult = decorView.getHeight() / 540f;
                lockGen = mult * 100;
                scaleYaw = halfWidth / 960f;
            }
        });

        decorView.setOnTouchListener(new View.OnTouchListener() {
            public float[] lastY = new float[2];

            float middleX = 0;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int action = motionEvent.getAction() & MotionEvent.ACTION_MASK;
                int index = (motionEvent.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;


//               if(action!=MotionEvent.ACTION_MOVE)
//                Log.i("RemoteNative",action+" "+index+" "+motionEvent.getPointerCount());

                if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
                    float x = 0, y = 0;
                    if (action == MotionEvent.ACTION_DOWN) {
                        x = motionEvent.getX();
                        y = motionEvent.getY();
                    } else if (action == MotionEvent.ACTION_POINTER_DOWN) {
                        int id = motionEvent.getPointerId(index);
                        x = motionEvent.getX(id);
                        y = motionEvent.getY(id);
                    }
                    if (x < halfWidth) {
                        lastY[0] = y;
                        if (altHoldEnable) {
                            rcAUX2 = minRC;
                        }
                        inputMode = INPUT_MODE_NONE;
                    } else {
                        lastY[1] = y;
                        middleX = x;

                        inputMode = INPUT_MODE_GRAVITY;
                    }

                } else if (action == MotionEvent.ACTION_MOVE) {
                    for (int i = 0; i < motionEvent.getPointerCount(); i++) {
                        if (motionEvent.getX(i) < halfWidth) {
                            float y = motionEvent.getY(i);
                            rcThrottle += (int) ((lastY[0] - y) / mult);

                            lastY[0] = y;
                            break;
                        } else {
                            float x = motionEvent.getX(i);
                            float rightX = ((x - middleX) / scaleYaw);

                            if (Math.abs(rightX) > 50) {
                                int tempX = (int) rightX;
                                if (rightX < 0) {
                                    tempX += 50;
                                } else {
                                    tempX -= 50;
                                }
                                rcYaw = medYawRC + tempX;
                            } else {
                                rcYaw = medYawRC;
                            }
                        }
                    }
                } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
                    float x = 0, y = 0;
                    if (action == MotionEvent.ACTION_UP) {
                        x = motionEvent.getX();
                        y = motionEvent.getY();
                    } else if (action == MotionEvent.ACTION_POINTER_UP) {
                        int id = motionEvent.getPointerId(index);
                        try {
                            x = motionEvent.getX(id);
                            y = motionEvent.getY(id);
                        } catch (Exception e) {
                            Log.e(getClass().getSimpleName(), e.toString());
                        }
                    }

                    if (x < halfWidth) {//左侧
                        if (altHoldEnable &&
                                rcThrottle > 1300) {
                            rcAUX2 = maxRC;
                        }
                    } else {//右侧
                        if (lastY[1] - y > lockGen) {
                            medRollRC = (int) (map(rotationY, minY, maxY, minRC, maxRC));
                            medPitchRC = (int) (map(rotationX, minX, maxX, maxRC, minRC));
                        }

                        inputMode = INPUT_MODE_NONE;
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
        txtAUX2.setText(rcAUX2+"");
        txtAUX3.setText(rcAUX3+"");
        txtAUX4.setText(rcAUX4+"");

        if(switchArm.isChecked()&& rcAUX1 == minRC)
        {
            switchArm.setChecked(false);
        }
        else if(switchArm.isChecked() == false && rcAUX1 == maxRC)
        {
            switchArm.setChecked(true);
        }

        txtVbat.setText((vbat / 10.0) + "v");
        txtVbat.setTextColor(vbat < 109 ? Color.WHITE : Color.BLACK);
        txtVbat.setBackgroundColor(vbat < 109 ? Color.RED:Color.GREEN);

        joyStick.setPadPosition(Math.round(map(rcRoll,minRC,maxRC,0,100)),Math.round(map(rcPitch,minRC,maxRC,100,0)));
        progressBarThrottle.setValue(Math.round(map(rcThrottle,minThrottleRC,maxThrottleRC,0,100)));

        long currentTime = System.currentTimeMillis();
        long dur = currentTime - time;

        fps = (int) (1000.0 / (float)dur);
        txtFPS.setText(fps+"");

        time = currentTime;
    }


}
