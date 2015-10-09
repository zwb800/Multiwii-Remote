package com.mobilejohnny.multiwiiremote.remote;

import android.hardware.usb.*;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by admin2 on 2015/4/11.
 */
public class FDTI {
    private static final int REQUEST_TYPE_OUT = 0x40;
    private static final int REQUEST_RESET = 0;
    private static final int REQUEST_SET_BUADRATE = 3;
    private static final int STATUS_BYTE_LEN = 2;

    private UsbManager manager;

    private int bcdDevice = 1;//FT232RL
    private int numOfChannels = 6;
    private UsbEndpoint endpointIN;
    private UsbEndpoint endpointOUT;
    private UsbDeviceConnection connection;
    private byte[] readBuffer;
    private boolean closed = true;

    public FDTI(UsbManager manager)
    {
        this.manager = manager;

        readBuffer = new byte[1024];
    }



    public boolean begin(UsbDevice device) {

        boolean result = false;
        if(!closed)
            return true;

        connection = manager.openDevice(device);
        int interfaceCount = device.getInterfaceCount();
        device.getInterface(interfaceCount-1);

        for (int j=0;j<interfaceCount;j++)
        {
            UsbInterface usbInterface = device.getInterface(j);

            if(connection.claimInterface(usbInterface,true))
            {
                Log.i(this.getClass().getSimpleName(),device.getDeviceName()+" "+device.getVendorId()+" "+device.getProductId());
                    reset();
                    clear();
                    setBaudRate();

                    for (int i=0;i<usbInterface.getEndpointCount();i++)
                    {
                        UsbEndpoint endpoint = usbInterface.getEndpoint(i);
                        if(endpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK)
                        {
                            if(endpoint.getDirection() == UsbConstants.USB_DIR_IN)
                            {
                                endpointIN = endpoint;
                            }
                            else
                            {
                                endpointOUT = endpoint;
                            }
                        }
                    }


                    if(endpointIN!=null && endpointOUT!=null)
                    {
                        closed = false;
                        result = true;
                        break;
                    }

            }
            else {
                Log.e(this.getClass().getSimpleName(), "claimInterface error");
                break;
            }
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

    public int read(byte[] data) throws IOException {
        if(closed)
            throw new IOException("Connection is closed");
        int len = 0;
        synchronized (this) {
            len = connection.bulkTransfer(endpointIN, readBuffer, readBuffer.length, 0);
        }
        int maxPacketSize = endpointIN.getMaxPacketSize();
        return filterStatusBytes(data, len, maxPacketSize);
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



    public void close()
    {
        if(connection!=null) {
            connection.close();
            closed = true;
        }
    }

}
