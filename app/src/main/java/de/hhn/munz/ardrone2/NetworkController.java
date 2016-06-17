package de.hhn.munz.ardrone2;

import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class NetworkController {
    private static final String TAG = ".NetworkController";

    private static final String SERVER_HOST = "192.168.1.1";
    private static final int SERVER_PORT = 5556;

    private static NetworkController instance;

    private DatagramSocket socket;
    private int sequenceNumber;
    private boolean keepAlive;

    private NetworkController () {
        try {
            sequenceNumber = 0;
            keepAlive = true;
            this.keepAlive();
            socket = new DatagramSocket();
        } catch (Exception e) {
            Log.w(TAG, e.getMessage());
        }
    }

    public static NetworkController getInstance() {
        if (NetworkController.instance == null) {
            NetworkController.instance = new NetworkController();
        }
        return NetworkController.instance;
    }

    public synchronized void sendString(String data) {
        try {
            if (ATCommand.keepAlive().equals(data)) {
                sequenceNumber = 0;
            }

            InetAddress IPAddress =  InetAddress.getByName(SERVER_HOST);

            byte[] bytes = String.format(data,++sequenceNumber).getBytes();

            DatagramPacket packet = new DatagramPacket(bytes,bytes.length, IPAddress, SERVER_PORT);

            socket.send(packet);

        } catch (Exception e) {
            Log.w(TAG, e.getMessage());
        }
    }

    private void keepAlive() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (keepAlive) {
                    try {
                        sendString(ATCommand.keepAlive());
                        Thread.sleep(30);
                    } catch (Exception e) {
                        Log.w(TAG, "KeepAlive: " + e.getMessage());
                    }
                }
            }
        }).start();
    }

    public void closeConnection() {
        keepAlive = false;
        socket.close();
        socket = null;
    }

    public void restartConnection() {
        try {
            if (!keepAlive) {
                socket = new DatagramSocket();
                sequenceNumber = 0;
                keepAlive = true;
                this.keepAlive();
            }
        } catch (Exception e) {
            Log.w(TAG, e.getMessage());
        }
    }
}
