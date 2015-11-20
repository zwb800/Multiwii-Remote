package com.mobilejohnny.multiwiiremote.remote;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


public abstract class RemoteActivity extends ActionBarActivity {

    private static final int USB_OTG_RESULT_FAILED = 0;
    private static final int USB_OTG_RESULT_SUCCESS = 1;
    private static final int USB_OTG_RESULT_NO_PERMISSION = 2;
    protected static final int INPUT_MODE_TOUCH = 1;
    protected static final int INPUT_MODE_JOYSTICK = 2;

    // Sensor objects
    static SensorManager mSensorManager;
    static Sensor accelerometer;
    static Sensor magnetometer;
    static Sensor gravity;

    // Bluetooth manager
    static Bluetooth tBlue;
    static boolean sensorAvailable = false;
    static float azimuth       = 0.0f;
    static float pitch         = 0.0f;
    static float roll          = 0.0f;

    //Time control variables
    static int cycle = 0;   //Activity
    static int lastCycle = 0;
    static int cycleAcc = 0;  //Gravity
    static int lastCycleAcc = 0;
    static int cycleMag = 0;  //MagneticField
    static int lastCycleMag = 0;
    static int cycleGPS = 0;  //GlobalPosition
    static int lastCycleGPS = 0;



//    private static final byte[] MSP_HEADER_BYTE = MSP_HEADER.getBytes();
//    private static final int headerLength = MSP_HEADER_BYTE.length;

    static final int minRC = 1150, maxRC = 1850,medRC = 1500, minThrottleRC = 1000, maxThrottleRC = 2000;

    //, medRC = 1500;
    protected static int medRollRC = medRC,medPitchRC = medRC,medYawRC = medRC;

    static int servo[] = new int[8],
            rcThrottle = minThrottleRC,
            rcRoll = medRollRC, rcPitch = medPitchRC, rcYaw =medYawRC,
            rcAUX1=minRC, rcAUX2=minRC, rcAUX3=minRC, rcAUX4=minRC;
    private android.hardware.SensorEventListener sensorEventListener;
    private long lastSend;//遥控信号最后发送时间
    private long lastRequestAnalog;//电压请求最后发送时间

    //Sensor;
    static float rotationX=0, rotationY=0, rotationZ=0;
    static float minX=-1, maxX=1, minY=-1, maxY=1;
    static int horizonInstrSize = 100;

    protected int fps;
    private boolean unlock;
    private BroadcastReceiver BTReceiver;
    protected View decorView;
    private TCP tcp;
    private UDP udp;
    private UsbSerial usbSerial;
    private int port = 8080;
    private String device_name = null;
    private String host = "192.168.0.142";
    private int connect_type = -1;
    private static final int CONNECT_BLUETOOTH = 0;
    private static final int CONNECT_TCP = 1;
    private static final int CONNECT_UDP = 2;
    private static final int CONNECT_USBOTG = 3;
    protected boolean altHoldEnable;
    protected SharedPreferences preference;
    private UsbManager usbManager;
    public  static  final String USB_PERMISSION = "com.mobilejohnny.multiwiiremote.remote.USB_PERMISSION";
    private Receiver receiver;
    protected int vbat;//电压
    private boolean exit = false;
    protected int inputMode = INPUT_MODE_TOUCH;
    private float joyStickRoll,joyStickPitch,joyStickYaw,joyStickThrottle;
    private float minJoyStickRoll = -0.8f;
    private float maxJoyStickRoll = 0.8f;
    private float minJoyStickPitch = -0.8f;
    private float maxJoyStickPitch = 0.8f;
    private float minJoyStickYaw = -0.8f;
    private float maxJoyStickYaw = 0.8f;
    private float minJoyStickThrottle = -0.9f;
    private float maxJoyStickThrottle = 0.9f;
    private long lastARMTime;
    protected boolean enableGravity = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        decorView = getWindow().getDecorView();


        if(getRequestedOrientation()!=ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
            return;

        decorView.setKeepScreenOn(true);//保持屏幕常亮
        preference = PreferenceManager.getDefaultSharedPreferences(this);
        connect_type = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString("connection_type", "-1"));
        device_name =  preference.getString("device_name", "");
        host  = preference.getString("host", "");
        port = Integer.parseInt(preference.getString("port", "0"));

