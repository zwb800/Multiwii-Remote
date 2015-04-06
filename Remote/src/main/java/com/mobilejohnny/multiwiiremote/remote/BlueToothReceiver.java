package com.mobilejohnny.multiwiiremote.remote;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by admin2 on 2015/3/18.
 */
public class BlueToothReceiver extends BroadcastReceiver {

    private final Context ctx;


    public BlueToothReceiver(Context context)
        {
            super();
            this.ctx = context;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i("BT",action);
            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                //Do something if connected
                //Toast.makeText(ctx, "flone connected", Toast.LENGTH_SHORT).show();
                // floneConnected = true;
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                //Do something if disconnected
                Toast.makeText(ctx, "Bluetooth disconnected", Toast.LENGTH_SHORT).show();
                //floneConnected = false;
//                try {
//                    tBlue.close();
//
//                }
//                catch (Exception e) {
//                    println("No se ha podido cerrar el socket" + e);
//                }
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //Done searching
                Toast.makeText(ctx, "Bluetooth discovery finished.", Toast.LENGTH_SHORT).show();
            }
            else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //Device found
                Toast.makeText(ctx, "Found Bluetooth device.", Toast.LENGTH_SHORT).show();
            }
        }
}
