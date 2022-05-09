package com.romanckua.bluetoothuart;

import android.bluetooth.*;
import android.os.Build;
import android.os.Handler;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.UUID;

import static android.bluetooth.BluetoothDevice.*;
import static android.bluetooth.BluetoothGatt.*;
import static java.lang.Thread.sleep;


public class BluetoothLeService implements BluetoothConnection {

    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothDevice device;
    private BluetoothGatt gatt;
    private @Nullable
    Runnable discoverServicesRunnable;
    private ServiceUART myService;
    private Handler bleHandler = new Handler();
    private List<BluetoothGattService> services;
    private String blueToothDeviceAddress;

    private BluetoothGattCharacteristic characteristicTX;
    private BluetoothGattCharacteristic characteristicRX;


    public BluetoothLeService(ServiceUART myService, String blueToothDeviceAddress) {
        this.blueToothDeviceAddress = blueToothDeviceAddress;
        this.myService = myService;
    }


    @Override
    public void run() {
        device = bluetoothAdapter.getRemoteDevice(blueToothDeviceAddress);
        try {
            sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        appendTextView("BLE mode. Спроба підключення, чекайте...");
        connectGatt();
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        int delay;
        int bondstate;

        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {

            if (status == GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    appendTextView("Підключено.");
                    bondstate = device.getBondState();
                    if (bondstate == BOND_BONDING) {
                        while (bondstate == BOND_BONDING) {
                            try {
                                sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            bondstate = device.getBondState();
                        }
                    }

                    if (bondstate == BOND_NONE || bondstate == BOND_BONDED) {
                        discoverServicesRunnable = new Runnable() {
                            @Override
                            public void run() {
                                int delayWhenBonded = 0;
                                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
                                    delayWhenBonded = 1500;
                                }
                                delay = bondstate == BOND_BONDED ? delayWhenBonded : 0;
                                boolean result = gatt.discoverServices();
                                try {
                                    sleep(3000);
                                    setHM10Service();
                                    sleep(500);
                                    buttonUnLock();
                                    getInfoDevice(characteristicTX);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                if (!result) {
                                }
                                discoverServicesRunnable = null;
                            }
                        };
                    }

                    bleHandler.postDelayed(discoverServicesRunnable, delay);

                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    appendTextView("Відключаюся...");
                    disconnect();
                } else {

                }
            } else {

                switch (status) {
                    case GATT_SERVER:
                        buttonLock();
                        gatt.close();
                        appendTextView("Сплив час очікування з'єднання та(або) пристрій відключився сам");
                        appendTextView("Спроба підключення");
                        try {
                            sleep(500);
                            connectGatt();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        break;
                    case GATT_FAILURE:
                        disconnect();
                        buttonLock();
                        appendTextView("Помилка: GATT_FAILURE -");
                        appendTextView(String.valueOf(status));
                        break;
                    case 19:
                        disconnect();
                        buttonLock();
                        appendTextView("Віддалений пристрій від'єднався");
                        appendTextView(String.valueOf(status));
                        break;
                    default:
                        disconnect();
                        buttonLock();
                        appendTextView("Помилка: -");
                        appendTextView(String.valueOf(status));
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
            } else {
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                readCharacteristic(characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            readCharacteristic(characteristic);
        }
    };

    private void connectGatt() {
        gatt = device.connectGatt(myService, true, mGattCallback, TRANSPORT_LE);
    }

    private void setHM10Service() {
        services = gatt.getServices();
        for (BluetoothGattService index : services
        ) {
            if ("0000ffe0-0000-1000-8000-00805f9b34fb".equals(index.getUuid().toString())) {
                characteristicTX = index.getCharacteristic(UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"));
                characteristicRX = index.getCharacteristic(UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb"));
                gatt.setCharacteristicNotification(characteristicRX, true);
            }
        }
    }

    private void readCharacteristic(BluetoothGattCharacteristic characteristic) {

        final byte[] data = characteristic.getValue();
        String str = "";
        for (int i = 0; i < data.length; i++) {
            str = str + (char) data[i];
        }
        appendTextView(str);
    }

    public void getInfoDevice(BluetoothGattCharacteristic characteristic) throws InterruptedException {

        String[] arrayBTEInfo = {"AT+VERS?", "AT+NAME?", "AT+ADDR?", "AT+RSSI?", "AT+MODE?", "AT+TEMP?"};

        for (int i = 0; i < arrayBTEInfo.length; i++) {
            characteristic.setValue((arrayBTEInfo[i]).getBytes());
            gatt.writeCharacteristic(characteristic);
            sleep(100);
        }
    }

    @Override
    public void sendMessage(String string) {
        final byte[] message = (string).getBytes();
        characteristicTX.setValue(message);
        gatt.writeCharacteristic(characteristicTX);
    }

    private void appendTextView(String string) {
        myService.textMessage(MainActivity.PARAM_TEXT, string);
    }

    private void buttonUnLock() {
        myService.textMessage(MainActivity.PARAM_MSG, "buttonUnLock");
        myService.setStatusConn("buttonStatus", true);
    }

    private void buttonLock() {

        myService.textMessage(MainActivity.PARAM_MSG, "buttonLock");
        myService.setStatusConn("buttonStatus", false);

    }

    public void disconnect() {
        buttonLock();
        if (gatt != null) {
            gatt.close();
        }
        Thread.interrupted();
    }


}