        medPitchRC = Integer.parseInt(preference.getString("middle_pitch", "1500"));
        medRollRC = Integer.parseInt(preference.getString("middle_roll", "1500"));
//        minJoyStickRoll = preference.getFloat("min_roll", minJoyStickRoll);
//        maxJoyStickRoll = preference.getFloat("max_roll", maxJoyStickRoll);
//        minJoyStickPitch = preference.getFloat("min_pitch", minJoyStickPitch);
//        maxJoyStickPitch = preference.getFloat("max_pitch", maxJoyStickPitch);
//        minJoyStickYaw = preference.getFloat("min_yaw", minJoyStickYaw);
//        maxJoyStickYaw = preference.getFloat("max_yaw", maxJoyStickYaw);
//        minJoyStickThrottle = preference.getFloat("min_throttle", minJoyStickThrottle);
//        maxJoyStickThrottle = preference.getFloat("max_throttle", maxJoyStickThrottle);

        Log.i("JoyStick","minRoll:"+minJoyStickRoll+" maxRoll:"+maxJoyStickRoll);

        altHoldEnable = preference.getBoolean("alt_hold", false);

        rcThrottle = minThrottleRC;
        rcAUX1 = minRC;
        rcAUX2 = minRC;
        rcAUX3 = minRC;
        rcAUX4 = minRC;

        BTReceiver = new BlueToothReceiver(this);

