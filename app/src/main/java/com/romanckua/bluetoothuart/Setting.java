package com.romanckua.bluetoothuart;


import android.content.Context;
import java.io.*;
import java.util.HashMap;
import java.util.Map;


public class Setting {

    private final Context activity;
    private static HashMap<String, String> defaultSetting = new HashMap<>();
    private static HashMap<String, String> listSettingItem = new HashMap<>();
    private final String settingFileName = "setting.cfg";

    static {
        defaultSetting.put("device","00:00:00:00:00:00");
        defaultSetting.put("method","ble");
        defaultSetting.put("mode","ble");
    }

    public Setting(Context activity) {
        this.activity = activity;
    }


    public synchronized String getSetting(String key) {

        if (!readConfig()) {
            writeConfig(defaultSetting);
        }
        readConfig();
        return listSettingItem.get(key);
    }

    public synchronized void setSetting(String key, String data) {

        if (!readConfig()) {
            writeConfig(defaultSetting);
        }
        readConfig();
        listSettingItem.put(key, data);
        writeConfig(listSettingItem);



    }

    private boolean readConfig() {

        FileInputStream fileInputStream = null;
        try {
            fileInputStream = activity.openFileInput(settingFileName);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
            listSettingItem.clear();
            while (bufferedReader.ready()) {
                String[] arraySplitString = bufferedReader.readLine().split("=");
                listSettingItem.put(arraySplitString[0], arraySplitString[1]);
            }
            bufferedReader.close();
            fileInputStream.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    private void writeConfig(HashMap<String, String> hashMapArrayList) {

        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = activity.openFileOutput(settingFileName, Context.MODE_PRIVATE);
            for (Map.Entry<String, String> index: hashMapArrayList.entrySet()
                 ) {
                fileOutputStream.write((index.getKey() + "=" + index.getValue() + "\n").getBytes());
            }
            fileOutputStream.close();

        } catch (Exception e) {}

    }


}
