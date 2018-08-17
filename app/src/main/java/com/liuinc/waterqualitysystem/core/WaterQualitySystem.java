package com.liuinc.waterqualitysystem.core;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.liuinc.waterqualitysystem.core.data.BoundaryDTO;
import com.liuinc.waterqualitysystem.core.data.FieldDO;
import com.liuinc.waterqualitysystem.core.data.LimitDO;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static android.content.Context.MODE_PRIVATE;

public class WaterQualitySystem {
    private String TAG = this.getClass().getSimpleName();

    private Activity mActivity;
    private Map<String, FieldDO> mSettings = new TreeMap<>();
    private String mDeviceId = "";
    private Map<String, LimitDO> mFieldsLimits = new TreeMap<>();
    private RequestQueue mQueue;

    public static final int THINK_SPEAK_LIQUID_TEMPERATURE = 1;
    public static final int THINK_SPEAK_LIQUID_PH7 = 2;
    public static final int THINK_SPEAK_LIQUID_PH4 = 3;
    public static final int THINK_SPEAK_LIQUID_DO = 4;

    public int start(Activity activity) {
        mActivity = activity;
        loadDbSettings();
        mQueue = Volley.newRequestQueue(activity.getApplicationContext());
        startBackGroundService();
        return 0;
    }

    public int stop() {
        return 0;
    }

    public String getDevice() {
        return mDeviceId;
    }

    public String getDevice(int position) {
        if(position < mSettings.size()) {
            int i = 0;
            for (Map.Entry<String, FieldDO> item : mSettings.entrySet()) {
                if (i == position) {
                    return item.getKey();
                } else {
                    ++i;
                }
            }
        }
        return "";
    }

    private FieldDO getDeviceSetting(String device) {
        FieldDO field = mSettings.get(device);
        if (field != null)
            return field;
        return new FieldDO();
    }

    public LimitDO getDeviceLimits(String device) {
        LimitDO field = mFieldsLimits.get(device);
        if (field != null)
            return field;
        return new LimitDO();
    }

    public int setDevice(String device) {
        FieldDO field = mSettings.get(device);
        if(field != null) {
            mDeviceId = field.btMac;
        }
        return 0;
    }

    public String[] listDevicesReadName() {
        ArrayList<String> names = new ArrayList<>();
        if (mSettings.size() > 0) {
            for (Map.Entry<String, FieldDO> entry : mSettings.entrySet()) {
                if(entry.getValue().aliasName.length() == 0) {
                    names.add(entry.getValue().id);
                } else {
                    names.add(entry.getValue().aliasName);
                }
            }
        }
        return names.toArray(new String[0]);
    }

    public String getDeviceReadName() {
        if (mDeviceId.length() == 0)
            return "";
        FieldDO field = mSettings.get(mDeviceId);
        if(field != null) {
            if (field.aliasName.length() == 0) {
                return field.btMac;
            } else {
                return mDeviceId;
            }
        }
        return "";
    }

    public String[] listDevices() {
        ArrayList<String> names = new ArrayList<>(mSettings.keySet());
        return names.toArray(new String[0]);
    }

    public int addDevice(FieldDO field) {
        mSettings.put(field.btMac, field);
        mDeviceId = field.btMac;
        saveDbSettings();
        return 0;
    }

    public int removeDevice() {
        FieldDO field = mSettings.get(mDeviceId);
        if(field != null) {
            mSettings.remove(mDeviceId);
            saveDbSettings();
        }
        mDeviceId = getFirstDevice();
        return 0;
    }

    public int saveLimits(LimitDO limits) {
        mFieldsLimits.put(limits.btMac, limits);
        saveDbLimits();
        return 0;
    }

    public String getThinkSpeakUrl(int thinkSpeakParam) {
        return "https://thingspeak.com/channels/" + getDeviceSetting(mDeviceId).thinkSpeakId + "/charts/" + String.valueOf(thinkSpeakParam) + "?bgcolor=%23ffffff&color=%23d62020&dynamic=true&results=60&type=line&update=15";
    }

