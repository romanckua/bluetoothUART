package com.romanckua.bluetoothuart;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.*;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ScanBLE implements BluetoothDeviceList {

    Activity activity;
    BluetoothLeScanner scanner;
    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice device;
    List<ScanFilter> filters = null;
    HashSet<String> hashSetBTaddList = new HashSet<>();
    ArrayList<String> listViewBTlist = new ArrayList<>();
    ArrayList<String> macAddressList = new ArrayList<>();
    ArrayAdapter<String> adapter;

    private final ScanCallback scanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            device = result.getDevice();
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

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
        }

        @Override
        public void onScanFailed(int errorCode) {
        }
    };

    public ScanBLE(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void scanStart() {

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        scanner = bluetoothAdapter.getBluetoothLeScanner();

        ScanSettings scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
                .setReportDelay(0L)
                .build();

        if (scanner != null) {
            scanner.startScan(filters, scanSettings, scanCallback);
        } else {
        }
    }

    @Override
    public void scanStop() {
        scanner.stopScan(scanCallback);
    }

    @Override
    public ArrayAdapter<String> getListAdapter() {
        if (listViewBTlist != null) {
            adapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, listViewBTlist);
        }
        return adapter;
    }

    @Override
    public String getSelectBluetoothDevice(int select) {
        return macAddressList.get(select);
    }
}
