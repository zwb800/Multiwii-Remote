package com.mobilejohnny.multiwiiremote.remote;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;

/**
 * Created by zwb08_000 on 2015/3/21.
 */
public class UDP {

    private OutputStream outputStream;
    private InputStream inputStream;
    private InetAddress inetAddress;
    private int port;
    private DatagramSocket datagramSocket;

    public UDP()
    {

    }

    public InputStream getInputStream()
    {
        return  inputStream;
    }

    public OutputStream getOutputStream()
    {
        return outputStream;
    }

    public boolean connect(String ip,int port)
    {

        try {
            inetAddress = InetAddress.getByName(ip);
            this.port = port;
            datagramSocket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return true;
    }

    public boolean send(byte[] data)
    {
        boolean result = false;
        final DatagramPacket datagramPacket = new DatagramPacket(data,data.length,inetAddress,port);
        new Thread(){
            @Override
            public void run() {
                try {
                    if(datagramSocket!=null)
                    datagramSocket.send(datagramPacket);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
        result = true;
        return result;
    }

    public boolean startServer(int port,UDPListener listener)
    {
        try {
            final DatagramSocket datagramSocket = new DatagramSocket(port);

            inputStream = new InputStream() {
                byte[] buffer = new byte[1024];
                int len = -1;
                int i = 0;
                final DatagramPacket datagramPacket = new DatagramPacket(buffer,1024);
                @Override
                public int read() throws IOException {
                    if(len==-1)
                    {
                        datagramSocket.receive(datagramPacket);
                        len = datagramPacket.getLength();
                    }
                    else if(len==i)
                    {
                        len = -1;
                        i=0;
                        return  len;
                    }

                    return buffer[i++];
                }
            };

            outputStream = new OutputStream() {
                @Override
                public void write(int i) throws IOException {
                    byte[] buffer = new byte[1];
                    DatagramPacket datagramPacket = new DatagramPacket(buffer,1);
                    buffer[0] = (byte) i;
                    datagramSocket.send(datagramPacket);
                }
            };

            listener.onConnected(inputStream,outputStream);


        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return  false;
    }

    public void close()
    {
        if(datagramSocket!=null)
        datagramSocket.close();
    }

    public interface UDPListener{
        void onConnected(InputStream inputStream, OutputStream outputStream);
    }
}
