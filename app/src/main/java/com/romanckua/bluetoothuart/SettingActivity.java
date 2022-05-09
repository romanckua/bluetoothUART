package com.romanckua.bluetoothuart;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class SettingActivity extends AppCompatActivity{

    Button buttonMethodBLE;
    Button buttonMethodClassic;
    Button buttonModeBLE;
    Button buttonModeClassic;
    Setting setting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settingactivity);

        buttonMethodBLE = findViewById(R.id.buttonMethodBLE);
        buttonMethodClassic = findViewById(R.id.buttonMethodClassic);
        buttonModeBLE = findViewById(R.id.buttonModeBLE);
        buttonModeClassic = findViewById(R.id.buttonModeClassic);
        setting = new Setting(this);
        checkMethod();
        checkMode();

        buttonMethodBLE.setOnClickListener(v -> {
            setting.setSetting("method", "ble");
            checkMethod();

        });
        buttonMethodClassic.setOnClickListener(v -> {
            setting.setSetting("method", "classic");
            checkMethod();

        });
        buttonModeBLE.setOnClickListener(v -> {
            setting.setSetting("mode", "ble");
            checkMode();

        });
        buttonModeClassic.setOnClickListener(v -> {
            setting.setSetting("mode", "classic");
            checkMode();

        });
    }

    private void checkMethod () {
        if (setting.getSetting("method").equals("ble")) {
            buttonMethodBLE.setEnabled(false);
            buttonMethodClassic.setEnabled(true);
        } else {
            buttonMethodBLE.setEnabled(true);
            buttonMethodClassic.setEnabled(false);
        }
    }

    private void checkMode() {
        if (setting.getSetting("mode").equals("ble")) {
            buttonModeBLE.setEnabled(false);
            buttonModeClassic.setEnabled(true);
        } else {
            buttonModeBLE.setEnabled(true);
            buttonModeClassic.setEnabled(false);
        }
    }

    @Override
    protected void onPause() {
        Intent intent = new Intent(MainActivity.BROADCAST_ACTION);
        intent.putExtra(MainActivity.PARAM_MSG, "reconnect");
        LocalBroadcastManager.getInstance(SettingActivity.this).sendBroadcast(intent);
        super.onPause();
    }
}
