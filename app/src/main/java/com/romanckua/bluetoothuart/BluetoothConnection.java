package com.romanckua.bluetoothuart;

public interface BluetoothConnection extends Runnable {

    public void disconnect();

    public void sendMessage(String string);


}
