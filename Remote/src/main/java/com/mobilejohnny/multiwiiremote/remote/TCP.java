package com.mobilejohnny.multiwiiremote.remote;

import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by admin2 on 2015/3/20.
 */
public class TCP {

    private TCPListener listener;
    private OutputStream outputStream;
    private Socket socket;
    private ServerSocket serverSocket;

    public TCP()  {
    }

    public void connect(final String ip, final int port)
    {
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    socket = new Socket(ip,port);
                    outputStream = socket.getOutputStream();
                } catch (IOException e) {
                    Log.e("TCP",e.getMessage());
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public boolean send(byte[] data)
    {
        boolean result = false;
        try {
            outputStream.write(data);
            result = true;
        } catch (IOException e) {
            Log.e("TCP",e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    public OutputStream getOutputStream()
    {
        return outputStream;
    }

    public boolean startServer(int port, final TCPListener listener)
    {
        boolean result = false;
        try {
            serverSocket = new ServerSocket(port);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        while(!Thread.currentThread().isInterrupted()){

                            socket = serverSocket.accept();
                            listener.onConnected(socket);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();



            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public void close()
    {
        try {
            if(socket!=null)socket.close();
            if(serverSocket!=null)serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public interface TCPListener{
        void onConnected(Socket socket);
    }
}
