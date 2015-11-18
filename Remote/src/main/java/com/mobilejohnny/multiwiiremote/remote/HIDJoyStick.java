package com.mobilejohnny.multiwiiremote.remote;

import android.annotation.TargetApi;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import java.util.Arrays;

/**
 * Created by zwb on 15-10-11.
 */
@TargetApi(12)
public class HIDJoyStick extends UsbSerial {
    private static final ID[] IDs = new ID[]{
            new ID(0x061c, 0x5523)
    };

    private static final int REQUEST_TYPE_OUT = 0x41;
    private static final int REQUEST_WRITE_REGISTER = 0x9A;


    public HIDJoyStick(UsbManager manager)
    {
        this.manager = manager;
    }

    public boolean begin(UsbDevice device) {

        boolean result = false;
        if(!closed)
            return true;

        result = initEndpoint(device);

        if(result)
        {
//            setBaudRate();
        }

        return result;
    }

    public void setBaudRate()
    {
        int index = 0xcc03;//115200
        int index2 = 0x0008;
        int val = 0x1312;
        int val2 = 0x0f2c;
        connection.controlTransfer(REQUEST_TYPE_OUT,REQUEST_WRITE_REGISTER,val,index,null,0,0);
        connection.controlTransfer(REQUEST_TYPE_OUT,REQUEST_WRITE_REGISTER,val2,index2,null,0,0);
    }

    public int write(byte[] data)
    {
        return connection.bulkTransfer(endpointOUT,data,data.length,0);
    }

    public byte[] read()  {
        int len = 0;
        synchronized (this) {
            len = connection.bulkTransfer(endpointIN, readBuffer, readBuffer.length, 0);
        }
        int maxPacketSize = endpointIN.getMaxPacketSize();

        byte[] data = new byte[0];
        if(len>0)
        {
           data = Arrays.copyOfRange(readBuffer,0,len);
        }

        return data;
    }

    public static boolean isSupported(UsbDevice device)
    {
        boolean result = false;
        int vid = device.getVendorId();//厂商ID
        int pid = device.getProductId();//产品ID

        for (int i = 0; i <IDs.length; i++) {
            ID id = IDs[i];
            if(id.vid == vid )
            {
                result = true;
                break;
            }
        }
        return result;
    }



}
