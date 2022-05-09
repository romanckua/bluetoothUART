package com.romanckua.bluetoothuart;

import android.content.*;
import android.os.Build;
import android.os.IBinder;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private String bluetoothScanMethod = "classic";
    private Setting setting = new Setting(this);
    private ServiceUART service;
    boolean bound = false;
    private ServiceConnection connectionService;
    private Intent intent;
    public static final String BROADCAST_ACTION = "com.romanckua.bluetoothuart";
    public final static String PARAM_TEXT = "TEXT";
    public final static String PARAM_MSG = "MSG";
    public final static String PARAM_START = "START";
    private TextView textView;
    private EditText inputText;
    private Button buttonRSSI;
    private Button buttonSend;
    private Button buttonSendAT;
    private Button buttonConnect;
    private Button buttonClear;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {

            String start = intent.getStringExtra(PARAM_START);
            if (start != null) {
                textView.setText(start + "\n" + textView.getText());
            }
            String text = intent.getStringExtra(PARAM_TEXT);
            if (text != null) {
                textView.setText(text + "\n" + textView.getText());
            }
            String msg = intent.getStringExtra(PARAM_MSG);
            if (msg != null) {
                if (msg.equals("destroy")) {
                    MainActivity.this.finish();
                }
                if (msg.equals("buttonLock")) {
                    buttonLock();
                }
                if (msg.equals("buttonUnLock")) {
                    buttonUnLock();
                }
                if (msg.equals("reconnect")) {
                    if (service != null) {
                        service.connect();
                    }
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textView);
        textView.setMovementMethod(new ScrollingMovementMethod());
        inputText = findViewById(R.id.inputText);
        inputText.setText("");
        buttonSendAT = findViewById(R.id.buttonSendAT);
        buttonSend = findViewById(R.id.buttonSend);
        buttonRSSI = findViewById(R.id.buttonRSSI);
        buttonClear = findViewById(R.id.buttonClear);
        buttonConnect = findViewById(R.id.buttonConnect);
        buttonRSSI.setEnabled(false);
        buttonSend.setEnabled(false);
        buttonSendAT.setEnabled(false);


        new BluetoothPermits(this).verificationOfPermits();
        new GeolocationPermits(this).verificationOfPermits();

        bluetoothScanMethod = setting.getSetting("method");
        startService();

        buttonRSSI.setOnClickListener(v -> {
            service.getBluetoothConnection().sendMessage("AT+RSSI?");
        });

        buttonSend.setOnClickListener(v -> {
            service.getBluetoothConnection().sendMessage(inputText.getText().toString()+"\n");
            inputText.setText("");
        });

        buttonSendAT.setOnClickListener(v -> {
            service.getBluetoothConnection().sendMessage(inputText.getText().toString());
            inputText.setText("AT+");
        });

        buttonClear.setOnClickListener(v -> {
            textView.setText("");

        });
        buttonConnect.setOnClickListener(v -> {
            if (buttonConnect.getText().equals("Connect") && bound) {
                service.connect();
            }
            if (buttonConnect.getText().equals("Disconnect") && bound ) {
                service.disconnect();
            }

        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 17890) {
            new BluetoothPermits(this).verificationOfPermits();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 17990) {
            new GeolocationPermits(this).verificationOfPermits();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingActivity.class);
           // intent.putExtra("ScanBluetoothActivity", bluetoothScanMethod);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_scanbt) {
            Intent intent = new Intent(MainActivity.this, ScanBluetoothActivity.class);
            intent.putExtra("ScanBluetoothActivity", bluetoothScanMethod);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startService() {

        intent = new Intent(MainActivity.this, ServiceUART.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }

        connectionService = new ServiceConnection() {

            public void onServiceConnected(ComponentName name, IBinder binder) {
                service = ((ServiceUART.ServiceUARTBinder) binder).getService();
                bound = true;
                if (service.getMessageRX().size() > 0) {
                    for (String index : service.getMessageRX()
                    ) {
                        textView.setText(index + "\n" + textView.getText());
                    }
                }
                if (service.getStatusConn().size() > 0) {
                    for (Map.Entry<String, Boolean> index : service.getStatusConn().entrySet()
                    ) {

                        if (index.getKey().equals("buttonStatus")) {
                            if (index.getValue()) {
                                buttonUnLock();
                            } else {
                                buttonLock();
                            }
                        }
                    }
                }
            }

            public void onServiceDisconnected(ComponentName name) {
                bound = false;
            }
        };

        bindService(intent, connectionService, BIND_AUTO_CREATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(BROADCAST_ACTION));

    }

    private void buttonLock() {

        buttonConnect.setText("Connect");
        buttonRSSI.setEnabled(false);
        buttonSend.setEnabled(false);
        buttonSendAT.setEnabled(false);
    }

    private void buttonUnLock() {

        buttonConnect.setText("Disconnect");
        buttonRSSI.setEnabled(true);
        buttonSend.setEnabled(true);
        buttonSendAT.setEnabled(true);

    }
}