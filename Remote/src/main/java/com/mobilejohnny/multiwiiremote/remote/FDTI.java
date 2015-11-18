package com.mobilejohnny.multiwiiremote.remote;

import android.annotation.TargetApi;
import android.hardware.usb.*;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Created by admin2 on 2015/4/11.
 */
@TargetApi(12)
public class FDTI extends UsbSerial {
    private static final ID[] IDs = new ID[]{
            new ID(0x0403, 0x6001),
            new ID(0x0403, 0x6015)
    };
    private static final int REQUEST_TYPE_OUT = 0x40;
    private static final int REQUEST_RESET = 0;
    private static final int REQUEST_SET_BUADRATE = 3;
    private static final int STATUS_BYTE_LEN = 2;


    public FDTI(UsbManager manager)
    {
        this.manager = manager;

        readBuffer = new byte[1024];
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

    public boolean begin(UsbDevice device) {

        boolean result = false;
        if(!closed)
            return true;

        result = initEndpoint(device);
        if(result)
        {
            reset();
            clear();
            setBaudRate();
        }

        return result;
    }

    public int reset() {
        return connection.controlTransfer(REQUEST_TYPE_OUT,REQUEST_RESET,0,0,null,0,0);
    }

    public int clear() {
        connection.controlTransfer(REQUEST_TYPE_OUT,REQUEST_RESET,1,0,null,0,0);//clear Rx
        return connection.controlTransfer(REQUEST_TYPE_OUT,REQUEST_RESET,2,0,null,0,0);//clear Tx
    }

    public void setBaudRate()
    {
        int rate = 0x001a;//115200
        connection.controlTransfer(REQUEST_TYPE_OUT,REQUEST_SET_BUADRATE,rate,0,null,0,0);
    }

    public int write(byte[] data)
    {
        return connection.bulkTransfer(endpointOUT,data,data.length,0);
    }

    public byte[] read()   {
        int len = 0;
        synchronized (this) {
            len = connection.bulkTransfer(endpointIN, readBuffer, readBuffer.length, 0);
        }
        int maxPacketSize = endpointIN.getMaxPacketSize();
//        return filterStatusBytes(data, len, maxPacketSize);
        byte[] data = Arrays.copyOfRange(readBuffer, 0, len);
        return data;
    }

    private int filterStatusBytes(byte[] data, int len, int maxPacketSize) {
        int packetCount = (int) Math.ceil(len/(float)maxPacketSize);
        int count = maxPacketSize - STATUS_BYTE_LEN;
        int offset,offset2;
        for (int i=0;i<packetCount;i++)
        {
            if(i==packetCount-1)
            {
                count = len % maxPacketSize - STATUS_BYTE_LEN;
                if(count<=0)
                    break;
            }
            offset = i * maxPacketSize + STATUS_BYTE_LEN;
            offset2 = i * (maxPacketSize - STATUS_BYTE_LEN);
            System.arraycopy(readBuffer,offset,data,offset2,count);
        }

        return len - packetCount * STATUS_BYTE_LEN ;
    }





}