        this.registerReceiver(BTReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));
        this.registerReceiver(BTReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED));
        this.registerReceiver(BTReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));

        initSensor();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1){
            usbManager = (UsbManager) getSystemService(USB_SERVICE);
        }

        arm(false);

        tBlue = new Bluetooth(this,device_name);
        tcp = new TCP();
        udp = new UDP();
        connect();
        receiver = new Receiver();

        startThread();
    }

    protected void saveSetting()
    {
        if(preference!=null)
        {
            SharedPreferences.Editor editor = preference.edit();
            editor.putString("middle_pitch", medPitchRC + "");
            editor.putString("middle_roll",medRollRC+"");
            editor.putFloat("max_roll", maxJoyStickRoll);
            editor.putFloat("min_roll", minJoyStickRoll);
            editor.putFloat("max_pitch",maxJoyStickPitch);
            editor.putFloat("min_pitch",minJoyStickPitch);
            editor.putFloat("max_yaw",maxJoyStickYaw);
            editor.putFloat("min_yaw",minJoyStickYaw);
            editor.putFloat("max_throttle",maxJoyStickThrottle);
            editor.putFloat("min_throttle",minJoyStickThrottle);
            editor.commit();
        }

    }

    protected abstract void updateUI() ;


    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(USB_PERMISSION);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(receiver!=null) {
            unregisterReceiver(receiver);
        }
    }

    @Override
    protected void onDestroy() {

        exit =true;
        arm(false);
        sendRC();

        exitSensor();
        closeConnection();
        saveSetting();

        super.onDestroy();
    }

    private void closeConnection() {
        if (tBlue!= null)
            tBlue.close();
        if(tcp!=null)
            tcp.close();
        if(udp!=null)
            udp.close();
        if(BTReceiver!= null)
            unregisterReceiver(BTReceiver);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                decorView.setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }
        }

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        if(id == R.id.action_settings)
        {
            Intent intent =new Intent(this,SettingsActivity.class);
            startActivity(intent);
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    public void initSensor()
    {
        //Initiate instances
        sensorEventListener = new android.hardware.SensorEventListener(){

            private static final int azimutBuffer = 100;
            private float[] azimuts = new float [azimutBuffer];

            private float[] mGravity;
            private float[] mGeomagnetic;
            private float I[] = new float[16];
            private float R[] = new float[16];
            // Orientation values
            private float orientation[] = new float[3];
            private long millis;

            private  int az=0;

            private void addAzimut(float azimut){
                //for (az=azimutBuffer-1;az>0;az--)
                // azimuts[az] = azimuts[az-1];
                azimuts[az++] = azimut;
                if (az == azimutBuffer)
                    az = 0;
            }

            @Override
            public void onSensorChanged(SensorEvent event) {

                //if (event.accuracy == SensorManager.SENSOR_STATUS_ACCURACY_LOW) return;
                switch (event.sensor.getType()) {
                    case Sensor.TYPE_MAGNETIC_FIELD:
                        //mGeomagnetic = event.values.clone();
                        mGeomagnetic = lowPass( event.values.clone(), mGeomagnetic );
                        //mGeomagnetic = lowPass( event.values.clone(), mGeomagnetic );
                        //exponentialSmoothing( mGeomagnetic.clone(), mGeomagnetic, 0.5 );//
//                        cycleMag = millis - lastCycleMag;
//                        lastCycleMag = millis;
                        break;
                    case Sensor.TYPE_ACCELEROMETER:
                        //case Sensor.TYPE_GRAVITY:
                        //mGravity = event.values.clone();
                        mGravity = lowPass( event.values.clone(), mGravity );
                        //exponentialSmoothing( mGravity.clone(), mGravity, 0.2 );
//                        cycleAcc = millis - lastCycleAcc;
//                        lastCycleAcc = millis;
                        break;
                }

                if (mGravity != null && mGeomagnetic != null) {
                    //exponentialSmoothing( mGravity.clone(), mGravity, 0.2 );
                    //exponentialSmoothing( mGeomagnetic.clone(), mGeomagnetic, 0.5 );


                    I = new float[16];
                    R = new float[16];
                    if (SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic))
                    { // Got rotation matrix!
                        SensorManager.getOrientation(R, orientation);
                        //azimuth = orientation[0];
                        azimuth =  (float)Math.toDegrees(orientation[0]);
                        addAzimut(azimuth);
                        //Math.toDegrees(azimuthInRadians)+360)%360;
                        roll    =  -orientation[2];
                        pitch   = -orientation[1];
                        rotationX =  -orientation[2];
                        rotationY =  -orientation[1];
                    }
                }


            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }


            /*
           * time smoothing constant for low-pass filter 0 \u2264 alpha \u2264 1 ; a smaller
             * value basically means more smoothing See:
             * http://en.wikipedia.org/wiki/Low-pass_filter#Discrete-time_realization
             */
            private static final float ALPHA = 0.05f;

            /**
             * @see
             *      ://en.wikipedia.org/wiki/Low-pass_filter#Algorithmic_implementation
             * @see
             *      ://developer.android.com/reference/android/hardware/SensorEvent.html
             *      #values
             */


            protected float[] lowPass(float[] input, float[] output)
            {
                if (output == null)
                    return input;

                int inputLength = input.length;
                for (int i = 0; i < inputLength; i++)
                {
                    output[i] = output[i] + ALPHA * (input[i] - output[i]);
                }
                return output;
            }
        };
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer  = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        magnetometer   = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        sensorAvailable = true;

        mSensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(sensorEventListener, magnetometer, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(sensorEventListener, gravity, SensorManager.SENSOR_DELAY_FASTEST);
    }



    public void exitSensor()
    {
        if (sensorAvailable) mSensorManager.unregisterListener(sensorEventListener);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.i("Keyup", keyCode + "");
        if(keyCode == KeyEvent.KEYCODE_BUTTON_1)
        {
            arm(false);
            return true;
        }
        else if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            if(rcAUX1 == minRC) {//未解锁时才能退出
                finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((event.getSource() & InputDevice.SOURCE_KEYBOARD)
                == InputDevice.SOURCE_KEYBOARD) {
            if(event.getRepeatCount()==0)
            {
                Log.i("Keydown", keyCode + "");
                if (keyCode == KeyEvent.KEYCODE_BUTTON_1) {
                    arm(true);
                    return true;
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {

        if((event.getSource() & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK &&
                event.getAction() == MotionEvent.ACTION_MOVE)
        {
            inputMode = INPUT_MODE_JOYSTICK;
            int historySize = event.getHistorySize();
            //处理历史移动
            for (int i = 0; i < historySize; i++) {
                processMovement(event, i);
            }

            //处理及时移动
            processMovement(event, -1);
            return true;
        }

        return super.onGenericMotionEvent(event);
    }

    private void processMovement(MotionEvent event, int i) {
        joyStickRoll = getAxisValue(event,MotionEvent.AXIS_Z, i,true);
        joyStickPitch = getAxisValue(event,MotionEvent.AXIS_X, i,true);
        joyStickYaw = getAxisValue(event,MotionEvent.AXIS_RY, i,true);
        joyStickThrottle = getAxisValue(event, MotionEvent.AXIS_Y, i, false);

        minJoyStickRoll = getMaxValue(joyStickRoll, minJoyStickRoll);
        maxJoyStickRoll = getMaxValue(joyStickRoll, maxJoyStickRoll);
        minJoyStickPitch = getMaxValue(joyStickRoll, minJoyStickPitch);
        maxJoyStickPitch = getMaxValue(joyStickRoll,maxJoyStickPitch);
        minJoyStickYaw = getMaxValue(joyStickRoll, minJoyStickYaw);
        maxJoyStickYaw = getMaxValue(joyStickRoll,maxJoyStickYaw);
        minJoyStickThrottle = getMaxValue(joyStickRoll, minJoyStickThrottle);
        maxJoyStickThrottle = getMaxValue(joyStickRoll,maxJoyStickThrottle);

//        Log.i("Joystick", "roll:" + joyStickRoll + "pitch:" + joyStickPitch + " yaw:" + joyStickYaw + " throttle:" + joyStickThrottle);
//        Log.i("Joystick","minRoll:"+minJoyStickRoll+" maxRoll:"+maxJoyStickRoll);
    }

    private float getAxisValue(MotionEvent event,int axis, int historyIndex,boolean flat) {
        float value = historyIndex> -1 ? event.getHistoricalAxisValue(axis,historyIndex):event.getAxisValue(axis);
        InputDevice.MotionRange range = event.getDevice().getMotionRange(axis, event.getSource());

        if(flat) {
            if (Math.abs(value) < range.getFlat()) {
                return 0;
            }
            if(value>0) {
                value -= range.getFlat();
            }
            else
            {
                value += range.getFlat();
            }
        }

//        Log.i("flat",range.getFlat()+" "+" value:"+value);
        return value;
    }

    private float getMaxValue(float n,float ori)
    {
        float value = ori;
        if(ori > 0 && n >ori)
        {
            value = n;
        }
        else if(ori < 0 && n < ori)
        {
            value = n;
        }

        return value;
    }




    public void calculateRCValues() {
        if(inputMode == INPUT_MODE_TOUCH)
        {
            if(enableGravity) {
                rcRoll = Math.round(map(rotationY, minY, maxY, minRC, maxRC));
                rcPitch = Math.round(map(rotationX, minX, maxX, maxRC, minRC));
            }
            else
            {
                rcRoll = medRollRC;
                rcPitch = medPitchRC;
                rcYaw = medYawRC;
            }
        }
        else if(inputMode == INPUT_MODE_JOYSTICK)
        {
            float rollRange = getMinAbs(minJoyStickRoll, maxJoyStickRoll);
            float pitchRange = getMinAbs(minJoyStickPitch, maxJoyStickPitch);
            float yawRange = getMinAbs(minJoyStickYaw, maxJoyStickYaw);

            rcRoll = Math.round(map(joyStickRoll,rollRange*-1, rollRange, maxRC, minRC));
            rcPitch =  Math.round(map(joyStickPitch, pitchRange*-1, pitchRange, minRC, maxRC));
            rcYaw =  Math.round(map(joyStickYaw, yawRange*-1, yawRange,maxRC, minRC ));
            rcThrottle =  Math.round(map(joyStickThrottle, minJoyStickThrottle, maxJoyStickThrottle, maxThrottleRC, minThrottleRC));
        }

        rcThrottle = constrain(rcThrottle, minThrottleRC, maxThrottleRC);
        rcRoll = constrain(rcRoll, minRC, maxRC);
        rcPitch = constrain(rcPitch, minRC, maxRC);
        rcYaw= constrain(rcYaw, minRC, maxRC);
    }

    public float getMinAbs(float f1,float f2)
    {
        return Math.min(Math.abs(f1), Math.abs(f2));
    }

    protected int constrain(int val,int min,int max)
    {
        return Math.max(Math.min(val, max), min);
    }

    public static final float map(float value, float instart, float inend, float outstart, float outend) {
        return outstart + (outend - outstart) / (inend - instart) * (value - instart);
    }

    private  void sendRC() {
        send(MSP.getRCPocket(rcRoll, rcPitch, rcYaw, rcThrottle, rcAUX1, rcAUX2, rcAUX3, rcAUX4));
    }

    //发送电池状态请求
    protected void sendRequestAnalog()
    {
        send(MSP.getAnalogPocket());
    }

    public boolean arm(boolean b) {
        if(b && rcAUX1 == minRC)
        {
            if(rcThrottle>minThrottleRC)
            {
                Toast.makeText(RemoteActivity.this,"油门未至最低",Toast.LENGTH_SHORT).show();
                return false;
            }

            rcAUX1 = maxRC;
            lastARMTime = System.currentTimeMillis();
        }
        else if(b == false && rcAUX1 == maxRC)
        {
            rcAUX1 = minRC;

            long flightTime = (System.currentTimeMillis() - lastARMTime) / 1000;
            if(flightTime>60)
            {
                Toast.makeText(RemoteActivity.this,(flightTime / 60)+" Minutes Flight",Toast.LENGTH_LONG).show();
            }
            else
            {
                Toast.makeText(RemoteActivity.this,""+flightTime+" Seconds Flight",Toast.LENGTH_LONG).show();
            }

            return true;
        }

        return false;
    }

    String msg = "";
    public void connect() {
        final Handler handler = new Handler();
        if (connect_type == CONNECT_USBOTG) {
            int result = connectUsb();
            if(result != USB_OTG_RESULT_NO_PERMISSION) {
                Toast.makeText(RemoteActivity.this,
                        "USB-OTG "+(result==USB_OTG_RESULT_SUCCESS?"Connected":"Connect failed"),
                        Toast.LENGTH_SHORT).show();
            }
        }
        else
        {

            new Thread(){
                @Override
                public void run() {

                        boolean result = false;
                        try {

                            if (connect_type == CONNECT_BLUETOOTH) {
                                result = tBlue.connect();
                                msg = device_name;
                            } else if (connect_type == CONNECT_TCP) {
                                result = tcp.connect(host, port);
                                msg = "TCP " + host + ":" + port;
                            } else if (connect_type == CONNECT_UDP) {
                                result = udp.connect(host, port);
                                msg = "UDP " + host + ":" + port + "";
                            }

                            if(result)
                                msg += " Connected";
                            else
                                msg+= " Connect Failed";
                        }
                        catch(Exception e)
                        {
                            Log.e("MWC Remote","Connect Error",e);
                        }

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(RemoteActivity.this, msg, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
            }.start();
        }
    }

    @TargetApi(12)
    private int connectUsb()
    {
        int result = USB_OTG_RESULT_FAILED;
        HashMap<String, UsbDevice> deviceSet =  usbManager.getDeviceList();
        Log.i("USB-OTG","USB设备数:"+deviceSet.size());

        for (UsbDevice device:deviceSet.values())
        {
            if(usbManager.hasPermission(device))
            {
                Log.i("USB-OTG","开始连接"+device.getDeviceName()+" "+device.getVendorId()+" "
                        +device.getDeviceId());

                if(CH34x.isSupported(device))
                {
                    Log.i("USB-OTG","检测到CH340");
                    usbSerial = new CH34x(usbManager);
                }
                else if(FDTI.isSupported(device))
                {
                    Log.i("USB-OTG","检测到FTDI");
                    usbSerial = new FDTI(usbManager);
                }
                else
                {
                    Log.i(getClass().getSimpleName(), "不支持的USB设备");
                }

                if(usbSerial!=null && usbSerial.begin(device))
                {
                    result = USB_OTG_RESULT_SUCCESS;
                }
            }
            else
            {
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(USB_PERMISSION), 0);
                usbManager.requestPermission(device, pendingIntent);

                result = USB_OTG_RESULT_NO_PERMISSION;
                break;
            }
        }

        return result;
    }

    public synchronized void send(byte[] data)
    {
        if(connect_type==CONNECT_BLUETOOTH)
        {
            tBlue.write(data);
        }
        else if(connect_type==CONNECT_TCP)
        {
            tcp.send(data);
        }
        else if(connect_type==CONNECT_UDP)
        {
            udp.send(data);
        }
        else if(connect_type==CONNECT_USBOTG)
        {
            if(usbSerial!=null && (!usbSerial.isClosed()))
            {
                usbSerial.write(data);
            }
        }
    }

    //接收数据
    protected void startThread()
    {
        final Handler handler = new Handler();
        Thread uiThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Runnable processUpdateUI = new Runnable() {
                    @Override
                    public void run() {
                        updateUI();
                    }
                };

                try {
                    while ((!Thread.currentThread().isInterrupted()) && (!exit)) {
                        handler.post(processUpdateUI);
                        Thread.sleep(80, 0);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        Thread requestThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while ((!Thread.currentThread().isInterrupted()) && (!exit)) {
                        sendRequestAnalog();

                        Thread.sleep(1000, 0);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        Thread rcThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100, 0);

                    while ((!Thread.currentThread().isInterrupted()) && (!exit)) {
                        calculateRCValues();
                        sendRC();

                        byte[] rxData = receiveBytes();
                        int v = MSP.getVbat(rxData);
                        if (v > 0) {
                            vbat = v;
                        }

                        Thread.sleep(Math.max(0, 20 - (System.currentTimeMillis() - lastSend)), 0);

                        long dur = System.currentTimeMillis() - lastSend;
                        fps = (int) Math.round(1000.0 / (float) dur);

                        lastSend = System.currentTimeMillis();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        uiThread.start();
        rcThread.start();
        requestThread.start();
    }

    private synchronized byte[] receiveBytes() {
        byte[] rxData = new byte[0];
        try {
            if (connect_type == CONNECT_BLUETOOTH) {
                rxData = tBlue.read();
            } else if (connect_type == CONNECT_TCP) {

            } else if (connect_type == CONNECT_UDP) {

            } else if (connect_type == CONNECT_USBOTG) {
                if (usbSerial != null)
                    rxData = usbSerial.read();
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return rxData;
    }

    public class Receiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().contentEquals(USB_PERMISSION))
            {
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false))
                {
                    connect();
                }
            }
        }
    }

}
