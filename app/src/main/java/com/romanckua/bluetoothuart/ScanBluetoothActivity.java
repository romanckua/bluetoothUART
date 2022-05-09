package com.romanckua.bluetoothuart;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class ScanBluetoothActivity extends AppCompatActivity {

    private String scanBluetoothMethod = "default";
    private BluetoothDeviceList scan;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scannerbluetooth);

        scanBluetoothMethod = getIntent().getStringExtra("ScanBluetoothActivity");

        if (scanBluetoothMethod.equals("ble")) {
            scan = new ScanBLE(ScanBluetoothActivity.this);
        }
        if (scanBluetoothMethod.equals("classic")) {
            scan = new ScanClassicBT(ScanBluetoothActivity.this);
        }

            scan.scanStart();
            listView = findViewById(R.id.scannerbluetoothList);
            listView.setAdapter(scan.getListAdapter());



        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View itemClicked, int position, long id) {
                String device = scan.getSelectBluetoothDevice(position);
                System.out.println("select device: "+device);
                Setting setting = new Setting(ScanBluetoothActivity.this);
                setting.setSetting("device", device);
                Intent intent = new Intent(MainActivity.BROADCAST_ACTION);
                intent.putExtra(MainActivity.PARAM_MSG, "reconnect");
                LocalBroadcastManager.getInstance(ScanBluetoothActivity.this).sendBroadcast(intent);
                scan.scanStop();
                finish();
            }
        });
    }

    @Override
    protected void onPause() {
        scan.scanStop();
        super.onPause();
        finish();
    }
}


