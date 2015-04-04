package com.mobilejohnny.multiwiiremote.remote;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.preference.ListPreference;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by admin2 on 2015/3/25.
 */
public class BluetoothDeviceListPreference extends ListPreference {
    private Bluetooth bluetooth;

    public BluetoothDeviceListPreference(Context context) {
        super(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BluetoothDeviceListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BluetoothDeviceListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public BluetoothDeviceListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        bluetooth = new Bluetooth();
        Set<BluetoothDevice> devices = bluetooth.getBondedDevices();
        ArrayList<CharSequence> listEntries = new ArrayList<CharSequence>();
        for (BluetoothDevice device : devices)
        {
            listEntries.add(device.getName());
        }


        CharSequence[] arrEntries = listEntries.toArray(new CharSequence[listEntries.size()]);
        setEntries(arrEntries);
        setEntryValues(arrEntries);
    }
}
