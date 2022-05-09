package com.romanckua.bluetoothuart;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;

public class BluetoothPermits implements VerificationOfPermits {

    Activity activity;
    private final int REQUEST_CODE = 17890;

    BluetoothPermits(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void verificationOfPermits() {

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                activity.startActivityForResult(enableBtIntent, REQUEST_CODE);
        }
    }
}
