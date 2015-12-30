package com.mobilejohnny.multiwiiremote.remote;

import android.util.Log;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by admin on 2015/11/19.
 */
public class MSP {

    private static final String MSP_HEADER = "$M<";//协议头

    private static final int MSP_SET_RAW_RC = 200;//设置RC数据的消息ID
    private static final int MSP_ANALOG = 110;//获取电压

    static byte[] payloadChar = new byte[16];

    public static void updateRCPayload(int rcRoll,int rcPitch,int rcYaw,int rcThrottle,int rcAUX1,int rcAUX2,int rcAUX3,int rcAUX4) {
        payloadChar[0] = (byte)(rcRoll & 0xFF); //strip the 'most significant bit' (MSB) and buffer\
        payloadChar[1] = (byte)(rcRoll >> 8 & 0xFF); //move the MSB to LSB, strip the MSB and buffer
        payloadChar[2] = (byte)(rcPitch & 0xFF);
        payloadChar[3] = (byte)(rcPitch >> 8 & 0xFF);
        payloadChar[4] = (byte)(rcYaw & 0xFF);
        payloadChar[5] = (byte)(rcYaw >> 8 & 0xFF);
        payloadChar[6] = (byte)(rcThrottle & 0xFF);
        payloadChar[7] = (byte)(rcThrottle >> 8 & 0xFF);

        //aux1
        payloadChar[8] = (byte)(rcAUX1 & 0xFF);
        payloadChar[9] = (byte)(rcAUX1 >> 8 & 0xFF);

        //aux2
        payloadChar[10] = (byte)(rcAUX2 & 0xFF);
        payloadChar[11] = (byte)(rcAUX2 >> 8 & 0xFF);

        //aux3
        payloadChar[12] = (byte)(rcAUX3 & 0xFF);
        payloadChar[13] = (byte)(rcAUX3 >> 8 & 0xFF);

        //aux4
        payloadChar[14] = (byte)(rcAUX4 & 0xFF);
        payloadChar[15] = (byte)(rcAUX4 >> 8 & 0xFF);
    }

    //send msp with payload
    private static byte[] requestMSP (int messageid, byte[] payload) {

        List<Byte> bf = new LinkedList<Byte>();

        //添加协议头
        byte[] mspHead = (MSP_HEADER).getBytes();
        for (int i=0;i<mspHead.length;i++) {
            bf.add(mspHead[i]);
        }

        //添加长度
        byte pl_size = (byte)((payload != null ? (payload.length) : 0)&0xFF);
        bf.add(pl_size);

        //添加命令ID
        bf.add((byte)(messageid & 0xFF));

        if (payload != null) {
            for (int i=0;i<payload.length;i++) {
                bf.add((byte)(payload[i]&0xFF));
            }
        }

        //计算CRC
        byte checksumMSP = 0;
        for (int i = mspHead.length; i < bf.size(); i++) {
            checksumMSP ^= (bf.get(i));
        }
        bf.add(checksumMSP);


        byte[] result = new byte[bf.size()];
        Byte[] result2 = bf.toArray(new Byte[0]);
        for (int i = 0; i < result.length; i++) {
            result[i] = result2[i];
        }
        return result;
    }

    //遥控信号数据包
    public static byte[] getRCPocket(int rcRoll,int rcPitch,int rcYaw,int rcThrottle,int rcAUX1,int rcAUX2,int rcAUX3,int rcAUX4) {
        updateRCPayload(rcRoll,rcPitch,rcYaw,rcThrottle,rcAUX1,rcAUX2,rcAUX3,rcAUX4);
        return requestMSP(MSP_SET_RAW_RC,payloadChar);//封装协议
    }

    //请求电池检测数据
    public static byte[]  getAnalogPocket()
    {
       return requestMSP(MSP_ANALOG,null);
    }

    public static int getVbat(byte[] rxData)
    {
        int vbat = 0;

        int start = indexOfPayloadStart(rxData,MSP_ANALOG,7);

        if(start>0 && start < rxData.length)
        {
//            Log.i("rx",convertToString(rxData));

            vbat = rxData[start] & 0xFF;
        }
        return vbat;
    }

    private static int indexOfPayloadStart(byte[] rxData,int messageID,int size) {
        int index =  -1;
        List<Byte> bf = new LinkedList<Byte>();

        //添加协议头
        byte[] mspHead = ("$M>").getBytes();
        for (int i=0;i<mspHead.length;i++) {
            bf.add(mspHead[i]);
        }

        //添加长度
        bf.add((byte) (size&0xFF));

        //添加命令ID
        bf.add((byte) (messageID&0xFF));

        byte[] findArr = toArray(bf);


        for (int i = 0; i < rxData.length; i++) {
            int j = 0;
            for (;j < findArr.length;j++) {
                if((i+j) >= rxData.length || rxData[i+j] != findArr[j])
                {
                    break;
                }
            }

            if(j == findArr.length)
            {
                index = i + j;
                break;
            }
        }

        return index;
    }

    private static byte[] toArray(List<Byte> bf) {
        Byte[] findArr = bf.toArray(new Byte[0]);
        byte[] result = new byte[bf.size()];

        for (int i = 0; i < result.length; i++) {
            result[i] = findArr[i];
        }
        return result;
    }

    private  static  String convertToString(byte[] data) {
        StringBuffer stringBuffer = new StringBuffer(data.length*2);
        for (int i=0;i<data.length;i++)
        {

            stringBuffer.append( String.format("%x ",data[i]).toUpperCase());

        }
        return stringBuffer.toString();
    }


}
