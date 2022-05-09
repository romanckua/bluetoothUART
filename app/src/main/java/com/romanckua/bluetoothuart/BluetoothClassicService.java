package com.romanckua.bluetoothuart;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.*;
import java.lang.reflect.Method;

public class BluetoothClassicService implements BluetoothConnection {

    private ServiceUART myService;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothDevice device;
    private BluetoothSocket clientSocket = null;
    private InputStream connectedInputStream = null;
    private OutputStream connectedOutputStream = null;

    public BluetoothClassicService(ServiceUART myService, String blueToothDeviceAddress) {
        this.myService = myService;
        device = bluetoothAdapter.getRemoteDevice(blueToothDeviceAddress);
    }

    @Override
    public void disconnect() {
        buttonLock();
        try {
            if (clientSocket != null) {
                clientSocket.close();
            }
            if (connectedInputStream != null) {
                connectedOutputStream.close();
                connectedInputStream.close();
            }

        } catch (IOException e) {
            Thread.interrupted();
        }
        Thread.interrupted();

    }

    @Override
    public void sendMessage(String string) {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(connectedOutputStream));
            bufferedWriter.write(string);
            bufferedWriter.flush();
        } catch (Exception e) {
            buttonLock();
            appendTextView(e.getMessage());
        }
    }

    @Override
    public void run() {
        appendTextView("Classic mode. Connecting... wait");

        try {
            Method m = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
            clientSocket = (BluetoothSocket) m.invoke(device, 1);

        } catch (Exception e) {
            appendTextView(e.getMessage());
            buttonLock();
        }
        try {
            clientSocket.connect();
            appendTextView("Connect.");
            buttonUnLock();
            connectedInputStream = clientSocket.getInputStream();
            connectedOutputStream = clientSocket.getOutputStream();
            startInputStream();
        } catch (Exception e) {
            buttonLock();
            appendTextView(e.getMessage());
        }

    }

    private void appendTextView(String string) {
        myService.textMessage(MainActivity.PARAM_TEXT, string);
    }

    private void buttonUnLock() {
        myService.textMessage(MainActivity.PARAM_MSG, "buttonUnLock");
        myService.setStatusConn("buttonStatus", true);
    }

    private void buttonLock() {

        myService.textMessage(MainActivity.PARAM_MSG, "buttonLock");
        myService.setStatusConn("buttonStatus", false);

    }


    private void startInputStream() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connectedInputStream));
                    while (true) {
                        if (bufferedReader.ready()) {
                            appendTextView(bufferedReader.readLine());
                        }
                    }
                } catch (Exception e) {
                    buttonLock();
                    appendTextView(e.getMessage());
                }
            }
        });
        thread.start();
    }

}



