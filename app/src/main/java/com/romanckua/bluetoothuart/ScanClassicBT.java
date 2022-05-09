package com.romanckua.bluetoothuart;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ScanClassicBT implements BluetoothDeviceList {

    Activity activity;
    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice device;
    HashSet<String> hashSetBTaddList = new HashSet<>();
    ArrayList<String> listViewBTlist = new ArrayList<>();
    ArrayList<String> macAddressList = new ArrayList<>();
    ArrayAdapter<String> adapter;
    private final BroadcastReceiver deviceBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (hashSetBTaddList != null) {
                    if (!hashSetBTaddList.contains(device.getAddress())) {
                        hashSetBTaddList.add(device.getAddress());
                        listViewBTlist.add(device.getName() + ": " + device.getAddress());
                        macAddressList.add(device.getAddress());
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    hashSetBTaddList.add(device.getAddress());
                    listViewBTlist.add(device.getName() + ": " + device.getAddress());
                    macAddressList.add(device.getAddress());
                    adapter.notifyDataSetChanged();
                }
            }
        }
    };


    public ScanClassicBT(Activity activity) {
        this.activity = activity;
    }


    @Override
    public ArrayAdapter getListAdapter() {
        return adapter;
    }

    @Override
    public void scanStart() {

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (listViewBTlist != null) {
            adapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, listViewBTlist);
        }

        getBondingDevice();

        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        activity.registerReceiver(deviceBroadcastReceiver, filter);

    }

    @Override
    public void scanStop() {

        if (bluetoothAdapter != null) {
            bluetoothAdapter.cancelDiscovery();
        }
    }

    @Override
    public String getSelectBluetoothDevice(int select) {
        return macAddressList.get(select);
    }

    private void getBondingDevice() {

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            ArrayList<BluetoothDevice> data = new ArrayList<>(pairedDevices);
            for (BluetoothDevice index : data
            ) {
                if (hashSetBTaddList != null) {
                    if (!hashSetBTaddList.contains(index.getAddress())) {
                        hashSetBTaddList.add(index.getAddress());
                        listViewBTlist.add(index.getName() + ": " + index.getAddress());
                        macAddressList.add(index.getAddress());
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    hashSetBTaddList.add(index.getAddress());
                    listViewBTlist.add(index.getName() + ": " + index.getAddress());
                    macAddressList.add(index.getAddress());
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }
}
