package com.mobilejohnny.multiwiiremote.remote;

import android.annotation.TargetApi;
import android.hardware.usb.*;
import android.util.Log;

/**
 * Created by zwb on 15-10-11.
 */
@TargetApi(12)
public abstract class UsbSerial {
    protected UsbManager manager;

    protected UsbEndpoint endpointIN;
    protected UsbEndpoint endpointOUT;
    protected UsbDeviceConnection connection;
    protected byte[] readBuffer = new byte[1024];
    protected boolean closed = true;

    public abstract boolean begin(UsbDevice device);
    public abstract int write(byte[] data);
    public abstract byte[] read();
    public boolean isClosed()
    {
        return closed;
    }

    public boolean initEndpoint(UsbDevice device) {

        boolean result = false;

        connection = manager.openDevice(device);
        int interfaceCount = device.getInterfaceCount();
        device.getInterface(interfaceCount-1);

        for (int j=0;j<interfaceCount;j++)
        {
            UsbInterface usbInterface = device.getInterface(j);

            if(connection.claimInterface(usbInterface,true))
            {
                Log.i(this.getClass().getSimpleName(), device.getDeviceName() + " " + device.getVendorId() + " " + device.getProductId());
//                reset();
//                clear();
//                setBaudRate();

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

    public void close()
    {
        if(connection!=null) {
            connection.close();
            closed = true;
        }
    }
}