    public int getThinkSpeakUrlAsync(int thinkSpeakParam, final IHttpGet callback) {
        final String url = "https://thingspeak.com/channels/" + getDeviceSetting(mDeviceId).thinkSpeakId + "/field/" + String.valueOf(thinkSpeakParam) + "/last.html";
        StringRequest stringRequest = new StringRequest(url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.length() > 0) {
                            callback.onGet(response);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("TAG", error.getMessage(), error);
            }
        });
        mQueue.add(stringRequest);
        return 0;
    }

    public boolean isValid(int type, String value) {
        LimitDO limit = mFieldsLimits.get(getDevice());
        if (limit == null)
            return true;
        if (value.length() == 0)
            return true;
        float data = 0;
        try {
            data = Float.parseFloat(value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        switch (type) {
        case WaterQualitySystem.THINK_SPEAK_LIQUID_TEMPERATURE:
            if (data < limit.temperatureLow || data > limit.temperatureHigh) {
                return false;
            }
            break;
        case WaterQualitySystem.THINK_SPEAK_LIQUID_PH7:
            if (data < limit.ph7Low || data > limit.ph7High) {
                return false;
            }
            break;
        case WaterQualitySystem.THINK_SPEAK_LIQUID_PH4:
            if (data > limit.ph4) {
                return false;
            }
            break;
        case WaterQualitySystem.THINK_SPEAK_LIQUID_DO:
            if (data < limit.doo) {
                return false;
            }
            break;
        default:
        }
        return true;
    }

    private String getFirstDevice() {
        String deviceId = "";
        if (mSettings.size() > 0) {
            deviceId = mSettings.keySet().iterator().next();
        }
        return deviceId;
    }

    private void loadDbSettings() {
        String jsonString = mActivity.getSharedPreferences("wqs", MODE_PRIVATE).getString("settings", "");
//        jsonString = "[{\"bt_mac\":\"98:D3:32:10:CE:5C\",\"id\":\"B-00002-T\",\"ts_id\":\"468843\",\"alias\":\"石斑\",\"wifi_id\":\"\",\"wifi_pswd\":\"\"},{\"bt_mac\":\"98:D3:32:10:CE:57\",\"id\":\"B-00003-T\",\"ts_id\":\"468842\",\"alias\":\"\",\"wifi_id\":\"\",\"wifi_pswd\":\"\"}]";
        if (!jsonString.equals("")) {
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<FieldDO>>() {}.getType();
            ArrayList<FieldDO> fields = gson.fromJson(jsonString, listType);
            if (fields != null)
                for (FieldDO field : fields)
                    mSettings.put(field.btMac, field);
        }
        jsonString = mActivity.getSharedPreferences("wqs", MODE_PRIVATE).getString("limits", "");
//        jsonString = "[{\"bt_mac\":\"98:D3:32:10:CE:5C\",\"temperature_high\":0,\"temperature_low\":20,\"ph7_high\":3.14,\"ph7_low\":1.59,\"ph4\":2.6,\"do\":2.71,\"alarm\":false},{\"bt_mac\":\"98:D3:32:10:CE:57\",\"temperature_high\":8.2,\"temperature_low\":8,\"ph7_high\":1.8,\"ph7_low\":1.6,\"ph4\":2.8,\"do\":4.6,\"alarm\":false}]";
        if (!jsonString.equals("")) {
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<LimitDO>>() {}.getType();
            ArrayList<LimitDO> fields = gson.fromJson(jsonString, listType);
            if (fields != null)
                for (LimitDO field : fields)
                    mFieldsLimits.put(field.btMac, field);
        }

        mDeviceId = getFirstDevice();
    }

    private void saveDbSettings() {
        Gson gson = new Gson();
        ArrayList<FieldDO> saveObject = new ArrayList<>(mSettings.values());
        String jsonString = gson.toJson(saveObject);
        mActivity.getSharedPreferences("wqs", MODE_PRIVATE).edit().putString("settings", jsonString).apply();
    }

    private void saveDbLimits() {
        Gson gson = new Gson();
        ArrayList<LimitDO> saveObject = new ArrayList<>(mFieldsLimits.values());
        String jsonString = gson.toJson(saveObject);
        mActivity.getSharedPreferences("wqs", MODE_PRIVATE).edit().putString("limits", jsonString).apply();
    }

    private void startBackGroundService() {
        Intent intent = new Intent(mActivity, PollingService.class);

        String[] devices = listDevices();
        ArrayList<BoundaryDTO> list = new ArrayList<>();
        for(String device : devices) {
            if (device.length() > 0) {
                FieldDO field = getDeviceSetting(device);
                BoundaryDTO boundary = new BoundaryDTO(mDeviceId, field.thinkSpeakId);
                LimitDO limit = getDeviceLimits(device);
                if(limit.alarm) {
                    boundary.setTemperatureHigh(limit.temperatureHigh);
                    boundary.setTemperatureLow(limit.temperatureLow);
                    boundary.setPh7High(limit.ph7High);
                    boundary.setPh7Low(limit.ph7Low);
                    boundary.setPh4(limit.ph4);
                    boundary.setDoo(limit.doo);
                    list.add(boundary);
                }
            }
        }

        intent.putExtra("DEVICE_BOUNDARY", list);
        mActivity.startService(intent);
    }
}
