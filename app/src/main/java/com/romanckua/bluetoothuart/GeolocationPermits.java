package com.romanckua.bluetoothuart;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class GeolocationPermits implements VerificationOfPermits {

    Activity activity;
    private final int PERMISSION_REQUEST = 17990;

    GeolocationPermits(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void verificationOfPermits() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST);
        }
    }
}
