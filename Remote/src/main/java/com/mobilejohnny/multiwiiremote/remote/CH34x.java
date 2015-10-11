package com.mobilejohnny.multiwiiremote.remote;

import android.annotation.TargetApi;
import android.hardware.usb.*;
import android.util.Log;

/**
 * Created by zwb on 15-10-11.
 */
@TargetApi(12)
public class CH34x extends UsbSerial {
    private static final ID[] IDs = new ID[]{
            new ID(0x4348, 0x5523),
            new ID(0x1a86, 0x7523),
            new ID(0x1a86, 0x5523)
    };
    
    private static final int REQUEST_TYPE_OUT = 0x41;
    private static final int REQUEST_WRITE_REGISTER = 0x9A;


    public CH34x(UsbManager manager)
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
            setBaudRate();
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


    public static boolean isSupported(UsbDevice device)
    {
        boolean result = false;
        int vid = device.getVendorId();//厂商ID
        int pid = device.getProductId();//产品ID

        for (int i = 0; i <IDs.length; i++) {
            ID id = IDs[i];
            if(id.vid == vid && id.pid == pid)
            {
                result = true;
                break;
            }
        }
        return result;
    }


    private static class ID
    {
        public int vid;
        public int pid;
        public ID(int vid,int pid)
        {
            this.vid = vid;
            this.pid = pid;
        }
    }
}
