package com.romanckua.bluetoothuart;

import android.widget.ArrayAdapter;


public interface BluetoothDeviceList<T> {

public ArrayAdapter<T> getListAdapter();

public void scanStart();

public void scanStop();

public String getSelectBluetoothDevice(int select);

}
