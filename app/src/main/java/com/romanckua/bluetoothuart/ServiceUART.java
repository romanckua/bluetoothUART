package com.romanckua.bluetoothuart;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.HashMap;

public class ServiceUART extends Service {

    public class ServiceUARTBinder extends Binder {
        ServiceUART getService() {
            return ServiceUART.this;
        }
    }

    private IBinder binder = new ServiceUARTBinder();
    private static final int ID_SERVICE = 17870;
    //private static final String START_PLEASE = "start";
    private static final String STOP_PLEASE = "stop";
    private String deviceAddress = "00:00:00:00:00:00";
    private String mode = "classic";
    private ArrayList<String> messageRX = new ArrayList<>();
    private HashMap<String, Boolean> statusBTservice = new HashMap<>();
    private BluetoothConnection bluetoothConnection;
    private Thread thread;
    private Setting setting;

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    public ServiceUART() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? createNotificationChannel(notificationManager) : "";
        Intent stopIntent = new Intent(this, ServiceUART.class);
        Intent resultIntent = new Intent(this, MainActivity.class);
        stopIntent.setAction(STOP_PLEASE);
        PendingIntent piStopService = PendingIntent.getService(this, 0, stopIntent, 0);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("Сповіщення Bluetooth UART")
                .setContentText("сервіс запущено у фоні")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .addAction(android.R.drawable.ic_delete, "Закрити додаток", piStopService)
                .setContentIntent(resultPendingIntent)
                .build();
        startForeground(ID_SERVICE, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            final String action = intent.getAction();
            if (STOP_PLEASE.equals(action)) {
                textMessage(MainActivity.PARAM_MSG, "destroy");
                serviceStop();
            }
        }
        return super.onStartCommand(intent, flags, startId);

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(NotificationManager notificationManager) {
        String channelId = String.valueOf((int) Math.random() * 111);
        String channelName = "Bluetooth UART";
        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
        channel.setImportance(NotificationManager.IMPORTANCE_LOW);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        notificationManager.createNotificationChannel(channel);
        return channelId;
    }

    public void setStatusConn(String string, boolean status) {
        statusBTservice.put(string, status);
    }

    public ArrayList<String> getMessageRX() {
        return messageRX;
    }

    public HashMap<String, Boolean> getStatusConn() {
        return statusBTservice;
    }


    public void textMessage(String first, String second) {
        Intent intent = new Intent(MainActivity.BROADCAST_ACTION);
        intent.putExtra(first, second);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        if (first.equals(MainActivity.PARAM_TEXT)) {
            messageRX.add(second);
        }
    }

    public void serviceStop() {

        bluetoothConnection.disconnect();
        thread.interrupt();
        stopSelf();
    }

    public void disconnect() {
        System.out.println("dis start");
        if (bluetoothConnection != null) {
            System.out.println(bluetoothConnection);
            bluetoothConnection.disconnect();
            System.out.println(bluetoothConnection);


        }
        System.out.println("send key");
        textMessage(MainActivity.PARAM_MSG, "buttonLock");
        setStatusConn("buttonStatus", false);
    }

    public void connect() {
        if (bluetoothConnection != null) {
            bluetoothConnection.disconnect();

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        setting = new Setting(this);
        deviceAddress = setting.getSetting("device");
        mode = setting.getSetting("mode");
        System.out.println("read config");
        System.out.println(deviceAddress);
        System.out.println(mode);


        if (mode.equals("ble")) {
            bluetoothConnection = new BluetoothLeService(ServiceUART.this, deviceAddress);
        }
        if (mode.equals("classic")) {
            bluetoothConnection = new BluetoothClassicService(ServiceUART.this, deviceAddress);
        }
        thread = new Thread(bluetoothConnection);
        thread.start();
    }

   public BluetoothConnection getBluetoothConnection() {
        return bluetoothConnection;
    }

}

